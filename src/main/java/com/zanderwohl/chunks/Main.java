package com.zanderwohl.chunks;

import com.zanderwohl.chunks.Client.Client;
import com.zanderwohl.chunks.Console.*;
import com.zanderwohl.chunks.Gamelogic.BadGame;
import com.zanderwohl.chunks.Server.SimLoop;
import com.zanderwohl.console.Message;
import com.zanderwohl.console.SuperConsole;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Yikes, what a mess of a file. To be replaced once SuperConsole is up and running.
 */
public class Main {

    /**
     * Main method.
     * Initializes the Message queues to pass messages to and from the console.
     * Initializes a client and a server that are connected to each other.
     * @param args No command-line arguments.
     * @throws IOException When the server cannot bind the the port.
     */
    public static void main(String[] args) throws IOException {
        prepareEnvironment();

        // creates queues to and from console
        ArrayBlockingQueue<Message> toConsole = new ArrayBlockingQueue<>(50);
        ArrayBlockingQueue<Message> fromConsole = new ArrayBlockingQueue<>(50);

        // Deal with startup commands
        StartupCommandsObjects sco = new StartupCommandsObjects(args);
        CommandManager startupCommandManager = new CommandManager(toConsole, sco.getCommandLineArgs(), sco);
        startupCommandManager.processCommands();
        startupCommandManager.doCommands();

        // Start one client.
        Client singleplayerClient = new Client("localhost", StartupSettings.PORT, toConsole, new BadGame());
        Thread clientThread = new Thread(singleplayerClient);

        // Start the game's console interface - not the user-facing console, but the part of this program that receives
        // and sends to SuperConsole - on its own thread.
        ConsoleBroker consoleBroker = new ConsoleBroker(toConsole, fromConsole);
        Thread consoleConnectorThread = new Thread(consoleBroker);
        consoleConnectorThread.start();

        // Start a SuperConsole window
        SuperConsole console = new SuperConsole();
        console.newConnection("Local Server","localhost",288);

        // Start the simulation on a thread
        try {
            SimLoop simLoop = new SimLoop(toConsole, fromConsole, StartupSettings.PORT);
            Thread simThread = new Thread(simLoop);
            simThread.start();
        } catch (CommandSet.WrongArgumentsObjectException e){
            return;
        }

        clientThread.start();

        //Start the user interface on a thread
    }

    /**
     * Prepares the environment for the program to run, saving/loading files without crashing.
     */
    public static void prepareEnvironment(){
        makeDirectories();
    }

    /**
     * Create the directories needed for saving files.
     */
    public static void makeDirectories(){
        new File(FileConstants.saveFolder + "/").mkdirs();
        new File(FileConstants.screenshotFolder + "/").mkdirs();
        new File(FileConstants.logFolder + "/").mkdirs();
        new File(FileConstants.clientLogFolder + "/").mkdirs();
        new File(FileConstants.atlasFolder + "/").mkdirs();
    }


}
