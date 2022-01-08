package com.zanderwohl.chunks.Console;

import com.zanderwohl.console.Message;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class CommandManager {

    private final ArrayBlockingQueue<Message> commandQueue;
    private final ArrayBlockingQueue<Message> toConsole;

    private final LinkedList<UserCommand> userCommandQueue = new LinkedList<>();

    private final HashMap<String, Command> commands;
    private final ArrayList<Command> commandsList;

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

    /**
     * Add a command to this CommandManager, which means this command manager can execute it.
     * @param command The command to add.
     * @param commandName The name the command will be known by. Uses this alias instead of the name in the Command obj.
     * @return True if the command was added, false if there is already a command by this name and it was not added.
     */
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

    /**
     * Add a command to this CommandManager, which means this command manager can execute it.
     * This differs from the overload that takes in a command name in that it uses the name the Command object contains.
     * @param command The command to add.
     * @return True if the command was added, false if there is already a command by this name and it was not added.
     */
    public boolean addCommand(Command command){
        return addCommand(command, command.getName());
    }

    /**
     * Takes in messages from the command queue, constructs them into a Command object.
     * It then places them on the internal command queue for later execution.
     */
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

    /**
     * Loops through all the commands in the queue and executes them.
     */
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
