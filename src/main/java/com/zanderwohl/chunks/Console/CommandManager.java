package com.zanderwohl.chunks.Console;

import com.zanderwohl.console.Message;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class CommandManager {

    private ArrayBlockingQueue<Message> commandQueue;
    private ArrayBlockingQueue<Message> toConsole;

    private LinkedList<UserCommand> userCommandQueue = new LinkedList<>();

    private HashMap<String, Command> commands;
    private ArrayList<Command> commandsList;

    public interface ICommandManagerArguments{
        Class<?> getCommandType();
    }

    public CommandManager(ArrayBlockingQueue<Message> toConsole, ArrayBlockingQueue<Message> commandQueue,
                          ICommandManagerArguments commandManagerArguments){
        this.commandQueue = commandQueue;
        this.toConsole = toConsole;
        this.commands = new HashMap<>();
        this.commandsList = new ArrayList<>();
        try {
            Class<?> commandType = commandManagerArguments.getCommandType();

            if(commandType == DefaultCommands.class) {
                DefaultCommands.giveObjects(this, commandsList, commands, commandManagerArguments);
                DefaultCommands.addCommands();
            }
            if(commandType == StartupSettings.class){
                StartupSettings.giveObjects(this, commandsList, commands, commandManagerArguments);
                StartupSettings.addCommands();
            }
        } catch(CommandSet.WrongArgumentsObjectException e) {
            toConsole.add(new Message("source=Sim Loop\nseverity=critical\nmessage=" + e.getMessage()));
        }
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
        while(!commandQueue.isEmpty()){
            Message command = commandQueue.remove();
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
