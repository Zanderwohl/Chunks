package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Delta.*;
import com.zanderwohl.chunks.Gamelogic.IGameLogic;
import com.zanderwohl.chunks.World.Volume;
import com.zanderwohl.chunks.World.World;
import com.zanderwohl.console.Message;
import com.zanderwohl.util.Sync;
import java.util.concurrent.ArrayBlockingQueue;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * The loop run on the client to handle input, graphics, and a few other things.
 */
public class ClientLoop {

    public final double RENDER_FPS = 60.0;

    private Window window;
    private long windowId;

    private volatile boolean running;

    private final ArrayBlockingQueue<Delta> serverUpdates;
    private final ArrayBlockingQueue<Delta> clientUpdates;

    private ArrayBlockingQueue<Message> toConsole;

    private ClientIdentity identity;
    private PPos position;
    private PPos prevPosition;

    private World currentWorld;

    private static boolean debug = false;

    private final IGameLogic gameLogic;

    public ClientLoop(ArrayBlockingQueue<Delta> clientUpdates, ArrayBlockingQueue<Delta> serverUpdates,
                      ClientIdentity clientIdentity,
                      ArrayBlockingQueue<Message> toConsole,
                      IGameLogic gameLogic){
        this.clientUpdates = clientUpdates;
        this.serverUpdates = serverUpdates;
        this.identity = clientIdentity;

        this.toConsole = toConsole;

        this.gameLogic = gameLogic;

        position = new PPos(0.0, 0.0, 0.0, 0.0, 0.0, identity.getDisplayName());

        running = true;
    }

    private void informServer(){
        //Uh? Not sure what this was going to do.
    }

    private void acceptUpdates(){
        while(!serverUpdates.isEmpty()){
            Delta update = serverUpdates.poll();
            applyUpdate(update);
        }
    }

    private void applyUpdate(Delta update){
        if(update instanceof Hello){
            Hello h = (Hello) update;
            if(window != null){
                window.setTitle(h.NAME + ": " + h.MOTD);
            }
        }
        if(update instanceof Chat){
            Chat c = (Chat) update;
            System.out.println(c.toString());
            return;
        }
        if(update instanceof Kick){
            Kick k = (Kick) update;
            toConsole.add(new Message("source=Client Window\nmessage=" + k.getReason()));
        }
        if(update instanceof PPos){
            PPos ppos = (PPos) update;
            if(ppos.player.equals(identity.getDisplayName())){
                position = ppos;
                prevPosition = new PPos(0.0, 0.0, 0.0, 0.0, 0.0, identity.getDisplayName()); // ???
            } else {
                //TODO: Update another player's position.
            }
        }
        if(update instanceof World){
            World w = (World) update;
            currentWorld = w;
            if(debug) {
                System.out.println("World changed to " + w.getName());
            }
        }
        if(update instanceof Volume){
            Volume v = (Volume) update;
            currentWorld.setVolume(v);
            if(debug) {
                System.out.println("Loaded volume at " + v.getLocation());
            }
        }
        if(update instanceof ServerClose){
            toConsole.add(new Message("source=Client Window\nmessage=Disconnected because the server has closed."));
        }
        if(update instanceof VolumeAge){
            //TODO: Compare age of volume we have against age of the server's volume.
        }
    }

    public void init() throws Exception{
        window = new Window(toConsole);
        windowId = window.init();

        if(gameLogic != null){
            gameLogic.init(window);
        }
    }

    private void handleInput(){
        gameLogic.input(window);
    }

    private void updateGameState(double deltaTime){
        gameLogic.update((float) deltaTime);
    }

    private void sendUpdatesToServer(){
        if(prevPosition != null && !prevPosition.equals(position)){
            clientUpdates.add(position);
            prevPosition = position;
        }

    }

    public void loop(){
        GL.createCapabilities();
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        clientUpdates.add(new Chat(identity, "Hello server!"));

        long lastLoopStartMoment = System.nanoTime();
        double deltaTime = 0.0;
        long lastFPSTime = 0;
        long steps = 0; // Uh?? what was this for?

        while(!window.shouldClose()){
            long loopStartMoment = System.nanoTime();
            long elapsedTime = loopStartMoment - lastLoopStartMoment;
            lastLoopStartMoment = loopStartMoment;
            steps += elapsedTime;
            deltaTime = elapsedTime / Sync.SIM_NS;

            lastFPSTime += elapsedTime;
            if(lastFPSTime >= Sync.ONE_BILLION){
                lastFPSTime = 0;
            }

            acceptUpdates();
            handleInput();
            updateGameState(deltaTime);
            sendUpdatesToServer();
            render();

            Sync.sync(loopStartMoment, lastLoopStartMoment, toConsole);
        }

        clientUpdates.add(new Disconnect(Disconnect.DisconnectReason.ClientClosed));
    }

    private void render(){
        gameLogic.render(window);
        window.update();
    }

    public void run(){
        try {
            init();
            loop();
            window.destroy();
        } catch(Exception e){
            toConsole.add(new Message("severity=CRITICAL\nsource=Client Loop\nmessage=Failed to initialize client. JVM details: " + e.getMessage()));
        }
        finally {
            window.free();
            gameLogic.cleanup();
        }

    }

    public void cleanup(){
        gameLogic.cleanup();
    }
}
