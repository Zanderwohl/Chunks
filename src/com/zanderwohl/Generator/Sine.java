package com.zanderwohl.Generator;

public class Sine extends Generator {

    public Sine(int seed){

    }

    @Override
    public int eval(int x, int y, int z) {
        double ground = 10 * Math.sin(x / (2 * Math.PI)) * Math.tan(z / (2 * Math.PI));
        double big = distanceFromOrigin(x, z);
        double small = distanceBetween(x, z, 302, 0);
        //double ground = 10 * Math.sin(x / (2 * Math.PI)) * Math.tan(z / (2 * Math.PI));
        //double ground = 10 * Math.sin(big / (2 * Math.PI)) + 5 * Math.sin(small / (2 * Math.PI));
        ground += 20.0;
        if(y < ground) {
            //return ((y / 4) % 10) + 1;
            return 1;
        } else {
            return 0;
        }
    }
}
