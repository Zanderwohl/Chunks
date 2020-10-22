package com.zanderwohl.chunks.Client;

import com.zanderwohl.console.Message;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Client implements Runnable {

    private int port;
    private final ClientIdentity identity;
    ConcurrentLinkedQueue<Message> toConsole;

    public Client(int port, ConcurrentLinkedQueue<Message> toConsole){
        this.port = port;
        this.toConsole = toConsole;
        identity = new ClientIdentity("rexpup");
    }

    @Override
    public void run() {
        toConsole.add(new Message("source=Client\nmessage=Client initialized."));
    }
}
