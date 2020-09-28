package com.zanderwohl.chunks.Console;

import com.zanderwohl.chunks.World.WorldManager;
import com.zanderwohl.console.Message;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandManager {

    ConcurrentLinkedQueue<Message> fromConsole;
    ConcurrentLinkedQueue<Message> toConsole;

    LinkedList<Command> commandQueue = new LinkedList<>();

    WorldManager worldManager;

    public CommandManager(ConcurrentLinkedQueue<Message> toConsole, ConcurrentLinkedQueue<Message> fromConsole,
                          WorldManager worldManager){
        this.fromConsole = fromConsole;
        this.toConsole = toConsole;
        this.worldManager = worldManager;
    }

    public void processCommands(){
        while(!fromConsole.isEmpty()){
            Message command = fromConsole.remove();
            try {
                Command c = new Command(command.getAttribute("message"));
                commandQueue.add(c);
                toConsole.add(new Message("message=" + c.toString()));

            } catch (Command.OpenStringException e) {
                toConsole.add(new Message("message=" + e.getMessage() + "\nsource=" + "Command Manager" +
                        "\nseverity=warning"));
            }
        }
    }

    public void doCommands(){
        while (!commandQueue.isEmpty()) {
            Command c = commandQueue.remove();
            String command = c.getCommand();

            switch (command){
                case "SAY":
                    toConsole.add(new Message("message=SERVER: " + c.getArgumentsString() + "\n" +
                            "source=Command Manager\nseverity=normal"));
                    break;
                case "NEW":
                    worldManager.newWorld(c.getArgument(0));
                    break;
                default:
                    toConsole.add(new Message("message=The command \"" + command + "\" is not recognized.\n" +
                            "source=Command Manager\nseverity=error"));
            }
        }
    }
}
