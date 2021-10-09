package com.zanderwohl.chunks.Server;

import com.zanderwohl.chunks.Client.Client;
import com.zanderwohl.chunks.Client.ClientIdentity;
import com.zanderwohl.chunks.Console.StartupSettings;
import com.zanderwohl.chunks.Delta.*;
import com.zanderwohl.chunks.World.Coord;
import com.zanderwohl.chunks.World.WorldManager;
import com.zanderwohl.console.Message;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The server-side class that deals with listening to and
 */
public class ClientHandler implements Runnable{

    protected volatile boolean running;

    private Socket socket;
    protected ArrayBlockingQueue<Message> toConsole;
    protected ClientIdentity identity;

    private ConcurrentHashMap<ClientIdentity,ClientHandler> clientsById;

    protected ArrayBlockingQueue<Delta> clientUpdates;
    protected ArrayBlockingQueue<Delta> serverUpdates;

    /**
     * A Client Handler sends and receives data from a client, updating the server about client actions and vice
     * versa.
     * @param clientSocket The socket the client is behind.
     * @param toConsole The stream of messages to the server's Console.
     */
    public ClientHandler(Socket clientSocket, ArrayBlockingQueue<Message> toConsole,
                         ConcurrentHashMap<ClientIdentity,ClientHandler> clientsById,
                         ArrayBlockingQueue<Delta> clientUpdates){
        this.running = true;
        this.socket = clientSocket;
        this.toConsole = toConsole;
        this.clientsById = clientsById;
        this.clientUpdates = clientUpdates;
        this.serverUpdates = new ArrayBlockingQueue<>(100);
    }

    public void disconnect(String reason){
        if(reason == null){
            reason = "An unspecified reason.";
        }
        reason = "Disconnected from Server: " + reason;
        running = false;
        serverUpdates.add(new Kick(reason));
    }

    /**
     * Oh goodness please no.
     * I really don't know how to fix this. 2021-09-18
     */
    public void run(){
        InputStream in;
        ObjectInputStream oin;
        BufferedReader br;
        ObjectOutputStream out;
        try{
            in = socket.getInputStream();
            oin = new ObjectInputStream(in);
            //br = new BufferedReader(new InputStreamReader(in));
            out = new ObjectOutputStream(socket.getOutputStream());
            identity = (ClientIdentity) oin.readObject();
            clientsById.put(identity, this);
        } catch (IOException e) {
            toConsole.add(new Message("source=Client Handler\nseverity=critical\nmessage="
            + "A client just failed to connect."));
            return;
        } catch (ClassNotFoundException e){
            toConsole.add(new Message("source=Client Handler\nseverity=critical\nmessage="
            + "The first communication from a client was NOT its identity! Disconnecting."));
            return;
        }
        toConsole.add(new Message("source=Client Handler\nmessage=" +
                "User " + identity.getUsername() + " connected to the server!"));
        serverUpdates.add(new Hello(StartupSettings.SERVER_NAME, StartupSettings.MOTD));

        Send send = new Send(this, out);
        Receive receive = new Receive(this, oin);

        Thread sendThread = new Thread(send);
        Thread receiveThread = new Thread(receive);

        sendThread.start();
        receiveThread.start();
    }

    private static class Send implements Runnable {

        private ClientHandler parent;
        private ObjectOutputStream out;

        public Send(ClientHandler parent, ObjectOutputStream out){
            this.parent = parent;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                PPos initialPos = new PPos(20.0, 40.0, 20.0, 0.0, 0.0,
                        parent.identity.getDisplayName());
                out.writeObject(initialPos);
                sendInitialVolumes(initialPos.toCoord());
                while (parent.running) {
                    Delta update = parent.serverUpdates.take();
                    out.writeObject(update);
                }
            } catch (IOException e) {
                parent.running = false;
                parent.toConsole.add(new Message("source=Client Handler\nseverity=critical\nmessage="
                    + "Connection to client " + parent.identity.getDisplayName() + " failed.\n" +
                        "Maybe a Delta does not implement Serializable?"));
            } catch (InterruptedException e){
                parent.toConsole.add(new Message("source=Client Handle\nseverity=critical\nmessage="
                + "Client Handler was unexpectedly interrupted!"));
            }
        }

        private void sendInitialVolumes(Coord location){
            Delta worldRequest = new WorldRequest(null);
            worldRequest.setFrom(parent.identity);
            parent.clientUpdates.add(worldRequest);
            StartingVolumesRequest svr = new StartingVolumesRequest(location);
            svr.setFrom(parent.identity);
            parent.clientUpdates.add(svr);
        }
    }

    private static class Receive implements Runnable {

        private ClientHandler parent;
        private ObjectInputStream in;

        public Receive(ClientHandler parent, ObjectInputStream in){
            this.parent = parent;
            this.in = in;
        }

        @Override
        public void run() {
            try {
                try {
                    while(parent.running){
                        Object o = in.readObject();
                        if(o instanceof Delta){
                            Delta d = (Delta) o;
                            d.setFrom(parent.identity);
                            parent.clientUpdates.add(d);
                        } else {
                            parent.toConsole.add(new Message("source=Client Handler\nseverity=warning\nmessage="
                            + "The client " + parent.identity + " sent a non-delta object!"));
                        }
                    }
                } catch (ClassNotFoundException e) {
                    parent.toConsole.add(new Message("source=Client Handler\nseverity=warning\nmessage="
                    + "The client " + parent.identity + " sent an unrecognized object!"));
                }
            } catch (IOException e) {
                parent.running = false;

            }
        }
    }
}
