package com.zanderwohl.chunks;

import com.zanderwohl.chunks.Console.Console;
import com.zanderwohl.chunks.Server.SimLoop;

import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.zanderwohl.console.SuperConsole;
import com.zanderwohl.console.Message;

/**
 * Yikes, what a mess of a file. To be replaced once SuperConsole is up and running.
 */
public class Main {

    /**
     * Main method.
     * Initializes the Message queues to pass messages to and from the console.
     * @param args No command-line arguments.
     */
    public static void main(String[] args){
        prepareEnvironment();

        ConcurrentLinkedQueue<Message> toConsole = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Message> fromConsole = new ConcurrentLinkedQueue<>();

        //toConsole.add(new Message("message=Uh"));

        Console consoleConnector = new Console(toConsole, fromConsole);
        Thread consoleConnectorThread = new Thread(consoleConnector);
        consoleConnectorThread.start();

        SuperConsole console = new SuperConsole();
        Thread consoleThread = new Thread(console);
        consoleThread.start();

        Console.log("","Main", "NORMAL", "");

        SimLoop simLoop = new SimLoop(toConsole, fromConsole);
        Thread simThread = new Thread(simLoop);
        simThread.start();
    }

    /*
     * Command-line loop for generating worlds that is deprecated.
     * @param args none.
     */
    /*
    public static void main_old(String[] args) {
        prepareEnvironment();

        World w = new World("default");
        boolean quit = false;

        Scanner scan = new Scanner(System.in);
        do {
            System.out.print('>');
            String[] command = scan.nextLine().split(" ");

            switch(command.length){
                case 0:
                    System.out.println("Please enter a command.");
                    break;

                case 1:
                    if(command[0].equals("save")){
                        w.save(w.getName());
                    }
                    if(command[0].equals("image")){
                        BufferedImage image = ImageWorld.makeImage(w);
                        ImageWorld.saveImage(image, w.getName());
                    }
                    if(command[0].equals("quit")){
                        quit = true;
                    }
                    if(command[0].equals("init")){
                        w.initialize();
                    }
                    if(command[0].equals("prepare")){
                        w.prepare();
                    }
                    break;

                case 2:
                    if(command[0].equals("new")){
                        w = new World(command[1]);
                    }
                    if(command[0].equals("save")){
                        w.setName(command[1]);
                        w.save(command[1]);
                    }
                    if(command[0].equals("load")){

                    }
                    if(command[0].equals("test")){
                        w = new World(command[1]);
                        w.prepare();
                        w.addDomain("color");
                        w.initialize();
                        w.save(w.getName());
                        System.out.println("Printing Image!");
                        //BufferedImage image = ImageWorld.makeImage2(w, 0, 0, 16, 1920, 1080);
                        BufferedImage image = ImageWorld.makeImage(w);
                        ImageWorld.saveImage(image, w.getName());
                        System.out.println("Done!");
                    }
                    if(command[0].equals("domain")){
                        w.addDomain(command[1]);
                    }
                    break;

                case 3:
                    if(command[0].equals("new")){
                        w = new World(command[1], Integer.parseInt(command[2]));
                    }
                    break;

                default:
                    System.out.println("Command not recognized.");
                    break;
            }

        } while(!quit);
        System.out.println("Thank you for simulating today!");
    }*/

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
    }


}
