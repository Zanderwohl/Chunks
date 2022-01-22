package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Console.ConsoleBroker;
import com.zanderwohl.chunks.Console.StartupSettings;
import com.zanderwohl.chunks.Delta.Delta;
import com.zanderwohl.chunks.Gamelogic.BadGame;
import com.zanderwohl.chunks.Gamelogic.IGameLogic;
import com.zanderwohl.chunks.Main;
import com.zanderwohl.chunks.World.WorldManager;
import com.zanderwohl.console.Message;
import com.zanderwohl.console.SuperConsole;
import com.zanderwohl.util.ExceptionUtils;

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
    private final String serverHost;
    private final int serverPort;
    private final ClientIdentity identity;
    private final ArrayBlockingQueue<Message> toConsole;

    private final ArrayBlockingQueue<Delta> serverUpdates;
    private final ArrayBlockingQueue<Delta> clientUpdates;

    private ConcurrentLinkedQueue<Message> queue;

    private final IGameLogic gameLogic;

    protected WorldManager worldManager;

    /**
     * An entry point that only starts a client, not a server.
     * This is a bunch of hard-coded stuff that shouldn't be.
     * @param args Unused.
     */
    public static void main(String[] args) {
        int port = 32112;
        Main.prepareEnvironment();

        ArrayBlockingQueue<Message> toConsole = new ArrayBlockingQueue<>(50);
        ArrayBlockingQueue<Message> fromConsole = new ArrayBlockingQueue<>(50);

        Client singleplayerClient = new Client("localhost", port, toConsole, new BadGame(toConsole));
        Thread clientThread = new Thread(singleplayerClient);

        //Start the game's console - not the user-facing console, but the part of this program that receives and sends
        //to SuperConsole - on its own thread.
        ConsoleBroker consoleBroker = new ConsoleBroker(toConsole, fromConsole);
        Thread consoleConnectorThread = new Thread(consoleBroker);
        consoleConnectorThread.start();

        //Start a SuperConsole window
        SuperConsole console = new SuperConsole();
        console.newConnection("Local Server","localhost",288);

        clientThread.start();
    }

    /**
     *
     * @param host The URL of the server.
     * @param port The port the server will be listening on.
     * @param toConsole The queue to send messages to the server.
     */
    public Client(String host, int port, ArrayBlockingQueue<Message> toConsole, IGameLogic gameLogic){
        this.serverHost = host;
        this.serverPort = port;
        this.toConsole = toConsole;
        identity = new ClientIdentity("Player " + (new Random()).nextInt(1000));

        this.gameLogic = gameLogic;

        int queueSize = 100; // Magic number. 30 is too small.
        serverUpdates = new ArrayBlockingQueue<>(queueSize);
        clientUpdates = new ArrayBlockingQueue<>(queueSize);

        worldManager = new WorldManager(toConsole, StartupSettings.DEFAULT_WORLD_NAME);

        gameLogic.setBlockLibrary(worldManager.library);
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

        ClientLoop w = new ClientLoop(clientUpdates, serverUpdates, identity, toConsole, gameLogic);
        w.run();
    }

    /**
     * The thread that dequeues delta updates and sends them to the server.
     */
    private static class Send implements Runnable {

        Client parent;
        Socket server;
        ArrayBlockingQueue<Delta> clientUpdates;

        /**
         * Only send constructor.
         * @param client The client object which is the parent.
         * @param server The socket connected to the server.
         * @param clientUpdates The queue from the client of updates.
         */
        public Send(Client client, Socket server, ArrayBlockingQueue<Delta> clientUpdates){
            parent = client;
            this.server = server;
            this.clientUpdates = clientUpdates;
        }

        /**
         * Loop and send updates. Can stop the client if an error occurs.
         */
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
                        + "Client " + parent.identity.getDisplayName() + " disconnected from server! "
                        + ExceptionUtils.boxMessage(e) + " " + ExceptionUtils.errorSource(e)));
                parent.running = false;
                return;
            } catch (InterruptedException e){
                parent.toConsole.add(new Message("severity=critical\nsource=Client\nmessage="
                + "Client was unexpectedly interrupted!"));
            }
            parent.running = false;
        }
    }

    /**
     * The thread that takes updates from the server and enqueues them for the client.
     */
    private static class Receive implements Runnable {

        Client parent;
        Socket server;
        ArrayBlockingQueue<Delta> serverUpdates;

        /**
         * The only receive constructor.
         * @param client The client which is the parent.
         * @param server The socket to the server currently-connected to.
         * @param serverUpdates The queue of updates from the server.
         */
        public Receive(Client client, Socket server, ArrayBlockingQueue<Delta> serverUpdates){
            parent = client;
            this.server = server;
            this.serverUpdates = serverUpdates;
        }

        /**
         * Loop and receive updates. Can stop the client if an error occurs.
         */
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
                        + "Client " + parent.identity.getDisplayName()
                        + ExceptionUtils.boxMessage(e) + " " + ExceptionUtils.errorSource(e)));
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

