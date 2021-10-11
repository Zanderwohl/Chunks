package com.zanderwohl.util;

import com.zanderwohl.console.Message;

import java.util.concurrent.ArrayBlockingQueue;

public class Sync {

    public static final int ONE_MILLION = 1_000_000;
    public static final int ONE_BILLION = 1_000_000_000;

    // Controls how many "ticks" per second the simulation runs at.
    // Obviously limited to how fast the computer can do this.
    // The server will start complaining if it can't keep up.
    public static final double SIM_FPS = 20.0;
    public static final double SIM_NS = ONE_BILLION / SIM_FPS;

    public static void sync(long now, long lastNow, ArrayBlockingQueue<Message> toConsole){
        long sleepTime = (long)(lastNow - now + SIM_NS) / ONE_MILLION;
        long sleepEnd = now + sleepTime;
        if(sleepTime < 0){
            toConsole.add(new Message("source=Client Loop\nseverity=warning\n"
                    + "message=Client can't keep up with graphics and physics! " + (-sleepTime) + " ns behind."));
            sleepEnd = now;
        }
        while(System.nanoTime() < sleepEnd) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                toConsole.add(new Message("source=Client Loop\nseverity=error\n"
                        + "message=Java Error: " + e.getStackTrace().toString()));
            }
        }
    }
}
