package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Console.Console;
import com.zanderwohl.chunks.Delta.Chat;
import com.zanderwohl.chunks.Delta.Delta;
import com.zanderwohl.chunks.Main;
import com.zanderwohl.console.Message;
import com.zanderwohl.console.SuperConsole;

import java.io.*;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The class that communicates over the network with the server (the SimLoop Class) to synchronize the game state.
 */
public class Client implements Runnable {

    protected volatile boolean running = true;

    private Socket socket;
    private String serverHost;
    private int serverPort;
    private final ClientIdentity identity;
    private ArrayBlockingQueue<Message> toConsole;

    private ArrayBlockingQueue<Delta> serverUpdates;
    private ArrayBlockingQueue<Delta> clientUpdates;

    private ConcurrentLinkedQueue<Message> queue;

    /**
     * An entry point that only starts a client, not a server.
     * @param args Unused.
     */
    public static void main(String[] args) {
        int port = 32112;
        Main.prepareEnvironment();

        ArrayBlockingQueue<Message> toConsole = new ArrayBlockingQueue<>(50);
        ArrayBlockingQueue<Message> fromConsole = new ArrayBlockingQueue<>(50);

        Client singleplayerClient = new Client("localhost", port, toConsole);
        Thread clientThread = new Thread(singleplayerClient);

        //Start the game's console - not the user-facing console, but the part of this program that receives and sends
        //to SuperConsole - on its own thread.
        Console consoleConnector = new Console(toConsole, fromConsole);
        Thread consoleConnectorThread = new Thread(consoleConnector);
        consoleConnectorThread.start();

        //Start a SuperConsole window
        SuperConsole console = new SuperConsole();
        Thread consoleThread = new Thread(console);
        consoleThread.start();

        clientThread.start();
    }

    /**
     *
     * @param host The URL of the server.
     * @param port The port the server will be listening on.
     * @param toConsole The queue to send messages to the server.
     */
    public Client(String host, int port, ArrayBlockingQueue<Message> toConsole){
        this.serverHost = host;
        this.serverPort = port;
        this.toConsole = toConsole;
        identity = new ClientIdentity("Player " + (new Random()).nextInt(1000));

        int queueSize = 30;
        serverUpdates = new ArrayBlockingQueue<>(queueSize);
        clientUpdates = new ArrayBlockingQueue<>(queueSize);
    }

    /**
     * Run the client. Will eventually start a window that contains a client for a particular game, whether that game
     * is remote or local.
     */
    @Override
    public void run() {
        toConsole.add(new Message("source=Client\nmessage=Client initialized."));

        Thread send;
        Thread receive;

        try {
            socket = new Socket(serverHost, serverPort);

            send = new Thread(new Send(this, socket, clientUpdates));
            receive = new Thread(new Receive(this, socket, serverUpdates));

        } catch (IOException e){
            toConsole.add(new Message("source=Client\nseverity=critical\nmessage="
                    + "Could not connect to host '" + serverHost + "' on port " + serverPort + "!"));
            return;
        }

        send.start();
        receive.start();

        ClientWindow w = new ClientWindow(clientUpdates, serverUpdates, identity, toConsole);
        w.start();
    }

    private static class Send implements Runnable {

        Client parent;
        Socket server;
        ArrayBlockingQueue<Delta> clientUpdates;

        public Send(Client client, Socket server, ArrayBlockingQueue<Delta> clientUpdates){
            parent = client;
            this.server = server;
            this.clientUpdates = clientUpdates;
        }

        @Override
        public void run() {
            PrintWriter out;
            ObjectOutputStream objectOut;
            try {
                out = new PrintWriter(server.getOutputStream(), true);
                objectOut = new ObjectOutputStream(server.getOutputStream());

                objectOut.writeObject(parent.identity);

                while (parent.running) {
                    objectOut.writeObject(clientUpdates.take());
                }
            } catch (IOException e){
                parent.toConsole.add(new Message("severity=critical\nsource=Client\nmessage="
                        + "Client disconnected from server!"));
                parent.running = false;
                return;
            } catch (InterruptedException e){
                parent.toConsole.add(new Message("severity=critical\nsource=Client\nmessage="
                + "Client was unexpectedly interrupted!"));
            }
            parent.running = false;
        }
    }

    private static class Receive implements Runnable {

        Client parent;
        Socket server;
        ArrayBlockingQueue<Delta> serverUpdates;

        public Receive(Client client, Socket server, ArrayBlockingQueue<Delta> serverUpdates){
            parent = client;
            this.server = server;
            this.serverUpdates = serverUpdates;
        }

        @Override
        public void run() {
            InputStream in;
            ObjectInputStream oin;

            try {
                in = server.getInputStream();
                oin = new ObjectInputStream(in);
                while (parent.running) {
                    Object object = oin.readObject();
                    if(object instanceof Delta){
                        Delta d = (Delta) object;
                        serverUpdates.add(d);
                    } else {
                        parent.toConsole.add(new Message("source=Client Handler\nseverity=warning\nmessage="
                                + "The server sent a non-delta object!"));
                    }
                }
            } catch (IOException e) {
                parent.toConsole.add(new Message("severity=critical\nsource=Client\nmessage="
                        + "Client disconnected from server!"));
                parent.running = false;
                return;
            } catch (ClassNotFoundException e){
                parent.toConsole.add(new Message("severity=critical\nsource=Client\nmessage="
                        + "Server sent object that client does not understand."));
                parent.running = false;
                return;
            }
            parent.running = false;
        }
    }
}

