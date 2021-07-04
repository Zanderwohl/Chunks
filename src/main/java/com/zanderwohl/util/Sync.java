package com.zanderwohl.util;

import com.zanderwohl.console.Message;

import java.util.concurrent.ArrayBlockingQueue;

public class Sync {

    public static void sync(long now, long lastNow, double SIM_NS, ArrayBlockingQueue<Message> toConsole){
        long sleepTime = (long)(lastNow - now + SIM_NS) / 1000000;
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
