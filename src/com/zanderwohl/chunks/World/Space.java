package com.zanderwohl.chunks.World;


/**
 * Functions for converting world coordinates to Volume coordinates and vice-versa. World coordinates are the tuples
 * representing a point in the world relative to that World's origin, and Volume coordinates are tuples that describe a
 * location relative to a smaller Volume within the World.
 */
public class Space {
    /**
     * The x-width of a single Volume.
     */
    public static final int VOL_X = 16;
    /**
     * The y-height of a single Volume.
     */
    public static final int VOL_Y = 128;
    /**
     * The z-length of a single Volume.
     */
    public static final int VOL_Z = 16;

    public static int xToVolX(int x){
        return x / VOL_X;
    }

    public static int yToVolY(int y){
        return y / VOL_Y;
    }

    public static int zToVolZ(int z){
        return z / VOL_Z;
    }

    public static int volXToX(int x){
        return x * VOL_X;
    }

    public static int volYToY(int y){
        return y * VOL_Y;
    }

    public static int volZToZ(int z){
        return z * VOL_Z;
    }



}
