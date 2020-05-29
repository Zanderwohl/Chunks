package com.zanderwohl.chunks.Console;

import com.zanderwohl.console.Message;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Console connector is the sole broker between the Message queues and the network.
 * It sends messages from the toConsole queue over the network.
 * It receives messages from the network into the fromConsole queue.
 */
public class Console implements Runnable{

    private final int PORT = 288;
    private static ConcurrentLinkedQueue<Message> toConsole;
    private static ConcurrentLinkedQueue<Message> fromConsole;

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
    public Console(ConcurrentLinkedQueue<Message> toConsole, ConcurrentLinkedQueue<Message> fromConsole){
        this.toConsole = toConsole;
        this.fromConsole = fromConsole;
    }

    @Override
    public void run() {
        try (var listener = new ServerSocket(this.PORT)){
            //System.out.println("Console init.");
            Console.log("Console initializing...","Console Broker", "NORMAL", "");
            Socket socket = listener.accept();
            Thread send = new Thread(new Console.Send(new PrintWriter(socket.getOutputStream(), true), toConsole));
            Thread receive = new Thread(new Console.Receive(new Scanner(socket.getInputStream()), fromConsole));
            send.start();
            receive.start();
            Console.log("Console initialized...","Console Broker", "NORMAL", "");
        } catch (IOException e) {
            System.err.println("Error at Console Connector's run method.");
            e.printStackTrace();
        }
    }

    /**
     * Sends Messages over the network.
     */
    private static class Send implements Runnable {

        private PrintWriter output;
        ConcurrentLinkedQueue<Message> queue;

        public Send(PrintWriter output, ConcurrentLinkedQueue queue){
            this.output = output;
            this.queue = queue;
        }

        @Override
        public void run(){
            long nextSend = Instant.now().getEpochSecond() + 1;
            try {
                while(true){
                    //if(nextSend <= Instant.now().getEpochSecond()){
                    //    nextSend = Instant.now().getEpochSecond() + 10;
                    //    System.out.println("Sending packet.");
                    //    Message m = new Message(SendMessagesLoop.blankMessage());
                    //    output.println(m.toString());
                    //}
                    while(!queue.isEmpty()){
                        output.println(queue.remove().toString());
                    }
                }
            } catch (Exception e){
                System.out.println("Error:\n" + e);
            }
        }
    }

    /**
     * Receives Messages from the console and places them on the fromConsole queue.
     */
    private static class Receive implements Runnable {

        Scanner input;
        ConcurrentLinkedQueue<Message> queue;

        public Receive(Scanner input, ConcurrentLinkedQueue queue){
            this.input = input;
            this.queue = queue;
        }

        @Override
        public void run() {
            String userMessage = "";
            while(input.hasNextLine()){
                String line = input.nextLine();
                if(line.equals("EOM")) {
                    Message m = new Message(userMessage);
                    queue.add(m);
                    //System.out.println("Got user input:\n\t" + userMessage);
                    //System.out.println(m.toString());
                    //String returnMessage = "Dummy got the input: " + m.getAttribute("message");
                    //queue.add((new Message("source=Connector\nmessage=" + returnMessage
                    //        + "\ntime=" + Instant.now().getEpochSecond())));
                    userMessage = "";
                } else {
                    userMessage += line + "\n";
                }
            }
        }
    }
}
