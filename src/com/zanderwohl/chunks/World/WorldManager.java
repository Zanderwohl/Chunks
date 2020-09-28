package com.zanderwohl.chunks.World;

import com.zanderwohl.chunks.Block.BlockLibrary;
import com.zanderwohl.console.Message;
import org.json.JSONObject;
import util.FileLoader;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorldManager {

    private BlockLibrary library;

    ArrayList<World> worlds = new ArrayList<>();

    ConcurrentLinkedQueue<Message> toConsole;

    public WorldManager(ConcurrentLinkedQueue<Message> toConsole){
        this.toConsole = toConsole;
        library = new BlockLibrary();

        prepare();
    }

    public void generateWorld(){

    }

    public void prepare(){
        addDomain("default");
    }

    public void addDomain(String domain){
        JSONObject json;
        try {
            FileLoader domainFile = new FileLoader("domains/" + domain + "/domain.json");
            json = new JSONObject(domainFile.fileToString());
            library.addDomain(json);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

    }

    public BlockLibrary getLibrary(){
        return library;
    }

    public void newWorld(String name){ //TODO: Seeds? Generator parameters?
        World newWorld = new World(name);
        toConsole.add(new Message("message=Creating world '" + name + "'...\nsource=World Manager\n" +
                "severity=normal"));
        newWorld.initialize(); //TODO: Probably don't do this here.
        worlds.add(newWorld);
        toConsole.add(new Message("message=Created world '" + name + "'.\nsource=World Manager\n" +
                "severity=normal"));
    }
}
