package com.zanderwohl.chunks.Console;

import com.zanderwohl.chunks.FileConstants;
import com.zanderwohl.console.Message;
import com.zanderwohl.util.Precedence;
import com.zanderwohl.util.FileLoader;
import com.zanderwohl.util.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.BiConsumer;

/**
 * Manages startup settings. Looks in different places for options, then falls back to a default.
 */
public class StartupSettings {
    private static CommandManager commandManager;
    private static ArrayList<Command> commands;
    private static HashMap<String, Command> commandMap;

    public static String DEFAULT_WORLD_NAME;
    public static int PORT;
    public static String CONFIG;
    public static int MAX_USERS;
    public static int CLIENT_UPDATES_QUEUE_SIZE;
    public static String SERVER_NAME;
    public static String MOTD;

    /**
     * Provide the objects needed to set up these commands.
     * @param c The CommandManager.
     * @param cm The list the commands will be added to. In this case, only the single ArrangeStartup command.
     * @param cs Dictionary of all the commands by their primary name.
     * @param objects Objects needed by commands.
     * @throws CommandSet.WrongArgumentsObjectException
     */
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

    /**
     * Add the different startup configuration items and descriptions.
     */
    public static void addCommands() {
        Command arrangeStartup = new Command("override", "Arrange the startup conditions.", new ArrangeStartup());
        arrangeStartup.addArgument("world", false, "The name of the world to start.");
        arrangeStartup.addArgument("port", false, "The port the server is available on.");
        arrangeStartup.addArgument("config", false, "The path to the config file.");
        arrangeStartup.addArgument("client_updates_queue_size", false, "The size of the client updates queue. Should be bigger for a bigger server, and smaller to use less resources.");
        arrangeStartup.addArgument("server_name", false, "The name of the server.");
        arrangeStartup.addArgument("motd", false, "The message of the day.");
        commandManager.addCommand(arrangeStartup);
    }

    /**
     * Sets startup settings based on a hierarchy of Command-Line-Arg > Config-File > default.
     */
    public static class ArrangeStartup implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {

        /**
         * Sets all the startup settings.
         * @param arguments Arguments, usually input from command line.
         * @param toConsole Stream of messages to console.
         */
        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole) {
            CONFIG = Precedence.lastNonNull(FileConstants.configFile, arguments.get("config"));
            FileLoader configFile = new FileLoader(CONFIG);
            HashMap<String, String> config = Properties.toMap(configFile.getFileSafe());

            DEFAULT_WORLD_NAME = Precedence.lastNonNull("Victoria", config.get("world"), arguments.get("world"));
            PORT = Precedence.lastInt("32112", config.get("port"), arguments.get("port"));
            MAX_USERS = Precedence.lastInt("10", config.get("max_users"), arguments.get("max_users"));
            SERVER_NAME = Precedence.lastNonNull("Generic Server", config.get("server_name"), arguments.get("server_name"));
            MOTD = Precedence.lastNonNull("A ZandyCraft Server", config.get("motd"), arguments.get("motd"));

            CLIENT_UPDATES_QUEUE_SIZE = Precedence.lastInt("500", config.get("client_updates_queue_size"), arguments.get("client_updates_queue_size"));

            // TODO: Create config file based on resources/default_config.properties if one doesn't already exist.
            // TODO: Get default configuration from resrouces/default_config.properties.
        }
    }
}
