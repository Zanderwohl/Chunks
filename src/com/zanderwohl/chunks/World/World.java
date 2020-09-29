package com.zanderwohl.chunks.World;

import com.zanderwohl.chunks.Generator.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

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

    Generator g;

    int x_length = 10, y_length = 2, z_length = 10;
    private int seed = 0;

    private String name;

    public World(String name, int seed){
        this.name = name;
        if(seed != 0) {
            this.seed = seed;
        } else {
            seed = WorldManager.formatSeed(name);
        }
        g = new Simplex(seed);
    }

    public World(String name, String saveFile){
        this.name = name;
        this.seed = 0; //TODO: get seed from file.
        //TODO: get generation algorithm from save file.
        g = new Simplex(seed);
    }

    public void initialize(){
        basic();
        System.out.println("Initialization done!");
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
            e.printStackTrace();
        }
    }

    private void saveBlockLibrary(String folder, String saveName){
        try{
            PrintWriter out = new PrintWriter(folder + "/" + saveName + "/" + "blocks" + "." + libraryFileType);

            out.write("Domain.Block:idNo");
            out.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public void basic(){
        //Generator g = new Simplex(seed);
        //Generator g = new Sine(seed);
        Generator g = new Chaos(seed);
        //Generator g = new Layers(seed);
        for(int x = 0; x < x_length; x++){
            for(int y = 0; y < y_length; y++){
                for(int z = 0; z < z_length; z++){
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
     * @param volume_location The location to search
     * @return
     */
    public Volume getVolume(Coord volume_location){
        //if(terrain[x][y][z] != null){
        if(findVolume(volume_location) != null){
            //return terrain[x][y][z];
            //return emptyVolume;
        } else {
            try{
                int x = volume_location.getX();
                int y = volume_location.getY();
                int z = volume_location.getZ();
                String location = "saves/" + name + "/" + x + "_" + y + "_" + z + ".vol";
                //terrain[x][y][z] = new Volume(0, 0, 0, g, this);
                //terrain[x][y][z].load(location);
                Coord coord = new Coord(0, 0, 0);
                Volume v = new Volume(coord, g, this);
                v.load(location);
                setVolume(volume_location, v);
            } catch (FileNotFoundException e){
                //terrain[x][y][z] = new Volume(0, 0, 0, g, this);
                Coord coord = new Coord(0, 0, 0);
                setVolume(volume_location, new Volume(coord, g, this));
            }
        }
        //return terrain[x][y][z];
        return findVolume(volume_location);
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
        return Space.volXToX(x_length);
    }

    public int getY(){
        return Space.volYToY(y_length);
    }

    public int getZ(){
        return Space.volZToZ(z_length);
    }

    public int getBlock(Coord location){
        int x = location.getX();
        int y = location.getY();
        int z = location.getZ();
        int volx = x / Space.VOL_X;
        int voly = y / Space.VOL_Y;
        int volz = z / Space.VOL_Z;
        Coord volume_location = new Coord(volx, voly, volz);
        int blockx = x % Space.VOL_X;
        int blocky = y % Space.VOL_Y;
        int blockz = z % Space.VOL_Z;

        int block;
        try {
            block = findVolume(volume_location).getBlock(blockx, blocky, blockz);
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

        //System.err.println(volx + "." + blockx + " " + volz + "." + blockz);
        int peak = 0;
        int index = y_length;
        while(peak == 0 && index > 0){  //start from the top, and get the maximums of each volume vertically
            index--;
            if(index >= 0) {
                try {
                    //peak = terrain[volx][index][volz].getMaxHeight(blockx, blockz);
                    Coord volume_location = new Coord(volx, index, volz);
                    peak = getVolume(volume_location).getMaxHeight(blockx, blockz);
                } catch (NullPointerException e){
                    //Do nothing. just move on to a terrain volume that DOES exist.
                }
            }
        }

        return peak + Space.volYToY(index);
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
