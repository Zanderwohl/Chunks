package com.zanderwohl.chunks.Server;

/* Goodness this is disgusting. Is the enterprise coding getting to me? */
import com.zanderwohl.chunks.Client.ClientIdentity;
import com.zanderwohl.chunks.Console.*;
import com.zanderwohl.chunks.Delta.*;
import com.zanderwohl.chunks.FileConstants;
import com.zanderwohl.chunks.Logging.Log;
import com.zanderwohl.chunks.World.Coord;
import com.zanderwohl.chunks.World.Volume;
import com.zanderwohl.chunks.World.World;
import com.zanderwohl.chunks.World.WorldManager;
import com.zanderwohl.console.Message;
import com.zanderwohl.util.ExceptionUtils;
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

    // Managers that simloop delegates actions to.
    private final CommandManager commandManager;
    private final WorldManager worldManager;

    // The two queues of messages to and from the console.
    private final ArrayBlockingQueue<Message> toConsole;
    private final ArrayBlockingQueue<Message> fromConsole;

    /**
     * Updates to send to clients.
     */
    protected final ArrayBlockingQueue<Delta> clientUpdates;

    private ServerSocket serverSocket;
    private List<Thread> clients;
    private ConcurrentHashMap<String,ClientHandler> clientsById;
    private ClientAccepter clientAccepter;

    /**
     * The chat log.
     */
    protected LinkedList<Chat> chats;

    private SimLogDetail logSettings;
    private String logPath;

    /**
     * The constructor for a new simloop. Currently also acts as its initializer.
     * TODO: Fix this function's documentation. Wait, what's wrong with it?
     * @param toConsole The queue of messages to send to the console.
     * @param fromConsole The queue of messages from the console to be consumed.
     * @param port The port on which the server should run.
     * @throws IOException When the server cannot bind to the port.
     */
    public SimLoop(ArrayBlockingQueue<Message> toConsole, ArrayBlockingQueue<Message> fromConsole, int port) throws IOException, CommandSet.WrongArgumentsObjectException {
        // Set up the basics.
        this.toConsole = toConsole;
        this.fromConsole = fromConsole;
        this.serverSocket = new ServerSocket(port);

        // Set up lists for things to keep track of in sim.
        chats = new LinkedList<>();
        clientUpdates = new ArrayBlockingQueue<>(StartupSettings.CLIENT_UPDATES_QUEUE_SIZE);

        // We need to manage our worlds!
        worldManager = new WorldManager(toConsole, StartupSettings.DEFAULT_WORLD_NAME);

        // Set up the commands this simloop recognizes and can execute.
        SimLoop thisSimLoop = this;
        DefaultCommandsObjects commandObjects = new DefaultCommandsObjects(worldManager, thisSimLoop);
        commandManager = new CommandManager(toConsole, fromConsole, commandObjects);

        // Set up what we need to connect to and manage clients.
        clients = Collections.synchronizedList(new ArrayList<>());
        clientsById = new ConcurrentHashMap<>();
        clientAccepter = new ClientAccepter(serverSocket, clients, clientsById, toConsole, clientUpdates);

        // Set up logging
        logSettings = new SimLogDetail();
        logPath = FileConstants.logFolder +  "/" + this.startInstant + "." + FileConstants.logExtension;
    }

    /**
     * Close the server, gracefully as possible given the circumstances.
     * Can be used for error closes or non-error closes.
     * Tell all clients about the reason for server close.
     * @param quit The server close object that contains a quit reason.
     */
    public void closeServer(ServerClose quit){
        toConsole.add(new Message("source=Sim Loop\nmessage=Server closing: " + quit.closeMessage));
        sendToAllClients(quit);
        PrintWriter log = Log.openLog(logPath, toConsole);
        Log.append(log, "Server closed.");
        Log.closeLog(log);
        running = false;

        worldManager.saveAllWorlds();
    }

    /**
     * Gets the list of clients currently connected to the server... please don't use this?
     * @return The list of currently-connected clients.
     */
    public ArrayList<String> getClients(){
        ArrayList<String> clientList = new ArrayList<>();
        for(String identity: clientsById.keySet()){
            clientList.add(clientsById.get(identity).identity.getUsername());
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

        /* //UNCOMMENT THIS TO CAUSE HORRIBLE SERVER LAG. haha
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

    /**
     * Go through the queue of pending client updates and process all updates.
     */
    private void processAllClientUpdates(){
        PrintWriter log = Log.openLog(logPath, toConsole);

        while(!clientUpdates.isEmpty()){
            processOneClientUpdate(log);
        }

        Log.closeLog(log);
    }


    /**
     * Take one client update delta off the queue and process it, making appropriate client changes.
     * @param log The currently-open log.
     */
    private void processOneClientUpdate(PrintWriter log){
        Delta d = clientUpdates.remove();
        //TODO: a failed or bad request should kick the client.

        if(d instanceof Chat){
            Chat c = (Chat) d;
            processChat(log, c);
        }
        if(d instanceof PPos){
            PPos pos = (PPos) d;
            processPPos(log, pos);
        }
        if(d instanceof WorldRequest){
            WorldRequest wr = (WorldRequest) d;
            processWorldRequest(log, wr);
        }
        if(d instanceof VolumeRequest){
            VolumeRequest vr = (VolumeRequest) d;
            processVolumeRequest(log, vr);
        }
        if(d instanceof StartingVolumesRequest){
            StartingVolumesRequest svr = (StartingVolumesRequest) d;
            processStartingVolumesRequest(log, svr);
        }
        if(d instanceof Disconnect){
            Disconnect disconnect = (Disconnect) d;
            ClientHandler client = clientsById.get(disconnect.token);
            client.disconnect("User-initiated.");
        }
        //TODO: Add all the other types of deltas.
        // Note that the "action function" for each type of delta should log
        // according to its log detail level in logSettings.
    }

    /**
     * Process the position a player reports they are at.
     * In the future, this may correct the player.
     * @param log The currently-open log.
     * @param pos The reported position.
     */
    private void processPPos(PrintWriter log, PPos pos) {
        System.out.println(pos.getFrom().getDisplayName() + "\t" + pos.x + "\t" + pos.y + "\t" + pos.z);
        Log.append(log, pos.toString(), logSettings.PPoses);
    }

    /**
     * Process a chat request and add it
     * @param log The currently-open log.
     * @param c The new chat.
     */
    private void processChat(PrintWriter log, Chat c) {
        chats.add(c);
        Log.append(log, c.toString(), logSettings.chats);
    }

    /**
     * Process a request for metadata about a world and send that data to the client.
     * @param log The currently-open log.
     * @param wr The request.
     */
    private void processWorldRequest(PrintWriter log, WorldRequest wr){
        World worldToSend;
        Log.append(log, wr.toString(), logSettings.worldRequests);
        if(wr.requestedWorld == null){
            worldToSend = worldManager.getDefaultWorld();
        } else {
            worldToSend = worldManager.getWorld(wr.requestedWorld);
            if(worldToSend == null){
                return;
            }
        }
        sendToClient(wr.getFrom(), worldToSend);
    }

    /**
     * Process a request for the volumes around a new player's starting position, and sends appropriate volumes
     * to client.
     * @param log The currently-open log.
     * @param svr The request.
     */
    private void processStartingVolumesRequest(PrintWriter log, StartingVolumesRequest svr){
        Log.append(log, svr.toString(), logSettings.volumeRequests);
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

    /**
     * Process a request for a volume, and send requested volumes to that client if appropriate.
     * @param log The currently-open log.
     * @param vr The request.
     */
    private void processVolumeRequest(PrintWriter log, VolumeRequest vr){
        Log.append(log, vr.toString(), logSettings.volumeRequests);
        Coord volumeLocation = new Coord(vr.x, vr.y, vr.z, Coord.Scale.VOLUME);
        //TODO: Can currently only send Volumes from default world, since server doesn't yet keep track of which world the player is in.
        Volume volumeToSend = worldManager.getDefaultWorld().getVolume(volumeLocation, true);
        sendToClient(vr.getFrom(), volumeToSend);
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
        ClientHandler c = clientsById.get(client.getToken());
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
        for(ClientHandler clientHandler: clientsById.values()){
            ClientIdentity ci = clientHandler.identity;
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
        ClientHandler client = clientsById.get(user.getToken());
        if(client == null){
            return false;
        }
        //TODO: Save user state.
        client.disconnect(reason);
        clients.remove(client);
        clientsById.remove(user.getToken());
        toConsole.add(new Message("source=Sim Loop\nseverity=normal\nmessage="
                + "User " + client.identity.getDisplayName() + " has disconnected!"));
        return true;
    }

    /**
     * Remove all clients that have disconnected, to allow for more clients to be added.
     */
    private void pruneDeadClients(){
        Iterator<Map.Entry<String, ClientHandler>> i = clientsById.entrySet().iterator();
        while(i.hasNext()) {
            Map.Entry<String, ClientHandler> entry = i.next();
            ClientHandler client = entry.getValue();
            if(!client.running) {
                ClientIdentity identity = client.identity;
                disconnectUser(identity,null);
            }
        }
    }

    /**
     * Add a chat item to be sent out to all players. And the console.
     * @param c The chat.
     */
    public void addChat(Chat c){
        chats.add(c);
    }

    /**
     * Enqueue a client update. Will be processed when the simloop has available resources.
     * @param d The delta update to enqueue.
     */
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
                delta = elapsedTime / Sync.SIM_NS;

                lastFPSTime += elapsedTime;
                if (lastFPSTime >= Sync.ONE_BILLION) {
                    lastFPSTime = 0;
                }

                processAllClientUpdates();
                pruneDeadClients(); //TODO: Only do this once per second, or maybe less.
                update(delta);
                updateClients();

                Sync.sync(loopStartTime, lastLoopStartTime, toConsole);
            }
        } catch (Exception e){
            toConsole.add(new Message("severity=critical\nsource=Sim Loop\nmessage="
                    + "Server has encountered an unrecoverable error. Stopping."));
            toConsole.add(new Message("severity=critical\nsource=Sim Loop\nmessage="
                    + ExceptionUtils.boxMessage(e) + " " + ExceptionUtils.errorSource(e)));
            e.printStackTrace(); //TODO: Remove once message details are implemented.
        }

        toConsole.add(new Message("source=Sim Loop\nmessage=Server closed."));
    }
}
