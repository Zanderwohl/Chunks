package com.zanderwohl.Generator;

public abstract class Generator {

    public abstract int eval(int x, int y, int z);

    public double distanceFromOrigin(int x, int z){
        return Math.sqrt(x * x + z * z);
    }

    public double distanceBetween(int x1, int z1, int x2, int z2){
        int x = (x2 - x1);
        int z = (z2 - z1);
        return Math.sqrt(x * x + z * z);
    }
}
