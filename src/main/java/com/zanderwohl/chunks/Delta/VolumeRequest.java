package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.World.Coord;

import java.io.Serializable;

/**
 * A request for a volume, sent from a client to ask a server for a volume.
 */
public class VolumeRequest extends Delta implements Serializable {

    private static final long serialVersionUID = 32112000008L;

    /**
     * The location of the volume in volume-scale "world coordinates".
     */
    public final int x, y, z;

    /**
     * A request for a volume by world coordinate.
     * @param x X-location.
     * @param y Y-location.
     * @param z Z-location.
     */
    public VolumeRequest(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * A request for a volume by world coordinate.
     * @param location The location object.
     */
    public VolumeRequest(Coord location){
        location = location.blockToVol();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
    }
}
