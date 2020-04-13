package com.zanderwohl;

import com.zanderwohl.Image.ImageWorld;
import com.zanderwohl.World.World;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
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
    }

    public static void prepareEnvironment(){
        makeDirectories();
    }

    public static void makeDirectories(){
        new File("saves/").mkdirs();
    }


}
