package com.zanderwohl.chunks.World;

import util.FileLoader;
import com.zanderwohl.chunks.Generator.*;
import org.json.JSONObject;
import com.zanderwohl.chunks.Block.BlockLibrary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A collection of Volumes and their relations to each other, to allow for dynamic generation of the world in small
 * pieces at a time. Contains other information.
 */
public class World {

    public static final String fileType = "vol";
    public static final String metaFileType = "meta";
    public static final String libraryFileType = "man";

    private ArrayList<Volume> volumes = new ArrayList<Volume>();
    private Volume emptyVolume = new Volume(this);

    Generator g;

    int x_length = 10, y_length = 2, z_length = 10;
    //Volume[][][] terrain = new Volume[x_length][y_length][z_length];
    private int seed = 0;

    private String name;

    public World(String name){
        this.name = name;
        //basic();

    }

    public World(String name, int seed){
        this.name = name;
        this.seed = seed;
        g = new Simplex(seed);
    }

    public World(String name, String saveFile){
        this.name = name;
        this.seed = 0; //TODO: get seed from file.
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
            try {
                volume.save("saves/" + saveName);
            } catch(FileNotFoundException exception){
                exception.printStackTrace();
            }

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
                    setVolume(x, y, z, c);
                }
            }
        }
    }

    /**
     * Outward-facing. Does fancy cache stuff,
     * loads the Volume if it's not in memory,
     * generates if it doesn't exists, etc.
     * @param x Volume-scale x
     * @param y Volume-scale y
     * @param z Volume-scale z
     * @return
     */
    public Volume getVolume(int x, int y, int z){
        //if(terrain[x][y][z] != null){
        if(findVolume(x, y, z) != null){
            //return terrain[x][y][z];
            //return emptyVolume;
        } else {
            try{
                String location = "saves/" + name + "/" + x + "_" + y + "_" + x + ".vol";
                //terrain[x][y][z] = new Volume(0, 0, 0, g, this);
                //terrain[x][y][z].load(location);
                Coord coord = new Coord(0, 0, 0);
                Volume v = new Volume(coord, g, this);
                v.load(location);
                setVolume(x, y, z, v);
            } catch (FileNotFoundException e){
                //terrain[x][y][z] = new Volume(0, 0, 0, g, this);
                Coord coord = new Coord(0, 0, 0);
                setVolume(x, y, z, new Volume(coord, g, this));
            }
        }
        //return terrain[x][y][z];
        return findVolume(x, y, z);
    }

    /**
     * "Dumb" Volume finder, which just locates
     * a Volume in the array based on coords alone.
     * @param x Volume-scale x
     * @param y Volume-scale y
     * @param z Volume-scale z
     * @return
     */
    private Volume findVolume(int x, int y, int z){
        for(Volume v: volumes){
            if(x == v.getX() && y == v.getY() && z == v.getZ()){
                return v;
            }
        }
        //System.out.println("Volume at " + x + " " + y + " " + z + " is null!");
        return null;
    }

    /**
     * Uses dumb find to remove a chunk if it exists
     * in memory.
     * @param x Volume-scale x
     * @param y Volume-scale y
     * @param z Volume-scale z
     */
    private void removeVolume(int x, int y, int z){
        Volume v = findVolume(x, y, z);
        if(v != null){
            volumes.remove(v);
        }
    }

    /**
     * Sets a Volume into the world, and tells
     * the Volume where it lives.
     * @param x Volume-scale x
     * @param y Volume-scale y
     * @param z Volume-scale z
     * @param v The Volume to be placed in the world.
     */
    private void setVolume(int x, int y, int z, Volume v){
        removeVolume(x, y, z);
        v.setX(x);
        v.setY(y);
        v.setZ(z);
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

    public int getBlock(int x, int y, int z){
        int volx = x / Space.VOL_X;
        int voly = y / Space.VOL_Y;
        int volz = z / Space.VOL_Z;
        int blockx = x % Space.VOL_X;
        int blocky = y % Space.VOL_Y;
        int blockz = z % Space.VOL_Z;

        int block;
        try {
            block = findVolume(volx, voly, volz).getBlock(blockx, blocky, blockz);
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
                    peak = getVolume(volx, index, volz).getMaxHeight(blockx, blockz);
                } catch (NullPointerException e){
                    //Do nothing. just move on to a terrain volume that DOES exist.
                }
            }
        }

        return peak + Space.volYToY(index);
    }

    public Volume[] getVolumesInRadius(double radius, Coord center){
        ArrayList<Volume> volumes_list = new ArrayList<>();

        for(Volume v: this.volumes){
            //if()
        }

        Volume[] volumes_array = new Volume[volumes_list.size()];
        for(int i = 0; i < volumes_list.size(); i++){
            volumes_array[i] = volumes_list.get(i);
        }
        return volumes_array;
    }
}
