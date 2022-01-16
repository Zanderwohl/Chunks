package com.zanderwohl.chunks.Server;

import com.zanderwohl.chunks.Client.ClientIdentity;
import com.zanderwohl.chunks.Console.StartupSettings;
import com.zanderwohl.chunks.Delta.Delta;
import com.zanderwohl.chunks.World.WorldManager;
import com.zanderwohl.console.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread that runs in the background and listens for new attempts to connect.
 * On a new connection, start a new thread for the new client.
 */
public class ClientAccepter implements Runnable {

    private ServerSocket serverSocket;
    private ArrayBlockingQueue<Message> toConsole;
    private List<Thread> clients;
    private ConcurrentHashMap<String, ClientHandler> clientsById;
    private ArrayBlockingQueue<Delta> clientUpdates;

    /**
     * Start a Client Accepter. There should probably only be one of these.
     * Perhaps a second could run on another socket, listening on a different port, to add the new user to a
     * different environment?
     * @param serverSocket The socket the server is listening on.
     * @param clients The list of clients, to which new clients should be added.
     * @param clientsById A hashmap of all clients, indexed by their ID object.
     * @param toConsole The queue of messages to send to the server console.
     */
    public ClientAccepter(ServerSocket serverSocket, List<Thread> clients,
                          ConcurrentHashMap<String,ClientHandler> clientsById,
                          ArrayBlockingQueue<Message> toConsole,
                          ArrayBlockingQueue<Delta> clientUpdates){
        this.serverSocket = serverSocket;
        this.clients = clients;
        this.clientsById = clientsById;
        this.toConsole = toConsole;
        this.clientUpdates = clientUpdates;
    }

    /**
     * Run the Client Accepter, which will continuously listen for new clents.
     */
    @Override
    public void run(){
        while(true){
            try { //TODO: Have a pool of client threads, and distribute clients amongst the pool.
                Socket socket = serverSocket.accept();
                ClientHandler newClient = new ClientHandler(socket, toConsole, clientsById, clientUpdates);
                Thread clientThread = new Thread(newClient);
                clientThread.start();
                if(clients.size() >= StartupSettings.MAX_USERS){
                    toConsole.add(new Message("source=Client Accepter\nseverity=warning\nmessage=Player '" +
                            newClient.identity.getDisplayName() + "' tried to join but the server was full!"));
                    newClient.disconnect("Server is full (" + StartupSettings.MAX_USERS + " users)!");
                    //socket.close();
                } else {
                    clients.add(clientThread);
                    toConsole.add(new Message("source=Client Accepter\nmessage="
                            + "New client connected!"));
                }
            } catch (IOException e) {
                toConsole.add(new Message("source=Client Accepter\nseverity=critical\nmessage="
                + "A client failed to connect to the server."));
            }
        }
    }
}
