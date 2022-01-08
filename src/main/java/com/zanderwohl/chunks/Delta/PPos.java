package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.Client.ClientIdentity;
import com.zanderwohl.chunks.World.Coord;

import java.io.Serializable;

/**
 * A player's position. Includes the player's ID.
 */
public class PPos extends Delta implements Serializable {

    private static final long serialVersionUID = 32112000004L;

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

    /**
     * Checks if two PPos objects are equal (or close enough) to each other.
     * The epsilon used is .001.
     * @param other The other object to compare to.
     * @return True if the volumes are close enough.
     */
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
