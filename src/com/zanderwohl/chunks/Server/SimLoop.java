package com.zanderwohl.chunks.Server;

import com.zanderwohl.chunks.Console.CommandManager;
import com.zanderwohl.console.Message;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The thread that simulates the logic of the game world.
 */
public class SimLoop implements Runnable{

    private final double ONE_BILLION = 1000000000.0;
    public final double SIM_FPS = 20.0;
    private final double SIM_NS = ONE_BILLION / SIM_FPS;


    CommandManager commandManager;

    /**
     * Only constructor?
     * @param toConsole The queue of messages to send to the console.
     * @param fromConsole The queue of messages from the console to be consumed.
     */
    public SimLoop(ConcurrentLinkedQueue<Message> toConsole, ConcurrentLinkedQueue<Message> fromConsole){
        commandManager = new CommandManager(toConsole, fromConsole);
    }

    /**
     * Update the simulation.
     * @param delta The difference in time from this to the last update.
     */
    private void update(double delta){
        commandManager.processCommands();
        //System.out.println(delta);
    }

    @Override
    public void run() {
        long lastNow = System.nanoTime();
        double delta = 0.0;
        long lastFPSTime = 0;

        while(true){
            long now = System.nanoTime();
            long updateLength = now - lastNow;
            lastNow = now;
            delta = updateLength / SIM_NS;

            lastFPSTime += updateLength;
            if(lastFPSTime >= ONE_BILLION){
                lastFPSTime = 0;
            }

            update(delta);

            try {
                Thread.sleep((long)(lastNow - System.nanoTime() + SIM_NS) / 1000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }
}