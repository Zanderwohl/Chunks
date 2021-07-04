package com.zanderwohl.chunks.Console;

import com.zanderwohl.console.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Console connector is the sole broker between the Message queues and the network.
 * It sends messages from the toConsole queue over the network.
 * It receives messages from the network into the fromConsole queue.
 */
public class ConsoleBroker implements Runnable{

    private final int PORT = 288;
    private static ArrayBlockingQueue<Message> toConsole;
    private static ArrayBlockingQueue<Message> fromConsole;

    HashMap<String, Connection> connections = new HashMap<>();

    public static void log(String message, String source, String severity, String category){
        String time = Instant.now().getEpochSecond() + "";
        toConsole.add(new Message("message=" + message + "\nseverity=" + severity + "\ncategory=" + category +
                "\nsource=" + source));
    }

    /**
     *
     * @param toConsole The queue of Messages that the program wishes to send to the SuperConsole.
     * @param fromConsole The queue of messages that have been received from the console,
     *                    for consumption by the program.
     */
    public ConsoleBroker(ArrayBlockingQueue<Message> toConsole, ArrayBlockingQueue<Message> fromConsole){
        this.toConsole = toConsole;
        this.fromConsole = fromConsole;
    }

    /**
     * Run the console, including a send and receive thread for communicating both ways.
     */
    @Override
    public void run() {
        //TODO: Rewrite to allow multiple to/from queues. Currently, multiple connections will just take from the top of the stack with no discernment.
        int connectionIndex = 0;
        while(true){
            try (var listener = new ServerSocket(this.PORT)){
                ConsoleBroker.log("Console initializing...","Console Broker", "NORMAL", "None");
                Socket socket = listener.accept();
                Connection newConnection = new Connection(connectionIndex, toConsole, fromConsole, socket);
                connections.put(connectionID(connectionIndex), newConnection);
                connectionIndex++;
                ConsoleBroker.log("Console initialized...","Console Broker", "NORMAL", "None");
            } catch (IOException e) {
                ConsoleBroker.log("Connection failed.", "Console Broker", "CRITICAL", "None");
            }
        }
    }

    /**
     * My goodness, are we managing multiple connections now? Classes in classes in classes.
     */
    private static class Connection{

        /**
         * Manages the connection to a particular connected console.
         * //TODO: Add authentication?? idek where that should go.
         * @param toConsole Messages received from the game and server, to send to the console.
         * @param fromConsole Messages received from the console, to send to the game.
         * @param socket The socket with nothing set up. Basically a raw, unfettered socket. Delicious.
         */
        public Connection(int index,
                          ArrayBlockingQueue toConsole,
                          ArrayBlockingQueue fromConsole,
                          Socket socket){
            try {
                AtomicBoolean running = new AtomicBoolean(true);
                Thread send = new Thread(new ConsoleBroker.Send(running, new PrintWriter(socket.getOutputStream(), true), toConsole));
                Thread receive = new Thread(new ConsoleBroker.Receive(running, new Scanner(socket.getInputStream()), fromConsole, index));
                send.start();
                receive.start();
            } catch (IOException e) {

            }
        }
    }

    /**
     * Sends Messages over the network.
     */
    private static class Send implements Runnable {

        private PrintWriter output;
        private ArrayBlockingQueue<Message> queue;

        AtomicBoolean running;

        public Send(AtomicBoolean running, PrintWriter output, ArrayBlockingQueue queue){
            this.output = output;
            this.queue = queue;
            this.running = running;
        }

        @Override
        public void run(){
            long nextSend = Instant.now().getEpochSecond() + 1;
            try {
                while(running.get()){
                    output.println(queue.take().toString());
                }
            } catch (Exception e){
                System.out.println("Error:\n" + e);
                running.set(false);
            }
        }
    }

    /**
     * Receives Messages from the console and places them on the fromConsole queue.
     */
    private static class Receive implements Runnable {

        private Scanner input;
        private ArrayBlockingQueue<Message> queue;

        AtomicBoolean running;

        int index;
        String connectionID;

        public Receive(AtomicBoolean running, Scanner input, ArrayBlockingQueue queue, int index){
            this.input = input;
            this.queue = queue;
            this.running = running;
            this.index = index;
            connectionID = connectionID(index);
        }

        @Override
        public void run() {
            String userMessage = "";
            while(input.hasNextLine() && running.get()){
                String line = input.nextLine();
                if(line.equals("EOM")) {
                    Message m = new Message(userMessage);
                    m.setAttribute("from", connectionID);
                    queue.add(m);
                    userMessage = "";
                } else {
                    userMessage += line + "\n";
                }
            }
            running.set(false);
        }
    }

    public static String connectionID(int index){
        return "console_" + index;
    }
}
