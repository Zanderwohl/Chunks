package com.zanderwohl.chunks;

import com.zanderwohl.chunks.Client.Client;
import com.zanderwohl.chunks.Console.Console;
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
     * @param args No command-line arguments.
     * @throws IOException When the server cannot bind the the port.
     */
    public static void main(String[] args) throws IOException {
        int port = 32112;
        prepareEnvironment();

        //creates queues to and from console
        ArrayBlockingQueue<Message> toConsole = new ArrayBlockingQueue<>(50);
        ArrayBlockingQueue<Message> fromConsole = new ArrayBlockingQueue<>(50);

        Client singleplayerClient = new Client("localhost", port, toConsole);
        Thread clientThread = new Thread(singleplayerClient);

        //Start the game's console - not the user-facing console, but the part of this program that receives and sends
        //to SuperConsole - on its own thread.
        Console consoleConnector = new Console(toConsole, fromConsole);
        Thread consoleConnectorThread = new Thread(consoleConnector);
        consoleConnectorThread.start();

        //Start a SuperConsole window
        SuperConsole console = new SuperConsole();
        Thread consoleThread = new Thread(console);
        consoleThread.start();

        //Start the simulation on a thread
        SimLoop simLoop = new SimLoop(toConsole, fromConsole, port);
        Thread simThread = new Thread(simLoop);
        simThread.start();

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
    }


}
