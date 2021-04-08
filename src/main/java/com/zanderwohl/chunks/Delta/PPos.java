package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.Client.ClientIdentity;
import com.zanderwohl.chunks.World.Coord;

import java.io.Serializable;

public class PPos extends Delta implements Serializable {

    public final double x, y, z;
    public final double pitch, yaw;
    public final String player;

    /**
     * A Player position.
     * @param x
     * @param y
     * @param z
     * @param pitch
     * @param yaw
     * @param player
     */
    public PPos(double x, double y, double z, double pitch, double yaw, String player) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.player = player;
    }

    public Coord toCoord(){
        return new Coord((int) x, (int) y, (int) z, Coord.Scale.BLOCK);
    }

    public boolean equals(PPos other){
        double epsilon = .001;
        return (
                   (Math.abs(x - other.x) < epsilon)
                && (Math.abs(y - other.y) < epsilon)
                && (Math.abs(z - other.z) < epsilon)
                && (Math.abs(pitch - other.pitch) < epsilon)
                && (Math.abs(yaw - other.yaw) < epsilon)
                );
    }
}
