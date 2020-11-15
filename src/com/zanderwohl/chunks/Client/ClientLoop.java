package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Delta.Chat;
import com.zanderwohl.chunks.Delta.Delta;
import com.zanderwohl.chunks.Delta.Kick;
import com.zanderwohl.chunks.Delta.PPos;
import com.zanderwohl.chunks.World.Volume;
import com.zanderwohl.chunks.World.World;
import com.zanderwohl.console.Message;

import javax.swing.*;
import java.util.concurrent.ArrayBlockingQueue;

public class ClientLoop {

    private final double ONE_BILLION = 1000000000.0;
    public final double SIM_FPS = 20.0;
    private final double SIM_NS = ONE_BILLION / SIM_FPS;

    private volatile boolean running;
    private JFrame gameWindow;

    private ArrayBlockingQueue<Delta> serverUpdates;
    private ArrayBlockingQueue<Delta> clientUpdates;

    private ArrayBlockingQueue<Message> toConsole;

    private ClientIdentity identity;
    private PPos position;
    private PPos prevPosition;

    private World currentWorld;

    public ClientLoop(ArrayBlockingQueue<Delta> clientUpdates, ArrayBlockingQueue<Delta> serverUpdates,
                      ClientIdentity clientIdentity,
                      ArrayBlockingQueue<Message> toConsole){
        this.clientUpdates = clientUpdates;
        this.serverUpdates = serverUpdates;
        this.identity = clientIdentity;
        gameWindow = new JFrame();

        this.toConsole = toConsole;
        position = new PPos(0.0, 0.0, 0.0, 0.0, 0.0, identity.getDisplayName());

        running = true;
    }

    private void informServer(){

    }

    private void acceptUpdates(){
        while(!serverUpdates.isEmpty()){
            Delta update = serverUpdates.poll();
            applyUpdate(update);
        }
    }

    private void applyUpdate(Delta update){
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
                prevPosition = new PPos(0.0, 0.0, 0.0, 0.0, 0.0, identity.getDisplayName());
            } else {
                //TODO: Update another player's position.
            }
        }
        if(update instanceof World){
            World w = (World) update;
            currentWorld = w;
            System.out.println("World changed to " + w.getName());
        }
        if(update instanceof Volume){
            Volume v = (Volume) update;
            currentWorld.setVolume(v);
            System.out.println("Loaded volume at " + v.getLocation());
        }
    }

    private void updatePosition(){
        //TODO: Get user input???
    }

    private void sendPosition(){
        if(prevPosition != null && !prevPosition.equals(position)){
            clientUpdates.add(position);
            prevPosition = position;
        }

    }

    public void start(){
        gameWindow.setVisible(true);

        clientUpdates.add(new Chat(identity, "Hello server!"));

        long lastNow = System.nanoTime();
        double delta = 0.0;
        long lastFPSTime = 0;

        while(running){
            long now = System.nanoTime();
            long updateLength = now - lastNow;
            lastNow = now;
            delta = updateLength / SIM_NS;

            lastFPSTime += updateLength;
            if(lastFPSTime >= ONE_BILLION){
                lastFPSTime = 0;
            }

            informServer();
            acceptUpdates();
            updatePosition();
            sendPosition();

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
