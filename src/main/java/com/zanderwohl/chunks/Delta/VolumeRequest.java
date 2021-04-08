package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.World.Coord;

import java.io.Serializable;

public class VolumeRequest extends Delta implements Serializable {

    public final int x, y, z;

    /**
     * A request for a volume by world coordinate.
     * @param x
     * @param y
     * @param z
     */
    public VolumeRequest(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public VolumeRequest(Coord location){
        location = location.blockToVol();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
    }
}
