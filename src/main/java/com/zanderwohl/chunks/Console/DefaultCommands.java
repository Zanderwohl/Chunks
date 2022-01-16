package com.zanderwohl.chunks.Console;

import com.zanderwohl.chunks.Client.ClientIdentity;
import com.zanderwohl.chunks.Delta.Chat;
import com.zanderwohl.chunks.Delta.ServerClose;
import com.zanderwohl.chunks.Image.ImageWorld;
import com.zanderwohl.chunks.Server.SimLoop;
import com.zanderwohl.chunks.World.World;
import com.zanderwohl.chunks.World.WorldManager;
import com.zanderwohl.console.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.BiConsumer;

/**
 * All the default commands for the server while it is running.
 */
public class DefaultCommands {

    private static SimLoop simLoop;
    private static CommandManager commandManager;
    private static WorldManager worldManager;
    private static ArrayList<Command> commands;
    private static HashMap<String, Command> commandMap;

    /**
     * Pass the objects to DefaultCommands that it needs to set up the commands to execute properly.
     * @param c The CommandManager that will hold the commands and trigger them when necessary. Parent to this object.
     * @param cm This object appears to be useless and does nothing? // TODO: What does this object do?
     * @param cs Map of commands where name->Command object. Used by help text.
     * @param objects // TODO: What is this interface for/what does it provide?
     * @throws CommandSet.WrongArgumentsObjectException
     */
    public static void giveObjects(CommandManager c, ArrayList<Command> cm,
                                   HashMap<String, Command> cs, CommandManager.ICommandManagerArguments objects) throws CommandSet.WrongArgumentsObjectException {
        commandManager = c;
        commands = cm;
        commandMap = cs;

        if(objects instanceof DefaultCommandsObjects){
            DefaultCommandsObjects idco = (DefaultCommandsObjects) objects;
            worldManager = idco.wm;
            simLoop = idco.sl;
        } else {
            throw new CommandSet.WrongArgumentsObjectException("DefaultCommands needs a DefaultCommandsObjects ICommandManagerArguments, not a " + objects.getClass());
        }
    }

    /**
     * Add the default commands.
     * Write new default commands in this method.
     */
    public static void addCommands(){
        Command say = new Command("say", "Send a public message to everyone on the server.",
                new Say());
        say.addArgument("text",true, "The text string to send.");
        commandManager.addCommand(say);

        Command newWorld = new Command("new", "Generate a new world.", new NewWorld());
        newWorld.addArgument("name", true, "The name of the world.");
        newWorld.addArgument("generator", false, "The generator used for the world.");
        newWorld.addArgument("seed", false,
                "The same seed + generator combination will always result in the same world. Omission of this argument means world name is used as seed.");
        commandManager.addCommand(newWorld);

        Command list = new Command("list", "Lists all active worlds.", new ListWorlds());
        commandManager.addCommand(list);
        commandManager.addCommand(list, "worlds");

        Command save = new Command("save", "Saves a world.", new SaveWorld());
        save.addArgument("world", true, "The world to be saved.");
        commandManager.addCommand(save);

        Command saveAll = new Command("save-all", "Saves all currently active worlds.", new SaveAll());
        commandManager.addCommand(saveAll);
        commandManager.addCommand(saveAll, "saveall");

        Command killWorld = new Command("kill", "Kills a specified world without saving.",
                new KillWorld());
        killWorld.addArgument("world", true, "The world to be killed.");
        commandManager.addCommand(killWorld);

        Command topDownMap = new Command("image", "Creates a top-down map of a portion of the world.",
                new TopDownMap());
        topDownMap.addArgument("world", true, "The world to make an image of.");
        topDownMap.addArgument("size", true, "'big' for a block-image picture.");
        topDownMap.addArgument("scale", false, "The size of each block.");
        commandManager.addCommand(topDownMap);

        Command help = new Command("help", "Lists commands or gives help for a command.", new Help());
        help.addArgument("command", true, "A specific command to get help on.");
        commandManager.addCommand(help);

        Command kick = new Command("kick", "Forcefully disconnects a user from the server.",
                new Kick());
        kick.addArgument("user",true,"The user to kick.");
        kick.addArgument("reason", false, "Tells the user why they were kicked.");
        commandManager.addCommand(kick);

        Command online = new Command("online","Lists all online users.", new ListOnlineUsers());
        commandManager.addCommand(online);

        Command closeServer = new Command("quit", "Closes the server.", new CloseServer());
        closeServer.addArgument("reason", true, "Gives the users a reason why the server closed.");
        commandManager.addCommand(closeServer);
    }

    public static class Say implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>>{
        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole) {
            String text = arguments.get("text");
            if(text == null){
                toConsole.add(new Message("message=Input a message.\n" +
                        "source=Command Manager\nseverity=normal"));
            }
            simLoop.addClientUpdate(new Chat(arguments.get("text")));
        }
    }

    public static class NewWorld implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {
        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole){
            if(arguments.get("name") == null || arguments.get("name").equals("")){
                toConsole.add(new Message("source=Command Manager\nseverity=warning"
                        + "\nmessage=Could not create world without name."));
                return;
            }
            String seed;
            if(arguments.get("seed") == null){
                seed = arguments.get("name");
            } else {
                seed = arguments.get("seed");
            }
            //TODO: Add generator.
            worldManager.newWorld(arguments.get("name"), seed);
        }
    }

    public static class ListWorlds implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {
        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole){
            String[] worlds = worldManager.listWorldNames();
            String message = "Active worlds: " + Arrays.toString(worlds);
            toConsole.add(new Message("message=" + message + "\nsource=Command Manager"));
        }
    }

    public static class SaveWorld implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {
        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole){
            if(arguments.get("world") == null){
                toConsole.add(new Message("message=Specify a world to save.\nsource=Command Manager\n"
                        + "severity=warning"));
                return;
            }
            worldManager.saveWorld(arguments.get("world"));
        }
    }

    public static class SaveAll implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {
        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole){
            worldManager.saveAllWorlds();
        }
    }

    public static class KillWorld implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {
        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole){
             if (arguments.get("world") == null) {
                toConsole.add(new Message("message=Specify a world to kill.\nsource=Command Manager\n"
                        + "severity=warning"));
                return;
            }
            worldManager.killWorld(arguments.get("world"));
        }
    }

    public static class TopDownMap implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {
        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole){
            World w = worldManager.getWorld(arguments.get("world"));
            if(w == null){
                toConsole.add(new Message("message=The world \"" + arguments.get("world")
                        + "\" does not exist or is not loaded."
                        + "\nsource=Command Manager\nseverity=normal"
                ));
                return;
            }
            BufferedImage image;
            if("big".equalsIgnoreCase(arguments.get("size"))){
                int scale;
                try {
                    scale = Integer.parseInt(arguments.get("scale"));
                } catch (NumberFormatException e){
                    scale = 16;
                }
                image = ImageWorld.makeImage2(w, 0, 0, scale, 1920, 1080);
            } else {
                image = ImageWorld.makeImage(w);
            }
            ImageWorld.saveImage(image, w.getName());

            //TODO: Remove this code
            JFrame frame = new JFrame();
            frame.getContentPane().setLayout(new FlowLayout());
            frame.getContentPane().add(new JLabel(new ImageIcon(image)));
            frame.pack();
            frame.setVisible(true);
        }
    }

    public static class Help implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {
        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole){
            if(arguments.get("command") == null){
                for(Command c: commands){
                    toConsole.add(new Message("source=Command Manager\nmessage=" + c.toString()));
                }
            } else {
                Command c = commandMap.get(arguments.get("command"));
                String helpText = c.documentation();
                toConsole.add(new Message("source=Command Manager\nmessage=" + helpText));
            }
        }
    }

    public static class Kick implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {

        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole) {
            if(arguments.get("user") == null){
                toConsole.add(new Message("source=Command Manager\nseverity=warning\nmessage="
                + "Specify a user to kick."));
            } else {
                String userToKick = arguments.get("user");
                String reason = arguments.get("reason");
                if(reason == null){
                    reason = "No given reason.";
                }
                ClientIdentity client = simLoop.findClientByDisplayName(arguments.get("user"));
                boolean success = simLoop.disconnectUser(client, "Kicked: " + reason);
                if(success){
                    toConsole.add(new Message("source=Command Manager\nseverity=warning\nmessage="
                            + "Kicked user " + userToKick + "."));
                } else {
                    toConsole.add(new Message("source=Command Manager\nseverity=warning\nmessage="
                            + "User " + userToKick + " was not found."));
                }
                //TODO: Find and kick user.
            }
        }
    }

    public static class ListOnlineUsers implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {

        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole) {
            ArrayList<String> clients = simLoop.getClients();
            String clientList = clients.toString();
            toConsole.add(new Message("source=Command Manager\nmessage=" + clientList));
        }
    }

    public static class CloseServer implements BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> {

        @Override
        public void accept(HashMap<String, String> arguments, ArrayBlockingQueue<Message> toConsole){
            String reason = arguments.get("reason");
            ServerClose quit;
            if(reason == null){
                quit = new ServerClose();
            } else {
                quit = new ServerClose(reason);
            }
            simLoop.closeServer(quit);
        }
    }
}
