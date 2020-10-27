package com.zanderwohl.chunks.World;

import com.zanderwohl.chunks.Block.BlockLibrary;
import com.zanderwohl.console.Message;
import org.json.JSONObject;
import util.FileLoader;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorldManager {

    private BlockLibrary library;

    private HashMap<String, World> worlds = new HashMap<>();

    private ConcurrentLinkedQueue<Message> toConsole;

    public WorldManager(ConcurrentLinkedQueue<Message> toConsole){
        this.toConsole = toConsole;
        library = new BlockLibrary(toConsole);

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
            toConsole.add(new Message("message=Domain file for domain '" + domain + "' not found.\n"
                    + "source=World Manager\nseverity=warning"));
            //e.printStackTrace();
        }

    }

    public BlockLibrary getLibrary(){
        return library;
    }

    public String[] listWorldNames(){
        String[] worldNames = new String[worlds.size()];
        int i = 0;
        for(String name: worlds.keySet()){
            worldNames[i] = name;
            i++;
        }
        Arrays.sort(worldNames);
        return worldNames;
    }

    public void newWorld(String name, String seed){ //TODO: Seeds? Generator parameters?
        World newWorld = new World(name, formatSeed(seed), toConsole);
        toConsole.add(new Message("message=Creating world '" + name + "'...\nsource=World Manager\n" +
                "severity=normal"));
        newWorld.initialize(); //TODO: Probably don't do this here.
        worlds.put(name, newWorld);
        newWorld.setWorldManager(this);
        toConsole.add(new Message("message=Created world '" + name + "'.\nsource=World Manager\n" +
                "severity=normal"));
    }

    public World getWorld(String worldName){
        return worlds.get(worldName);
    }

    public void saveWorld(String name){
        toConsole.add(new Message("message=Saving world '" + name + "'...\nsource=World Manager"));
        World worldToSave = worlds.get(name);
        if(worldToSave == null){
            toConsole.add(new Message("message=World '" + name + "' does not exist!\nsource=World Manager\n"
            + "severity=critical"));
            return;
        }
        worldToSave.save(name);
        toConsole.add(new Message("message=World '" + name + "' saved.\nsource=World Manager"));
    }

    public void saveAllWorlds(){
        toConsole.add(new Message("message=Saving all worlds...\nsource=World Manager"));
        for(String name: worlds.keySet()){
            saveWorld(name);
        }
        toConsole.add(new Message("message=All worlds saved.\nsource=World Manager"));
    }

    public void killWorld(String name){
        World worldToKill = worlds.get(name);
        if(worldToKill == null){
            toConsole.add(new Message("message=No active world by the name '" + name +
                    "'.\nsource=World Manager"));
        } else {
            toConsole.add(new Message("message=Killing world '" + name + "'...\nsource=World Manager"));
            worlds.remove(name);
            toConsole.add(new Message("message=World '" + name + "' killed.\nsource=World Manager"));
        }
    }

    public static int formatSeed(String seed){
        if(seed == null){
            return 0;
        }
        int formattedSeed;
        try {
            formattedSeed = Integer.parseInt(seed);
            return formattedSeed;
        } catch (NumberFormatException e){
            return seed.hashCode();
        }
    }
}
