package com.zanderwohl.Generator;

import java.util.Random;

public class Chaos extends Generator{

    Random rand;

    public Chaos(int seed){
        rand = new Random(seed);
    }

    @Override
    public int eval(int x, int y, int z) {
        return (x + z) % (y | 1);
    }
}
