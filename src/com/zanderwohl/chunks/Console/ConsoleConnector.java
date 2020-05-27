package com.zanderwohl.chunks.Console;

import com.zanderwohl.console.Message;
import com.zanderwohl.console.tests.SendMessagesLoop;

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
public class ConsoleConnector implements Runnable{

    private final int PORT;
    private ConcurrentLinkedQueue<Message> toConsole;
    private ConcurrentLinkedQueue<Message> fromConsole;

    /**
     *
     * @param port The port the Console can be found on.
     * @param toConsole The queue of Messages that the program wishes to send to the SuperConsole.
     * @param fromConsole The queue of messages that have been received from the console,
     *                    for consumption by the program.
     */
    public ConsoleConnector(int port,
                            ConcurrentLinkedQueue<Message> toConsole, ConcurrentLinkedQueue<Message> fromConsole){
        this.PORT = port;
        this.toConsole = toConsole;
        this.fromConsole = fromConsole;
    }

    @Override
    public void run() {
        try (var listener = new ServerSocket(this.PORT)){
            System.out.println("Console init.");
            Socket socket = listener.accept();
            Thread send = new Thread(new ConsoleConnector.Send(new PrintWriter(socket.getOutputStream(), true), toConsole));
            Thread receive = new Thread(new ConsoleConnector.Receive(new Scanner(socket.getInputStream()), fromConsole));
            send.start();
            receive.start();
            System.out.println("Console interface initialized.");
        } catch (IOException e) {
            System.err.println("Error at Console Connector's run method.");
            e.printStackTrace();
        }
    }

    /**
     * Sends Messages over the network. Also sends a test message every ten seconds.
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
                    if(nextSend <= Instant.now().getEpochSecond()){
                        nextSend = Instant.now().getEpochSecond() + 10;
                        System.out.println("Sending packet.");
                        Message m = new Message(SendMessagesLoop.blankMessage());
                        output.println(m.toString());
                    }
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
                    System.out.println("Got user input:\n\t" + userMessage);
                    //System.out.println(m.toString());
                    String returnMessage = "Dummy got the input: " + m.getAttribute("message");
                    queue.add((new Message("source=Connector\nmessage=" + returnMessage
                            + "\ntime=" + Instant.now().getEpochSecond())));
                    userMessage = "";
                } else {
                    userMessage += line + "\n";
                }
            }
        }
    }
}
