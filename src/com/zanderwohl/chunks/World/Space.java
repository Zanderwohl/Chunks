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

    /**
     * The orders of the directions.
     */
    public static final int NORTH = 0;
    public static final int SOUTH = 1;
    public static final int EAST = 2;
    public static final int WEST = 3;
    public static final int UP = 4;
    public static final int DOWN = 5;

    /**
     * Whether a direction is positive or negative.
     */
    public static final int DIRECTION_NORTH = 1;
    public static final int DIRECTION_SOUTH = -1;
    public static final int DIRECTION_EAST = 1;
    public static final int DIRECTION_WEST = -1;
    public static final int DIRECTION_UP = 1;
    public static final int DIRECTION_DOWN = -1;


    public static int blockXToVolX(int x){
        return x / VOL_X;
    }

    public static int blockYToVolY(int y){
        return y / VOL_Y;
    }

    public static int blockZToVolZ(int z){
        return z / VOL_Z;
    }

    public static int volXToBlockX(int x){
        return x * VOL_X;
    }

    public static int volYToBlockY(int y){
        return y * VOL_Y;
    }

    public static int volZToBlockZ(int z){
        return z * VOL_Z;
    }

}
