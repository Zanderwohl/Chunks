package com.zanderwohl.chunks.World;

import com.zanderwohl.chunks.Block.BlockLibrary;
import org.json.JSONObject;
import util.FileLoader;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class WorldManager {

    private BlockLibrary library;

    ArrayList<World> worlds = new ArrayList<>();

    public WorldManager(){
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
}
