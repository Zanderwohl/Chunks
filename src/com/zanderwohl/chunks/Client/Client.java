package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Console.Console;
import com.zanderwohl.chunks.Main;
import com.zanderwohl.console.Message;
import com.zanderwohl.console.SuperConsole;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The class that communicates over the network with the server (the SimLoop Class) to synchronize the game state.
 */
public class Client implements Runnable {

    private Socket clientSocket;
    private String serverHost;
    private int serverPort;
    private final ClientIdentity identity;
    private ConcurrentLinkedQueue<Message> toConsole;

    PrintWriter out;
    ObjectOutputStream objectOut;

    /**
     * An entry point that only starts a client, not a server.
     * @param args Unused.
     */
    public static void main(String[] args) {
        int port = 32112;
        Main.prepareEnvironment();

        ConcurrentLinkedQueue<Message> toConsole = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Message> fromConsole = new ConcurrentLinkedQueue<>();

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
    public Client(String host, int port, ConcurrentLinkedQueue<Message> toConsole){
        this.serverHost = host;
        this.serverPort = port;
        this.toConsole = toConsole;
        identity = new ClientIdentity("Player " + (new Random()).nextInt(1000));


    }

    /**
     * Run the client. Will eventually start a window that contains a client for a particular game, whether that game
     * is remote or local.
     */
    @Override
    public void run() {
        toConsole.add(new Message("source=Client\nmessage=Client initialized."));

        try {
            clientSocket = new Socket(serverHost, serverPort);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            objectOut.writeObject(identity);
        } catch (IOException e){
            toConsole.add(new Message("source=Client\nseverity=critical\nmessage="
                    + "Could not connect to host '" + serverHost + "' on port " + serverPort + "!"));
            return;
        }

        JFrame testFrame = new JFrame();
        testFrame.setVisible(true);
    }
}

