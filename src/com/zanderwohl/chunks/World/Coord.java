package com.zanderwohl.chunks.World;

/**
 * A thruple of x, y, and z coordinates in integer form.
 */
public class Coord {

    private int x, y, z;
    public enum Scale { UNKNOWN, VOLUME, BLOCK };
    private Scale scale;

    /**
     * Create new coordinate.
     * @param x X-value.
     * @param y Y-value.
     * @param z Z-value.
     */
    public Coord(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
        this.scale = Scale.UNKNOWN;
    }

    /**
     * Create new coordinate.
     * @param x X-value.
     * @param y Y-value.
     * @param z Z-value.
     * @param scale Whether this is a volume-coordinate or block-coordinate.
     */
    public Coord(int x, int y, int z, Scale scale){
        this.x = x;
        this.y = y;
        this.z = z;
        this.scale = scale;
    }

    /**
     * Getter for x.
     * @return x-component of this Coord.
     */
    public int getX(){
        return x;
    }

    /**
     * Getter for y.
     * @return y-component of this Coord.
     */
    public int getY(){
        return y;
    }

    /**
     * Getter for z.
     * @return z-component of this Coord.
     */
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

    /**
     * Determines if two coordinates are equal in position.
     * Uses the scale of the Coord method is called on.
     * @param other The other coordinate.
     * @return
     */
    public boolean equals(Coord other){
        if(this.scale == Scale.BLOCK && other.scale == Scale.VOLUME){
            other = other.volToBlock();
        }
        if(this.scale == Scale.VOLUME && other.scale == Scale.BLOCK){
            other = other.blockToVol();
        }
        return this.getX() == other.getX() && this.getY() == other.getY() && this.getZ() == other.getZ();
    }

    /**
     * Produces a copy of this coordinate converted to Block coordinate.
     * If already Block coordinate, returns itself.
     * Can be used as a guardrail to ALWAYS have a Block coordinate.
     * @return The new World Coordinate.
     */
    public Coord volToBlock(){
        if(this.scale == Scale.BLOCK){
            return this;
        }
        int x_ = Space.volXToBlockX(x);
        int y_ = Space.volYToBlockY(y);
        int z_ = Space.volZToBlockZ(z);
        return new Coord(x_, y_, z_);
    }

    /**
     * Produces a copy of this coordinate converted to Volume coordinate.
     * If already a volume coordinate, returns itself.
     * Can be used as a guardrail to ALWAYS have a Volume coordinate.
     * @return The new Volume coordinate.
     */
    public Coord blockToVol(){
        if(this.scale == Scale.VOLUME){
            return this;
        }
        int x_= Space.blockXToVolX(x);
        int y_ = Space.blockYToVolY(y);
        int z_ = Space.blockZToVolZ(z);
        return new Coord(x_, y_, z_);
    }
}
