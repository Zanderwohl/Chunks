package com.zanderwohl.chunks.World;

public class Coord {

    private int x, y, z;

    public Coord(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getZ(){
        return z;
    }

    /**
     * Gives distance between this and other point.
     * @param other The other coordinate to be compared to.
     * @return The distance.
     */
    public double distance(Coord other){
        int x_ = x - other.getX();
        int y_ = y - other.getY();
        int z_ = z - other.getZ();
        return Math.sqrt(Math.pow(x_,2) + Math.pow(y_,2) + Math.pow(z_,2));
    }

    /**
     * Gives distance from a top-down perspective between this point and other point.
     * Compares along x and z axis; y axis length does not matter.
     * @param other The other coordinate to be compared to.
     * @return The distance.
     */
    public double horizontalDistance(Coord other){
        int x_ = x - other.getX();
        int z_ = z - other.getZ();
        return Math.sqrt(Math.pow(x_,2) + Math.pow(z_,2));
    }

    /**
     * Returns if point b is within a specified radius of point a; this point.
     * a.otherInRadius(b, radius)
     * Comparision is inclusive; points at the exact radius are included.
     * @param other Point b; the point to check if it is nearby to this point.
     * @param radius The radius that point b must be within.
     * @return True if "other" is within radius of this point.
     */
    public boolean otherInRadius(Coord other, double radius){
        return (this.distance(other) <= radius);
    }

    /**
     * Returns if point b is within a specified horizontal radius of point a; this point.
     * a.otherInRadius(b, radius)
     * Comparision is inclusive; points at the exact radius are included.
     * For more information about how this works, see doc on horizontalDistance()
     * @param other Point b; the point to check if it is nearby to this point.
     * @param radius The radius that point b must be within.
     * @return True if "other" is within radius of this point.
     */
    public boolean otherInHorizontalRadius(Coord other, double radius){
        return (this.horizontalDistance(other) <= radius);
    }

    public boolean equals(Coord other){
        return this.getX() == other.getX() && this.getY() == other.getY() && this.getZ() == other.getZ();
    }
}
