package com.zanderwohl.Generator;

public class Sine extends Generator {

    public Sine(int seed){

    }

    @Override
    public int eval(int x, int y, int z) {
        double big = distanceFromOrigin(x, z);
        double small = distanceBetween(x, z, 302, 0);

        double ground = ground(x, z);

        if (y < ground) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int ground(int x, int z) {
        double ground = 10 * Math.sin(x / (2 * Math.PI)) * Math.tan(z / (2 * Math.PI)) + 20.0;
        //double big = distanceFromOrigin(x, z);
        //double small = distanceBetween(x, z, 302, 0);
        return (int) Math.round(ground);
    }
}
