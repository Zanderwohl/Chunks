package com.zanderwohl.chunks.Console;

import com.zanderwohl.console.Message;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Helper objects for interpreting startup conditions commands.
 */
public class StartupCommandsObjects implements CommandManager.ICommandManagerArguments {

    /**
     * The startup arguments string, unparsed, usually provided via the command line.
     */
    public final String commandLineArgs;
    private Class<?> commandType;

    /**
     * Initialize the command line objects.
     * @param commandLineArgs Raw command-line arguments string. // TODO: Where is the documentation for these?
     */
    public StartupCommandsObjects(String[] commandLineArgs){
        this.commandLineArgs = "override " + String.join(" ", commandLineArgs);
        this.commandType = StartupSettings.class;
    }

    /**
     * Identifier for this class's name.
     * @return This class's symbol.
     */
    public Class<?> getCommandType(){
        return commandType;
    }

    /**
     * See return.
     * @return A queue containing only the command-line startup arguments.
     */
    public ArrayBlockingQueue<Message> getCommandLineArgs(){
        ArrayBlockingQueue<Message> queue = new ArrayBlockingQueue<>(1);
        queue.add(new Message("message=" + commandLineArgs));
        return queue;
    }
}
