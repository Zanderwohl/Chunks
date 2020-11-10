package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Delta.Chat;
import com.zanderwohl.chunks.Delta.Delta;
import com.zanderwohl.console.Message;

import javax.swing.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientWindow {

    private final double ONE_BILLION = 1000000000.0;
    public final double SIM_FPS = 20.0;
    private final double SIM_NS = ONE_BILLION / SIM_FPS;

    private volatile boolean running;
    JFrame gameWindow;

    private ArrayBlockingQueue<Delta> serverUpdates;
    private ArrayBlockingQueue<Delta> clientUpdates;

    private ArrayBlockingQueue<Message> toConsole;

    private ClientIdentity identity;

    public ClientWindow(ArrayBlockingQueue<Delta> clientUpdates, ArrayBlockingQueue<Delta> serverUpdates,
                        ClientIdentity clientIdentity,
                        ArrayBlockingQueue<Message> toConsole){
        this.clientUpdates = clientUpdates;
        this.serverUpdates = serverUpdates;
        this.identity = clientIdentity;
        gameWindow = new JFrame();

        this.toConsole = toConsole;

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
