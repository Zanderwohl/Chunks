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

    public abstract int eval(int x, int y, int z);

    public abstract int ground(int x, int z);

    public double distanceFromOrigin(int x, int z){
        return Math.sqrt(x * x + z * z);
    }

    public double distanceBetween(int x1, int z1, int x2, int z2){
        int x = (x2 - x1);
        int z = (z2 - z1);
        return Math.sqrt(x * x + z * z);
    }
}
