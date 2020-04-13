package com.zanderwohl.Generator;

import java.util.ArrayList;
import java.util.Random;

public class Layers extends Generator{

    Random rand;
    ArrayList<Simplex> layers = new ArrayList<>();

    public Layers(int seed){
        rand = new Random(seed);
        layers.add(new Simplex(seed));
    }

    @Override
    public int eval(int x, int y, int z) {
        return layers.get(0).eval(x, y, z);
    }
}
