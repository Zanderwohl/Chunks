package com.zanderwohl.chunks.Console;

import com.zanderwohl.chunks.World.WorldManager;
import com.zanderwohl.console.Message;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandManager {

    private ConcurrentLinkedQueue<Message> fromConsole;
    private ConcurrentLinkedQueue<Message> toConsole;

    private LinkedList<UserCommand> userCommandQueue = new LinkedList<>();

    private WorldManager worldManager;

    private HashMap<String, Command> commands;
    private ArrayList<Command> commandsList;

    public CommandManager(ConcurrentLinkedQueue<Message> toConsole, ConcurrentLinkedQueue<Message> fromConsole,
                          WorldManager worldManager){
        this.fromConsole = fromConsole;
        this.toConsole = toConsole;
        this.worldManager = worldManager;
        this.commands = new HashMap<>();
        this.commandsList = new ArrayList<>();
        DefaultCommands.giveObjects(this, worldManager, commandsList, commands);
        DefaultCommands.addDefaultCommands();
    }

    public boolean addCommand(Command command, String commandName){
        if(commands.containsKey(commandName)){
            toConsole.add(new Message("source=Command Manager\nseverity=error\n"));
            return false;
        }
        commands.put(commandName, command);
        commandsList.add(command);
        if(!commandsList.contains(command)) {
            commandsList.sort(Comparator.comparing(Command::getName));
        }
        return true;
    }

    public boolean addCommand(Command command){
        return addCommand(command, command.getName());
    }

    public void processCommands(){
        while(!fromConsole.isEmpty()){
            Message command = fromConsole.remove();
            try {
                UserCommand c = new UserCommand(command.getAttribute("message"));
                userCommandQueue.add(c);
            } catch (UserCommand.OpenStringException e) {
                toConsole.add(new Message("message=" + e.getMessage() + "\nsource=" + "Command Manager" +
                        "\nseverity=warning"));
            }
        }
    }

    public void doCommands() {
        while (!userCommandQueue.isEmpty()) {
            UserCommand c = userCommandQueue.remove();

            String commandString = c.getCommand();

            Command selectedCommand = commands.get(commandString);
            if(selectedCommand == null){
                unrecognizedCommand(c, commandString);
            } else {
                selectedCommand.accept(c, toConsole);
            }
        }
    }

    private void unrecognizedCommand(UserCommand userCommand, String commandString){
        toConsole.add(new Message("message=The command \"" + commandString + "\" is not recognized.\n"
                + "source=Command Manager\nseverity=error"));
    }
}
