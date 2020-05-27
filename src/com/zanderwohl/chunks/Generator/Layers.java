package com.zanderwohl.chunks.Generator;

import java.util.ArrayList;
import java.util.Random;

/**
 * A more complicated generator that takes multiple layers and stack them on top of each other.
 */
public class Layers extends Generator{

    Random rand;
    ArrayList<Generator> layers = new ArrayList<>();

    /**
     * TODO: write this.
     * @param seed The seed for the RNG.
     */
    public Layers(int seed){
        rand = new Random(seed);
        layers.add(new Simplex(seed));
        layers.add(new Sine(seed));
    }

    /**
     * TODO: Finish this method and document it.
     * @param x The x World coordinate of the block to be evaluated.
     * @param y The y World coordinate of the block to be evaluated.
     * @param z The z World coordinate of the block to be evaluated.
     * @return TODO: Write
     */
    @Override
    public int eval(int x, int y, int z) {
        return layers.get(0).eval(x, y, z) + layers.get(1).eval(x, y, z);
    }

    /**
     * See superclass "Generator" documentation.
     * @param x The x World coordinate of the height to be evaluated.
     * @param z The z World coordinate of the height to be evaluated.
     * @return See superclass "Generator" documentation.
     */
    @Override
    public int ground(int x, int z) {
        return 0;
    }
}
