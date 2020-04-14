package com.zanderwohl.chunks.Generator;

import java.util.Random;

/**
 * This is just a way to get all the existing blocks into the world.
 */
public class Chaos extends Generator{

    Random rand;

    public Chaos(int seed){
        rand = new Random(seed);
    }

    @Override
    public int eval(int x, int y, int z) {
        int ground = ground(x, z);
        if(z <= ground){
            return z % 10 + 1;
        } else {
            return 0;
        }
    }

    @Override
    public int ground(int x, int z) {
        return (x + z) % 10 + 30;
    }
}
