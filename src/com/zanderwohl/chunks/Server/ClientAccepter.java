package com.zanderwohl.chunks.Server;

import com.zanderwohl.console.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientAccepter implements Runnable {

    private ServerSocket serverSocket;
    private ConcurrentLinkedQueue<Message> toConsole;
    private List<Thread> clients;

    private int maximumClients = 10;

    public ClientAccepter(ServerSocket serverSocket, List<Thread> clients,
                          ConcurrentLinkedQueue<Message> toConsole){
        this.serverSocket = serverSocket;
        this.clients = clients;
        this.toConsole = toConsole;
    }

    @Override
    public void run(){
        while(true){
            try {
                Socket socket = serverSocket.accept();
                ClientHandler newClient = new ClientHandler(socket, toConsole);
                Thread clientThread = new Thread(newClient);
                clients.add(clientThread);
                clientThread.start();
                toConsole.add(new Message("source=Client Accepter\nmessage="
                + "New client connected!"));
            } catch (IOException e) {
                toConsole.add(new Message("source=Client Accepter\nseverity=critical\nmessage="
                + "A client failed to connect to the server."));
            }
        }
    }
}
