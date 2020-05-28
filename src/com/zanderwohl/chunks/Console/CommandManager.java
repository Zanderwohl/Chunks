package com.zanderwohl.chunks.Console;

import com.zanderwohl.console.Message;

import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandManager {

    ConcurrentLinkedQueue<Message> fromConsole;
    ConcurrentLinkedQueue<Message> toConsole;

    public CommandManager(ConcurrentLinkedQueue<Message> toConsole, ConcurrentLinkedQueue<Message> fromConsole){
        this.fromConsole = fromConsole;
        this.toConsole = toConsole;
    }

    public void processCommands(){
        while(!fromConsole.isEmpty()){
            Message command = fromConsole.remove();
            System.out.println(command.toString());
        }
    }
}
