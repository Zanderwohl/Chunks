package com.zanderwohl.chunks.Server;

import com.zanderwohl.chunks.Client.ClientIdentity;
import com.zanderwohl.chunks.Console.CommandManager;
import com.zanderwohl.chunks.Delta.*;
import com.zanderwohl.chunks.FileConstants;
import com.zanderwohl.chunks.Logging.Log;
import com.zanderwohl.chunks.StringConstants;
import com.zanderwohl.chunks.World.Coord;
import com.zanderwohl.chunks.World.Volume;
import com.zanderwohl.chunks.World.World;
import com.zanderwohl.chunks.World.WorldManager;
import com.zanderwohl.console.Message;
import com.zanderwohl.util.Sync;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The thread that simulates the logic of the game world.
 */
public class SimLoop implements Runnable {

    private boolean running;
    private String startInstant;

    private final double ONE_BILLION = 1000000000.0;
    public final double SIM_FPS = 20.0;
    private final double SIM_NS = ONE_BILLION / SIM_FPS;

    private final CommandManager commandManager;
    private final WorldManager worldManager;

    private final ArrayBlockingQueue<Message> toConsole;
    private final ArrayBlockingQueue<Message> fromConsole;

    protected final ArrayBlockingQueue<Delta> clientUpdates;

    private ServerSocket serverSocket;
    private List<Thread> clients;
    private ConcurrentHashMap<ClientIdentity,ClientHandler> clientsById;
    private ClientAccepter clientAccepter;

    protected LinkedList<Chat> chats;

    private SimLogDetail logSettings;
    private String logPath;

    /**
     * Only constructor?
     * TODO: Fix this function's documentation. Wait, what's wrong with it?
     * @param toConsole The queue of messages to send to the console.
     * @param fromConsole The queue of messages from the console to be consumed.
     * @param port The port on which the server should run.
     * @throws IOException When the server cannot bind to the port.
     */
    public SimLoop(ArrayBlockingQueue<Message> toConsole, ArrayBlockingQueue<Message> fromConsole, int port) throws IOException {
        this.toConsole = toConsole;
        this.fromConsole = fromConsole;
        this.serverSocket = new ServerSocket(port);

        clientUpdates = new ArrayBlockingQueue<>(500); //TODO: Make this a setting of some kind.

        worldManager = new WorldManager(toConsole, StringConstants.world);
        commandManager = new CommandManager(toConsole, fromConsole, worldManager, this);
        clients = Collections.synchronizedList(new ArrayList<>());
        clientsById = new ConcurrentHashMap<>();
        clientAccepter = new ClientAccepter(serverSocket, clients, clientsById, toConsole, clientUpdates);

        chats = new LinkedList<>();

        logSettings = new SimLogDetail();
        logPath = FileConstants.logFolder +  "/" + this.startInstant + "." + FileConstants.logExtension;
    }

    public void closeServer(ServerClose quit){
        toConsole.add(new Message("source=Sim Loop\nmessage=Server closing: " + quit.closeMessage));
        sendToAllClients(quit);
        PrintWriter log = Log.openLog(logPath, toConsole);
        Log.append(log, "Server closed.");
        Log.closeLog(log);
        running = false;

        worldManager.saveAllWorlds();
    }

    public ArrayList<ClientIdentity> getClients(){
        ArrayList<ClientIdentity> clientList = new ArrayList<>();
        for(ClientIdentity identity: clientsById.keySet()){
            clientList.add(identity);
        }
        return clientList;
    }

    /**
     * Update the simulation.
     * @param deltaT The difference in time from this to the last update.
     */
    private void update(double deltaT) {
        commandManager.processCommands();
        commandManager.doCommands();

        /* //UNCOMMENT THIS TO CAUSE HORRIBLE SERVER LAG.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e){
            System.err.println("no");
        }*/
        //System.out.println(deltaT);
    }

    /**
     * An external way to log something with the server loop. VERY expensive, don't use often.
     * @param event The string to write to th elog.
     * @return Whether it was successful or not.
     */
    public boolean logEvent(String event){
        PrintWriter log = Log.openLog(logPath, toConsole);
        boolean success = Log.append(log, event, true);
        Log.closeLog(log);
        return success;
    }

    private void processClientUpdates(){
        PrintWriter log = Log.openLog(logPath, toConsole);

        while(!clientUpdates.isEmpty()){
            Delta d = clientUpdates.remove();

            if(d instanceof Chat){
                chats.add((Chat) d);
                Log.append(log, d.toString(), logSettings.chats);
            }
            if(d instanceof PPos){
                PPos pos = (PPos) d;
                System.out.println(pos.getFrom().getDisplayName() + "\t" + pos.x + "\t" + pos.y + "\t" + pos.z);
                Log.append(log, d.toString(), logSettings.PPoses);
            }
            if(d instanceof WorldRequest){
                WorldRequest wr = (WorldRequest) d;
                World worldToSend;
                Log.append(log, d.toString(), logSettings.worldRequests);
                if(wr.requestedWorld == null){
                    worldToSend = worldManager.getDefaultWorld();
                } else {
                    worldToSend = worldManager.getWorld(wr.requestedWorld);
                    if(worldToSend == null){
                        return;
                    }
                }
                sendToClient(d.getFrom(), worldToSend);
            }
            if(d instanceof VolumeRequest){
                VolumeRequest vr = (VolumeRequest) d;
                Log.append(log, d.toString(), logSettings.volumeRequests);
                Coord volumeLocation = new Coord(vr.x, vr.y, vr.z, Coord.Scale.VOLUME);
                //TODO: Can currently only send Volumes from default world, since server doesn't keep track of which world the player is in.
                Volume volumeToSend = worldManager.getDefaultWorld().getVolume(volumeLocation, true);
                sendToClient(vr.getFrom(), volumeToSend);
            }
            if(d instanceof StartingVolumesRequest){
                StartingVolumesRequest svr = (StartingVolumesRequest) d;
                Log.append(log, d.toString(), logSettings.volumeRequests);
                //TODO: Send only nearby Volumes, not all of them. Also, select which world the player is in.
                World w = worldManager.getDefaultWorld();
                for(int x = 0; x < World.x_length; x++){ //TODO: this assumes the world is finite
                    for(int y = 0; y < World.y_length; y++){
                        for(int z = 0; z < World.z_length; z++){
                            Volume v = w.getVolume(new Coord(x, y, z, Coord.Scale.VOLUME), true);
                            if(v != null){
                                sendToClient(svr.getFrom(), v);
                            }
                        }
                    }
                }

            }
        }
        Log.closeLog(log);
    }

    /**
     * Send updates to all clients over the network.
     */
    private void updateClients(){
        while(!chats.isEmpty()){
            Chat c = chats.remove();
            toConsole.add(new Message("source=" + c.getFrom().getDisplayName() + "\nmessage=" + c.toString()));
            sendToAllClients(c);
        }
    }

    /**
     * Sends an update to every client currently connected.
     * @param update The update to send.
     */
    private void sendToAllClients(Delta update){
        for(ClientHandler client: clientsById.values()){
            client.serverUpdates.add(update);
        }
    }

    /**
     * Send a delta to a client specified by a ClientIdentity.
     * @param client The client to send to.
     * @param update The update to send.
     */
    private void sendToClient(ClientIdentity client, Delta update){
        ClientHandler c = clientsById.get(client);
        c.serverUpdates.add(update);

    }

    /**
     * Send update information to a single client.
     * Usually called in a loop to update all clients in a row.
     * @param update The index of the client to update.
     */
    private void updateClient(Delta update){

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
        if(user == null){
            return false;
        }
        ClientHandler client = clientsById.get(user);
        if(client == null){
            return false;
        }
        //TODO: Save user state.
        client.disconnect(reason);
        clients.remove(client);
        clientsById.remove(user);
        toConsole.add(new Message("source=Sim Loop\nmessage="
                + "User " + client.identity.getDisplayName() + " has disconnected!"));
        return true;
    }

    /**
     * Remove all clients that have disconnected, to allow for more clients to be added.
     */
    private void pruneDeadClients(){
        Iterator<Map.Entry<ClientIdentity, ClientHandler>> i = clientsById.entrySet().iterator();
        while(i.hasNext()) {
            Map.Entry<ClientIdentity, ClientHandler> entry = i.next();
            ClientHandler client = entry.getValue();
            if(!client.running) {
                ClientIdentity identity = entry.getKey();
                disconnectUser(identity,null);
            }
        }
    }

    public void addChat(Chat c){
        chats.add(c);
    }

    public void addClientUpdate(Delta d){
        clientUpdates.add(d);
    }

    /**
     * Run the world. Simulate physics and deal with client updates. Update clients.
     */
    @Override
    public void run() {
        running = true;

        long lastLoopStartTime = System.nanoTime();
        double delta;
        long lastFPSTime = 0;

        Thread clientAccepterThread = new Thread(clientAccepter);
        clientAccepterThread.start();

        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date startDate = new Date();
        this.startInstant = dateFormat.format(startDate);


        try {
            while (running) {
                long loopStartTime = System.nanoTime();
                long elapsedTime = loopStartTime - lastLoopStartTime;
                lastLoopStartTime = loopStartTime;
                delta = elapsedTime / SIM_NS;

                lastFPSTime += elapsedTime;
                if (lastFPSTime >= ONE_BILLION) {
                    lastFPSTime = 0;
                }

                processClientUpdates();
                pruneDeadClients(); //TODO: Only do this once per second, or maybe less.
                update(delta);
                updateClients();

                Sync.sync(loopStartTime, lastLoopStartTime, SIM_NS, toConsole);
            }
        } catch (Exception e){
            toConsole.add(new Message("severity=critical\nsource=Sim Loop\nmessage="
                    + "Server has encountered an unrecoverable error. Stopping."));
            toConsole.add(new Message("severity=critical\nsource=Sim Loop\nmessage="
                    + e.getStackTrace().toString()));
            e.printStackTrace(); //TODO: Remove once message details are implemented.
        }

        toConsole.add(new Message("source=Sim Loop\nmessage=Server closed."));
    }
}
