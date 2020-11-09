package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Delta.Chat;
import com.zanderwohl.chunks.Delta.Delta;

import javax.swing.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientWindow {


    private volatile boolean running;
    JFrame gameWindow;

    private ConcurrentLinkedQueue<Delta> serverUpdates;
    private ConcurrentLinkedQueue<Delta> clientUpdates;

    private ClientIdentity identity;

    public ClientWindow(ConcurrentLinkedQueue<Delta> clientUpdates, ConcurrentLinkedQueue<Delta> serverUpdates,
                        ClientIdentity clientIdentity){
        this.clientUpdates = clientUpdates;
        this.serverUpdates = serverUpdates;
        this.identity = clientIdentity;
        gameWindow = new JFrame();

        running = true;
    }

    private void informServer(){

    }

    private void acceptUpdates(){
        while(!serverUpdates.isEmpty()){
            Delta update = serverUpdates.remove();
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

        while(running){
            informServer();
            acceptUpdates();
        }
    }
}
