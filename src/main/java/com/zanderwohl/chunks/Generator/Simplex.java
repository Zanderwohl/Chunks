package com.zanderwohl.chunks.Generator;

import com.kdotJPG.OpenSimplex.OpenSimplexNoise;
import com.zanderwohl.chunks.Block.BlockLibrary;

/**
 * A basic terrain where the height is taken from a 2D Simplex.
 */
public class Simplex extends Generator{

    OpenSimplexNoise noise;

    private int HEIGHT = 100;
    private double SCALE = 100.0;
    private double MAGNITUDE = 60.0;

    private BlockLibrary blocks;

    public Simplex(int seed, BlockLibrary blocks){
        noise = new OpenSimplexNoise(seed);
        this.blocks = blocks;
    }

    /**
     * Creates a new Simplex layer with parameters.
     * @param seed A seed for the RNG.
     * @param height The average height of the terrain.
     * @param scale The scale for the size (x and z) of the variation.
     * @param magnitude The magnitude modifier for the height (y) of the terrain.
     */
    public Simplex(int seed, BlockLibrary blocks, int height, int scale, int magnitude){
        noise = new OpenSimplexNoise(seed);
        HEIGHT = height;
        SCALE = scale;
        MAGNITUDE = magnitude;
        this.blocks = blocks;
    }

    /**
     * Evaluates based on 2D simplex noise as a heightmap, scaled and stretched.
     * @param x The x World coordinate of the block to be evaluated.
     * @param y The y World coordinate of the block to be evaluated.
     * @param z The z World coordinate of the block to be evaluated.
     * @return See superclass "Generator" documentation.
     */
    @Override
    public int eval(int x, int y, int z) {
        int ground = ground(x, z);
        if(y > ground){
            return 0;
        }
        if(y == ground) {
            return blocks.getIdByName("default", "grass");
        }
        if (y + 3 > ground) {
            return blocks.getIdByName("default", "dirt");
        } else {
            return blocks.getIdByName("default", "stone");
        }
    }

    /**
     * See superclass "Generator" documentation.
     * @param x The x World coordinate of the height to be evaluated.
     * @param z The z World coordinate of the height to be evaluated.
     * @return See superclass "Generator" documentation.
     */
    @Override
    public int ground(int x, int z) {
        double ground = (noise.eval(x / SCALE,/* y / SCALE,*/ z / SCALE) / Math.sqrt(3.0 / 4.0));
        ground *= MAGNITUDE;
        ground += HEIGHT;
        return (int) Math.round(ground);
    }
}
