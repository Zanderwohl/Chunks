package com.zanderwohl.chunks.World;

import com.zanderwohl.chunks.Generator.*;
import com.zanderwohl.console.Message;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A collection of Volumes and their relations to each other, to allow for dynamic generation of the world in small
 * pieces at a time. Contains other information.
 */
public class World {

    public static final String fileType = "vol";
    public static final String metaFileType = "meta";
    public static final String libraryFileType = "man";

    private ArrayList<Volume> volumes = new ArrayList<>();
    private Volume emptyVolume = new Volume(this);

    private Generator g;
    private WorldManager worldManager;

    public final int x_length = 20, y_length = 2, z_length = 20;
    private int seed = 0;

    private String name;

    private ArrayBlockingQueue<Message> toConsole;

    public World(String name, int seed, ArrayBlockingQueue<Message> toConsole){
        this.name = name;
        this.toConsole = toConsole;
        if(seed != 0) {
            this.seed = seed;
        } else {
            seed = WorldManager.formatSeed(name);
        }
        g = new Simplex(seed);
    }

    public World(String name, String saveFile, ArrayBlockingQueue<Message> toConsole){
        this.name = name;
        this.toConsole = toConsole;
        this.seed = 0; //TODO: get seed from file.
        //TODO: get generation algorithm from save file.
        g = new Simplex(seed);
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
       // Generator g = new Simplex(seed);
        Generator g = new Sine(seed);
        //Generator g = new Chaos(seed);
        //Generator g = new Layers(seed);
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

    /**
     * Outward-facing. Does fancy cache stuff,
     * loads the Volume if it's not in memory,
     * generates if it doesn't exists, etc.
     * @param volumeLocation The location to search
     * @param createNewVols Whether or not a new Volume should be created if not found.
     * @return The volume.
     */
    public Volume getVolume(Coord volumeLocation, boolean createNewVols){
        //if(terrain[x][y][z] != null){
        Volume vol = findVolume(volumeLocation);
        if(vol != null){
           return vol;
        } else {
            try{
                int x = volumeLocation.getX();
                int y = volumeLocation.getY();
                int z = volumeLocation.getZ();
                String location = "saves/" + name + "/" + x + "_" + y + "_" + z + ".vol";
                //terrain[x][y][z] = new Volume(0, 0, 0, g, this);
                //terrain[x][y][z].load(location);
                Coord coord = new Coord(x, y, z);
                Volume v = new Volume(coord, g, this);
                v.load(location);
                setVolume(volumeLocation, v);
            } catch (FileNotFoundException e){
                //terrain[x][y][z] = new Volume(0, 0, 0, g, this);
                if(createNewVols) {
                    Coord coord = new Coord(0, 0, 0);
                    setVolume(volumeLocation, new Volume(coord, g, this));
                } else {
                    System.err.println("empty vol!");
                    return emptyVolume;
                }
            }
        }
        //return terrain[x][y][z];
        return findVolume(volumeLocation);
    }

    /**
     * "Dumb" Volume finder, which just locates
     * a Volume in the array based on coords alone.
     * @param location The Coord of the volume to find.
     * @return
     */
    private Volume findVolume(Coord location){
        for(Volume v: volumes){
            if(v.atLocation(location)){
                return v;
            }
        }
        //System.out.println("Volume at " + x + " " + y + " " + z + " is null!");
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
        volumes.add(v);
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

    public int getBlock(Coord location){
        int x = location.getX();
        int y = location.getY();
        int z = location.getZ();

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
        int volx = x / Space.VOL_X;
        int volz = z / Space.VOL_Z;
        int blockx = x % Space.VOL_X;
        int blockz = z % Space.VOL_Z;

        if(blockx < 0){
            volx--;
            blockx = Space.VOL_X - blockx;
        }
        if(blockz < 0) {
            volz--;
            blockz = Space.VOL_Z - blockz;
        }

        if(blockx >= Space.VOL_X){
            volx++;
            blockx = blockx - Space.VOL_X;
        }
        if(blockz >= Space.VOL_Z){
            volz++;
            blockz = blockz - Space.VOL_Z;
        }

        //System.err.println(volx + ":" + blockx + " " + volz + ":" + blockz);
        int peak = 0;
        int index = y_length;
        while(peak == 0 && index > 0){  //start from the top, and get the maximums of each volume vertically
            index--;
            if(index >= 0) {
                try {
                    //peak = terrain[volx][index][volz].getMaxHeight(blockx, blockz);
                    Coord volume_location = new Coord(volx, index, volz);
                    peak = getVolume(volume_location, false).getMaxHeight(blockx, blockz);
                } catch (NullPointerException e){
                    //Do nothing. just move on to a terrain volume that DOES exist.
                }
            }
        }

        return peak + Space.volYToBlockY(index);
    }

    public Volume[] getVolumesInRadius(double radius, Coord center){
        ArrayList<Volume> volumes_list = new ArrayList<>();

        for(Volume v: this.volumes){ //TODO: This is inefficient at high numbers of Volumes.
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
