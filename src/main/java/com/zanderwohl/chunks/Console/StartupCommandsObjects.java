package com.zanderwohl.chunks.Console;

import com.zanderwohl.console.Message;

import java.util.concurrent.ArrayBlockingQueue;

public class StartupCommandsObjects implements CommandManager.ICommandManagerArguments {

    public String commandLineArgs;
    private Class<?> commandType;

    public StartupCommandsObjects(String[] commandLineArgs){
        this.commandLineArgs = "override " + String.join(" ", commandLineArgs);
        this.commandType = StartupSettings.class;
    }

    public Class<?> getCommandType(){
        return commandType;
    }

    public ArrayBlockingQueue<Message> getCommandLineArgs(){
        ArrayBlockingQueue<Message> queue = new ArrayBlockingQueue<>(1);
        queue.add(new Message("message=" + commandLineArgs));
        return queue;
    }
}
