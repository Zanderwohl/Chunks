package com.zanderwohl.chunks.Generator;

/**
 * Contains generators that create new terrain. Generators must inherit from the Generator abstract superclass, which
 * primarily contains the eval(int x, int y, int z) method that takes in a Volume location and creates a new volume, which
 * must be generated totally independent of the others.
 *
 * They do not need to be complete, and context-dependant generation
 * can be completed later, when the populate() method is called, which is only called when all the surrounding Volumes have
 * also been generated, and will always be called before the user enters them.
 *
 * Each generator should must also provide a ground(int x, int z) method. This returns only where the ground level would be
 * as an integer, without details. This way, the output from this method can be combined with other terrain generators to
 * make more complex terrain.
 */
public abstract class Generator {

    /**
     * Evaluate which block should be returned, given world-coordinates.
     * @param x The x World coordinate of the block to be evaluated.
     * @param y The y World coordinate of the block to be evaluated.
     * @param z The z World coordinate of the block to be evaluated.
     * @return The id of the block at this location.
     */
    public abstract int eval(int x, int y, int z);

    /**
     * Evaluate the ground height of the ground (World y) of a given x and z location.
     * Note that this is only within this Volume, so if a higher peak is in the next chunk up, it will simply return the
     * max of this chunk.
     * @param x The x World coordinate of the height to be evaluated.
     * @param z The z World coordinate of the height to be evaluated.
     * @return The height of the ground at this location.
     */
    public abstract int ground(int x, int z);

    /**
     * Calculates the horizontal distance from the origin to a given point.
     * It's the "map distance" - basically, does not account for height.
     * Like measuring the distance between cities, does not take into account elevation change.
     * For example, the distance between (0,0,0) and (0,0,4) is 4.
     * The distance between (0,0,0) and (0,2412,4) is also 4.
     * Hence why only x and z need be specified.
     * @param x The x World coordinate to be evaluated.
     * @param z The z World coordinate to be evaluated.
     * @return The distance as an unrounded value.
     */
    public double distanceFromOrigin(int x, int z){
        return Math.sqrt(x * x + z * z);
    }

    /**
     * Calculates the horizontal distance from one given point to another.
     * It's the "map distance" - basically, does not account for height.
     * Like measuring the distance between cities, does not take into account elevation change.
     * For example, the distance between (3,0,0) and (0,0,4) is 5.
     * The distance between (3,415,0) and (0,45,4) is also 5.
     * Hence why only x and z need be specified.
     * @param x1 The first x World coordinate to be evaluated.
     * @param z1 The first z World coordinate to be evaluated.
     * @param x2 The first x World coordinate to be evaluated.
     * @param z2 The first z World coordinate to be evaluated.
     * @return The distance as an unrounded value.
     */
    public double distanceBetween(int x1, int z1, int x2, int z2){
        int x = (x2 - x1);
        int z = (z2 - z1);
        return Math.sqrt(x * x + z * z);
    }
}
