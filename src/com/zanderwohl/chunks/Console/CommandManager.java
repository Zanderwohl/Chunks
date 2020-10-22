package com.zanderwohl.chunks.Console;

import com.zanderwohl.chunks.Image.ImageWorld;
import com.zanderwohl.chunks.World.World;
import com.zanderwohl.chunks.World.WorldManager;
import com.zanderwohl.console.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandManager {

    private ConcurrentLinkedQueue<Message> fromConsole;
    private ConcurrentLinkedQueue<Message> toConsole;

    private LinkedList<Command> commandQueue = new LinkedList<>();

    private WorldManager worldManager;

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
            } catch (Command.OpenStringException e) {
                toConsole.add(new Message("message=" + e.getMessage() + "\nsource=" + "Command Manager" +
                        "\nseverity=warning"));
            }
        }
    }

    public void doCommands(){
        while (!commandQueue.isEmpty()) {
            Command c = commandQueue.remove();
            String commandString = c.getCommand();

            switch (commandString){
                case "SAY":
                    say(c);
                    break;
                case "NEW":
                    newWorld(c);
                    break;
                case "LIST":
                case "WORLDS":
                    listActiveWorlds(c);
                    break;
                case "SAVE":
                    saveWorld(c);
                    break;
                case "SAVEALL":
                case "SAVE-ALL":
                    saveAllWorlds(c);
                    break;
                case "KILL":
                    killWorld(c);
                    break;
                case "HELP":
                    listCommands(c);
                    break;
                case "IMAGE":
                    imageWorld(c);
                    break;
                default:
                    unrecognizedCommand(c, commandString);
            }
        }
    }

    private void unrecognizedCommand(Command command, String commandString){
        toConsole.add(new Message("message=The command \"" + commandString + "\" is not recognized.\n"
                + "source=Command Manager\nseverity=error"));
    }

    private void say(Command command){
        toConsole.add(new Message("message=SERVER: " + command.getArgumentsString() + "\n" +
                "source=Command Manager\nseverity=normal"));
    }

    private void newWorld(Command command){
        worldManager.newWorld(command.getArgument(0), command.getArgument(1));
    }

    private void listActiveWorlds(Command command){
        String[] worlds = worldManager.listWorldNames();
        String message = "Active worlds: " + Arrays.toString(worlds);
        toConsole.add(new Message("message=" + message + "\nsource=Command Manager"));
    }

    private void saveWorld(Command command){
        if(command.getArgument(0) == null){
            toConsole.add(new Message("message=Specify a world to save.\nsource=Command Manager\n"
            + "severity=warning"));
            return;
        }
        worldManager.saveWorld(command.getArgument(0));
    }

    private void saveAllWorlds(Command command){
        //TODO: Save all game state, not just worlds
        worldManager.saveAllWorlds();
    }

    public void killWorld(Command command) {
        if (command.getArgument(0) == null) {
            toConsole.add(new Message("message=Specify a world to kill.\nsource=Command Manager\n"
                    + "severity=warning"));
            return;
        }
        worldManager.killWorld(command.getArgument(0));
    }

    public void listCommands(Command command){
        toConsole.add(new Message("message=say, new, list, worlds, save, saveall, kill, help, image"
                + "\nsource=Command Manager\nseverity=normal"
        ));
    }

    public void imageWorld(Command command){
        World w = worldManager.getWorld(command.getArgument(0));
        if(w == null){
            toConsole.add(new Message("message=The world \"" + command.getArgument(0)
                    + "\" does not exist or is not loaded."
                    + "\nsource=Command Manager\nseverity=normal"
            ));
            return;
        }
        BufferedImage image;
        if("big".equalsIgnoreCase(command.getArgument(1))){
            image = ImageWorld.makeImage2(w, 0, 0, 16, 1920, 1080);
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
