package com.zanderwohl.chunks.Generator;

import java.util.ArrayList;
import java.util.Random;

/**
 * A more complicated generator that takes multiple layers and stack them on top of each other.
 */
public class Layers extends Generator{

    Random rand;
    ArrayList<Generator> layers = new ArrayList<>();

    public Layers(int seed){
        rand = new Random(seed);
        layers.add(new Simplex(seed));
        layers.add(new Sine(seed));
    }

    @Override
    public int eval(int x, int y, int z) {
        return layers.get(0).eval(x, y, z) + layers.get(1).eval(x, y, z);
    }

    @Override
    public int ground(int x, int z) {
        return 0;
    }
}
