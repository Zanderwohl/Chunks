package com.zanderwohl.chunks.World;

import com.zanderwohl.chunks.Block.Block;
import com.zanderwohl.chunks.Block.BlockLibrary;
import com.zanderwohl.chunks.Delta.Delta;
import com.zanderwohl.chunks.Generator.*;
import com.zanderwohl.console.Message;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A collection of Volumes and their relations to each other, to allow for dynamic generation of the world in small
 * pieces at a time. Contains other information.
 */
public class World extends Delta implements Serializable {

    public static transient final String fileType = "vol";
    public static transient final String metaFileType = "meta";
    public static transient final String libraryFileType = "man";

    private transient ArrayList<Volume> volumes = new ArrayList<>();
    private Volume emptyVolume = new Volume(this);

    private transient Generator g;
    private transient WorldManager worldManager;

    public static final int x_length = 5, y_length = 2, z_length = 5;
    private int seed = 0;

    private String name;

    private transient ArrayBlockingQueue<Message> toConsole;

    private BlockLibrary blocks;

    public World(String name, int seed, ArrayBlockingQueue<Message> toConsole, BlockLibrary blocks){
        this.name = name;
        this.toConsole = toConsole;
        if(seed != 0) {
            this.seed = seed;
        } else {
            seed = WorldManager.formatSeed(name);
        }
        g = new Simplex(seed, blocks);
        this.blocks = blocks;
    }

    public World(String name, String saveFile, ArrayBlockingQueue<Message> toConsole, BlockLibrary blocks){
        this.name = name;
        this.toConsole = toConsole;
        this.seed = 0; //TODO: get seed from file.
        //TODO: get generation algorithm from save file.
        g = new Simplex(seed, blocks);
    }

    public void setWorldManager(WorldManager wm){
        worldManager = wm;
    }

    public WorldManager getWorldManager(){
        return worldManager;
    }

    public void initialize(){
        basic();
        toConsole.add(new Message("message=Initialization of '" + name + "' done!\nsource=World: " + name));
    }

    public void save(String saveName){
        new File("saves/" + saveName).mkdirs();

        saveWorldData("saves/", saveName);
        saveBlockLibrary("saves/", saveName);

        for(Volume volume : volumes){
            //System.out.println(volume);
            volume.save("saves/" + saveName);

        }

    }

    public String getName(){
        return name;
    }

    public void setName(String newName){
        this.name = newName;
    }

    private void saveWorldData(String folder, String saveName){
        try{
            PrintWriter out = new PrintWriter(folder + "/" + saveName + "/" + "world" + "." + metaFileType);
            out.write("name:" + saveName + "\n");
            out.write("seed:" + seed + "\n");
            out.close();
        } catch (FileNotFoundException e){
            toConsole.add(new Message("message=Unable to save '" + name + "'.\nsource=World: " + name
                    + "\nseverity=critical"));
            e.printStackTrace();
        }
    }

    private void saveBlockLibrary(String folder, String saveName){
        try{
            PrintWriter out = new PrintWriter(folder + "/" + saveName + "/" + "blocks" + "." + libraryFileType);

            out.write("Domain.Block:idNo");
            out.close();
        } catch (FileNotFoundException e){
            toConsole.add(new Message("message=Unable to save block library for '" + name + "'.\nsource=World: "
                    + name));
            e.printStackTrace();
        }
    }

    /**
     * Generate a little bit of world. A finite bit.
     */
    public void basic(){
        for(int x = -x_length; x < x_length; x++){
            for(int y = 0; y < y_length; y++){
                for(int z = -z_length; z < z_length; z++){
                    Coord location = new Coord(x, y, z);
                    Volume c = new Volume(location, g, this);
                    setVolume(location, c);
                }
            }
        }
    }

    public Volume getVolume(Coord volumeLocation, boolean createNewVols){
        return getVolume(volumeLocation, createNewVols, true);
    }

    /**
     * Outward-facing. Does fancy cache stuff,
     * loads the Volume if it's not in memory,
     * generates if it doesn't exists, etc.
     * @param volumeLocation The location to search
     * @param createNewVols Whether or not a new Volume should be created if not found.
     * @return The volume.
     */
    public Volume getVolume(Coord volumeLocation, boolean createNewVols, boolean loadFromDisk){
        //if(terrain[x][y][z] != null){
        Volume vol = findVolume(volumeLocation);
        int x_ = volumeLocation.getX();
        int y_ = volumeLocation.getY();
        int z_ = volumeLocation.getZ();
        //System.out.println(x_ > 0 && y_ > 0 && z_ > 0);
        if(vol == null){
            int p = 1;
            //System.out.println(volumeLocation + " is null.");
        }
        if(vol != null){
           return vol;
        } else {
            if(loadFromDisk) {
                try {
                    int x = volumeLocation.getX();
                    int y = volumeLocation.getY();
                    int z = volumeLocation.getZ();
                    String location = "saves/" + name + "/" + x + "_" + y + "_" + z + ".vol";
                    Volume v = Volume.load(location);
                    if(v != null) {
                        setVolume(volumeLocation, v);
                    }
                    return v;
                } catch (FileNotFoundException e) {
                    if (createNewVols) {
                        Coord coord = new Coord(0, 0, 0);
                        setVolume(volumeLocation, new Volume(volumeLocation, g, this));
                    } else {
                        //System.err.println("empty vol!");
                        return emptyVolume;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                return emptyVolume;
            }
        }

        return null;
    }

    public void setVolume(Volume v){
        if(volumes == null){ //Since volumes is transient, it may be null.
            volumes = new ArrayList<>();
        }
        Volume extantVolume = findVolume(v.getLocation());
        if(extantVolume != null){
            volumes.remove(extantVolume);
        }
        //System.out.println(v.getLocation());
        volumes.add(v);
    }

    /**
     * "Dumb" Volume finder, which just locates
     * a Volume in the array based on coords alone.
     * @param location The Coord of the volume to find.
     * @return
     */
    private Volume findVolume(Coord location){
        for(Volume v: volumes){
            if(v.getLocation().equals(location)){
                return v;
            }
        }
        //TODO: Some kind of indexing scheme to make lookup much faster.
        //System.out.println("Volume at " + location + " is null!");
        return null;
    }

    /**
     * Uses dumb find to remove a chunk if it exists
     * in memory.
     * @param location Location the volume to be removed is at.
     */
    private void removeVolume(Coord location){
        Volume v = findVolume(location);
        if(v != null){
            volumes.remove(v);
        }
    }

    /**
     * Sets a Volume into the world, and tells
     * the Volume where it lives.
     * @param location The Coord to place the Volume at.
     * @param v The Volume to be placed in the world.
     */
    private void setVolume(Coord location, Volume v){
        removeVolume(location);
        v.setLocation(location);
        volumes.add(v); //TODO: remove old volumes?
    }

    public int getX(){
        return Space.volXToBlockX(x_length);
    }

    public int getY(){
        return Space.volYToBlockY(y_length);
    }

    public int getZ(){
        return Space.volZToBlockZ(z_length);
    }

    public int getBlockID(Coord location){
        int x = location.getX();
        int y = location.getY();
        int z = location.getZ();
        return getBlockID(x, y, z);
    }

    public int getBlockID(int x, int y, int z){
        int volx = x / Space.VOL_X;
        int voly = y / Space.VOL_Y;
        int volz = z / Space.VOL_Z;
        int blockx = x % Space.VOL_X;
        int blocky = y % Space.VOL_Y;
        int blockz = z % Space.VOL_Z;

        if(blockx < 0){
            volx--;
            blockx = Space.VOL_X - blockx;
        }
        if(blocky < 0){
            voly--;
            blocky = Space.VOL_Y - blocky;
        }
        if(blockz < 0) {
            volz--;
            blockz = Space.VOL_Z - blockz;
        }

        if(blockx >= Space.VOL_X){
            volx++;
            blockx = blockx - Space.VOL_X;
        }
        if(blocky >= Space.VOL_Y){
            voly++;
            blocky = blocky - Space.VOL_Y;
        }
        if(blockz >= Space.VOL_Z){
            volz++;
            blockz = blockz - Space.VOL_Z;
        }
        Coord volumeLocation = new Coord(volx, voly, volz);

        int block;
        try {
            block = findVolume(volumeLocation).getBlock(blockx, blocky, blockz);
        } catch (NullPointerException e){
            block =  0;
        }
        //System.out.println(block);
        return block;
    }

    public int getPeak(int x, int z){
        PartCoord loc = new PartCoord(x, 0, z);
        int volx = loc.getVolX();
        int volz = loc.getVolZ();
        int blockx = loc.getBlockX();
        int blockz = loc.getBlockZ();
        //System.out.println(blockx + " " + blockz);

        //System.err.println(volx + ":" + blockx + " " + volz + ":" + blockz);
        int peak = 0;
        int index = y_length;
        do {
            Volume v  = getVolume(new Coord(volx, index, volz, Coord.Scale.VOLUME), false, false);
            //System.out.println(v == emptyVolume);
            if(v != emptyVolume){
                peak = v.getMaxHeight(blockx, blockz);
            }
            index--;
        } while(peak == 0 && index >= 0);

        return peak + Space.volYToBlockY(index);
    }

    public Volume[] getVolumesInRadius(double radius, Coord center){
        ArrayList<Volume> volumes_list = new ArrayList<>();

        for(Volume v: volumes){ //TODO: This is inefficient at high numbers of Volumes.
            if(center.otherInRadius(v.getLocation(), radius)){
                volumes_list.add(v);
            }
        }

        Volume[] volumes_array = new Volume[volumes_list.size()];
        for(int i = 0; i < volumes_list.size(); i++){
            volumes_array[i] = volumes_list.get(i);
        }
        return volumes_array;
    }

    public void unloadDistant(double radius, Coord center, String saveName){
        Iterator<Volume> i = volumes.iterator();
        while (i.hasNext()) {
            Volume v = i.next(); // must be called before you can call i.remove()
            if(!center.otherInRadius(v.getLocation(), radius)) {
                v.save("saves/" + saveName);
                i.remove();
            }
        }
    }
}
