package com.zanderwohl.Generator;

import com.zanderwohl.OpenSimplex.OpenSimplexNoise;

public class Simplex extends Generator{

    OpenSimplexNoise noise;

    private int HEIGHT = 100;
    private double SCALE = 100.0;
    private double MAGNITUDE = 60.0;

    public Simplex(int seed){
        noise = new OpenSimplexNoise(seed);
    }

    public Simplex(int seed, int height, int scale, int magnitude){
        noise = new OpenSimplexNoise(seed);
        HEIGHT = height;
        SCALE = scale;
        MAGNITUDE = magnitude;
    }

    @Override
    public int eval(int x, int y, int z) {
        int ground = ground(x, z);
        if(y < ground) {
            //return (y / 2) % 10;
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int ground(int x, int z) {
        double ground = (noise.eval(x / SCALE,/* y / SCALE,*/ z / SCALE) / Math.sqrt(3.0 / 4.0));
        ground *= MAGNITUDE;
        ground += HEIGHT;
        return (int) Math.round(ground);
    }
}
