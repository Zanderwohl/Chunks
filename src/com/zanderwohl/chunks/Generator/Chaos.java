package com.zanderwohl.chunks.Generator;

import java.util.Random;

/**
 * This is just a way to get all the existing blocks into the world.
 */
public class Chaos extends Generator{

    Random rand;

    /**
     * Constructs a Chaos generator.
     * @param seed The seed fed into the RNG.
     */
    public Chaos(int seed){
        rand = new Random(seed);
    }

    /**
     * See superclass "Generator" documentation.
     * @param x The x World coordinate of the block to be evaluated.
     * @param y The y World coordinate of the block to be evaluated.
     * @param z The z World coordinate of the block to be evaluated.
     * @return See superclass "Generator" documentation.
     */
    @Override
    public int eval(int x, int y, int z) {
        int ground = ground(x, z);
        if(z <= ground){
            return z % 10 + 1;
        } else {
            return 0;
        }
    }

    /**
     * See superclass "Generator documentation.
     * @param x The x World coordinate of the height to be evaluated.
     * @param z The y World coordinate of the height to be evaluated.
     * @return See superclass "Generator" documentation.
     */
    @Override
    public int ground(int x, int z) {
        return (x + z) % 10 + 30;
    }
}
