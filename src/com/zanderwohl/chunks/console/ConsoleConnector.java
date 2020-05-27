package com.zanderwohl.chunks.console;

import com.zanderwohl.console.SuperConsole;
import com.zanderwohl.console.Message;
import com.zanderwohl.console.tests.DummyProgram;
import com.zanderwohl.console.tests.SendMessagesLoop;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsoleConnector implements Runnable{

    private final int PORT;

    public ConsoleConnector(int port){
        this.PORT = port;
    }

    @Override
    public void run() {
        try (var listener = new ServerSocket(this.PORT)){
            System.out.println("Console init.");
            ConcurrentLinkedQueue<Message> loopbackQueue = new ConcurrentLinkedQueue<>();
            Socket socket = listener.accept();
            Thread send = new Thread(new ConsoleConnector.Send(new PrintWriter(socket.getOutputStream(), true), loopbackQueue));
            Thread receive = new Thread(new ConsoleConnector.Receive(new Scanner(socket.getInputStream()), loopbackQueue));
            send.start();
            receive.start();
            System.out.println("Console interface initialized.");
        } catch (IOException e) {
            System.err.println("Error at Console Connector's run method.");
            e.printStackTrace();
        }
    }

    private static class Send implements Runnable {

        private PrintWriter output;
        ConcurrentLinkedQueue<Message> queue;

        public Send(PrintWriter output, ConcurrentLinkedQueue loopbackQueue){
            this.output = output;
            queue = loopbackQueue;
        }

        @Override
        public void run(){
            long nextSend = Instant.now().getEpochSecond() + 1;
            try {
                while(true){
                    if(nextSend == Instant.now().getEpochSecond()){
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

    private static class Receive implements Runnable {

        Scanner input;
        ConcurrentLinkedQueue<Message> queue;

        public Receive(Scanner input, ConcurrentLinkedQueue loopbackQueue){
            this.input = input;
            queue = loopbackQueue;
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
                    queue.add((new Message("source=Dummy\nmessage=" + returnMessage
                            + "\ntime=" + Instant.now().getEpochSecond())));
                    userMessage = "";
                } else {
                    userMessage += line + "\n";
                }
            }
        }
    }
}
