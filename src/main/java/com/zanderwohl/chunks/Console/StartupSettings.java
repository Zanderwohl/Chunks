package com.zanderwohl.chunks.Console;

import com.zanderwohl.console.Message;
import com.zanderwohl.util.Precedence;
import com.zanderwohl.util.FileLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.BiConsumer;

public class StartupSettings {
    private static CommandManager commandManager;
    private static ArrayList<Command> commands;
    private static HashMap<String, Command> commandMap;

    public static String DEFAULT_WORLD_NAME;
    public static int PORT;
    public static String CONFIG;
    public static int MAX_USERS;

    public static void giveObjects(CommandManager c, ArrayList<Command> cm,
                                   HashMap<String, Command> cs, CommandManager.ICommandManagerArguments objects) throws CommandSet.WrongArgumentsObjectException {
        commandManager = c;
        commands = cm;
        commandMap = cs;

        if(objects instanceof StartupCommandsObjects){
            StartupCommandsObjects sco = (StartupCommandsObjects) objects;
        } else {
            throw new CommandSet.WrongArgumentsObjectException("DefaultCommands needs a DefaultCommandsObjects ICommandManagerArguments, not a " + objects.getClass());
        }
    }

    public static void addCommands() {
        Command arrangeStartup = new Command("override", "Arrange the startup conditions.", new ArrangeStartup());
        arrangeStartup.addArgument("world", false, "The name of the world to start.");
        arrangeStartup.addArgument("port", false, "The port the server is available on.");
        arrangeStartup.addArgument("config", false, "The path to the config file.");
        commandManager.addCommand(arrangeStartup);
    }

    public static class ArrangeStartup implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {
        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole) {
            CONFIG = Precedence.lastNonNull("server.config", arguments.get("config"));
            FileLoader configFile = new FileLoader(CONFIG);
            String configString = configFile.getFileSafe();

            DEFAULT_WORLD_NAME = Precedence.lastNonNull("Victoria", arguments.get("world"));
            PORT = Precedence.lastInt("32112", arguments.get("port"));
            MAX_USERS = Precedence.lastInt("10", arguments.get("max_users"));
        }
    }
}
