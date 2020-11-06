package com.zanderwohl.chunks.Server;

import com.zanderwohl.console.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientAccepter extends Thread {

    private ServerSocket serverSocket;
    private ConcurrentLinkedQueue<Message> toConsole;
    private List<ClientHandler> clients;

    public ClientAccepter(ServerSocket serverSocket, List<ClientHandler> clients,
                          ConcurrentLinkedQueue<Message> toConsole){
        this.serverSocket = serverSocket;
        this.clients = clients;
        this.toConsole = toConsole;
    }

    public void run(){
        while(true){
            try {
                Socket socket = serverSocket.accept();
                ClientHandler newClient = new ClientHandler(socket, toConsole);
                clients.add(newClient);
                newClient.start();
            } catch (IOException e) {
                toConsole.add(new Message("source=Client Accepter\nseverity=critical\nmessage="
                + "A client failed to connect to the server."));
            }
        }
    }
}
