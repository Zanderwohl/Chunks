package com.zanderwohl.chunks.Server;

import com.zanderwohl.chunks.Client.ClientIdentity;
import com.zanderwohl.chunks.Console.CommandManager;
import com.zanderwohl.chunks.World.WorldManager;
import com.zanderwohl.console.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The thread that simulates the logic of the game world.
 */
public class SimLoop implements Runnable {

    private final double ONE_BILLION = 1000000000.0;
    public final double SIM_FPS = 20.0;
    private final double SIM_NS = ONE_BILLION / SIM_FPS;

    private int port;

    private CommandManager commandManager;
    private WorldManager worldManager;

    private ConcurrentLinkedQueue<Message> toConsole;
    private ConcurrentLinkedQueue<Message> fromConsole;

    private ServerSocket serverSocket;
    private List<Thread> clients;
    private ConcurrentHashMap<ClientIdentity,ClientHandler> clientsById;
    private ClientAccepter clientAccepter;

    /**
     * Only constructor?
     * TODO: Fix this function's documentation.
     * @param toConsole The queue of messages to send to the console.
     * @param fromConsole The queue of messages from the console to be consumed.
     * @param port The port on which the server should run.
     * @throws IOException When the server cannot bind to the port.
     */
    public SimLoop(ConcurrentLinkedQueue<Message> toConsole, ConcurrentLinkedQueue<Message> fromConsole, int port) throws IOException {
        this.port = port;
        this.toConsole = toConsole;
        this.fromConsole = fromConsole;
        this.serverSocket = new ServerSocket(port);

        worldManager = new WorldManager(toConsole);
        commandManager = new CommandManager(toConsole, fromConsole, worldManager, this);
        clients = Collections.synchronizedList(new ArrayList<Thread>());
        clientsById = new ConcurrentHashMap<>();
        clientAccepter = new ClientAccepter(serverSocket, clients, clientsById, toConsole);
    }

    /**
     * Update the simulation.
     * @param delta The difference in time from this to the last update.
     */
    private void update(double delta) {
        commandManager.processCommands();
        commandManager.doCommands();

        /* //UNCOMMENT THIS TO CAUSE HORRIBLE SERVER LAG.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e){
            System.err.println("no");
        }*/
        //System.out.println(delta);
    }

    /**
     * Send updates to all clients over the network.
     */
    private void updateClients(){
        //for(int i = 0; i < )
    }

    /**
     * Send update information to a single client.
     * Usually called in a loop to update all clients in a row.
     * @param index The index of the client to update.
     */
    private void updateClient(int index){

    }

    /**
     * Return a client as searched by an exact string match.
     * @param name The name to search for.
     * @returns The identity of a particular client.
     */
    public ClientIdentity findClientByDisplayName(String name){
        ClientIdentity client = null;
        for(ClientIdentity ci: clientsById.keySet()){
            if(ci.getDisplayName().equals(name)){
                client = ci;
            }
        }
        return client;
    }

    /**
     * Disconnects a user from the server gracefully.
     * @param user The user to disconnect.
     * @param reason If active, will tell the user why they were disconnected.
     * @returns True if the user was successfully disconnected, false otherwise.
     */
    public boolean disconnectUser(ClientIdentity user, String reason){
        ClientHandler threadToKill = clientsById.get(user);
        if(threadToKill == null){
            return false;
        }
        //TODO: Save user state.
        clients.remove(threadToKill);
        clientsById.remove(user);
        return true;
    }

    /**
     * Remove all clients that have disconnected, to allow for more clients to be added.
     */
    private void pruneDeadClients(){
        Iterator<Thread> i = clients.iterator();
        while(i.hasNext()){
            Thread t = i.next();
            if(!t.isAlive()){
                clients.remove(t);
            }
        }
    }

    /**
     * Run the world. Simulate physics and deal with client updates. Update clients.
     */
    @Override
    public void run() {
        long lastNow = System.nanoTime();
        double delta = 0.0;
        long lastFPSTime = 0;

        Thread clientAccepterThread = new Thread(clientAccepter);
        clientAccepterThread.start();

        while(true){
            long now = System.nanoTime();
            long updateLength = now - lastNow;
            lastNow = now;
            delta = updateLength / SIM_NS;

            lastFPSTime += updateLength;
            if(lastFPSTime >= ONE_BILLION){
                lastFPSTime = 0;
            }

            pruneDeadClients(); //TODO: Only do this once per second, or maybe less.
            update(delta);
            updateClients();

            try {
                long sleepTime = (long)(lastNow - System.nanoTime() + SIM_NS) / 1000000;
                if(sleepTime < 0){
                    toConsole.add(new Message("source=Sim Loop\nseverity=warning\n"
                            + "message=Server can't keep up with physics! " + (-sleepTime) + " ns behind."));
                    sleepTime = 0;
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                toConsole.add(new Message("source=Sim Loop\nseverity=error\n"
                        + "message=Java Error: " + e.getStackTrace()));
            }


        }
    }
}
