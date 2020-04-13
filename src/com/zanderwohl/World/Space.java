package com.zanderwohl.World;

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
