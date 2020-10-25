package com.zanderwohl.chunks.Client;

import com.zanderwohl.console.Message;

import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The class that communicates over the network with the server (the SimLoop Class) to synchronize the game state.
 */
public class Client implements Runnable {

    private URL serverUrl;
    private int serverPort;
    private final ClientIdentity identity;
    private ConcurrentLinkedQueue<Message> toConsole;

    /**
     *
     * @param url The URL of the server.
     * @param port The port the server will be listening on.
     * @param toConsole The queue to send messages to the server.
     */
    public Client(URL url, int port, ConcurrentLinkedQueue<Message> toConsole){
        this.serverUrl = url;
        this.serverPort = port;
        this.toConsole = toConsole;
        identity = new ClientIdentity("rexpup");
    }

    /**
     * Run the client. Will eventually start a window that contains a client for a particular game, whether that game
     * is remote or local.
     */
    @Override
    public void run() {
        toConsole.add(new Message("source=Client\nmessage=Client initialized."));
    }
}
