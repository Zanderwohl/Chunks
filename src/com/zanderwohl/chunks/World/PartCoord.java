package com.zanderwohl.chunks.World;

public class PartCoord extends Coord {

    private int volX, volY, volZ;
    private int blockX, blockY, blockZ;

    public PartCoord(int x, int y, int z){
        super(x, y, z, Scale.BLOCK);
        volX = x / Space.VOL_X;
        volY = y / Space.VOL_Y;
        volZ = z / Space.VOL_Z;
        blockX = x % Space.VOL_X;
        blockY = y % Space.VOL_Y;
        blockZ = z % Space.VOL_Z;

        if(blockX < 0){
            volX--;
            blockX = Space.VOL_X - blockX;
        }
        if(blockY < 0){
            volY--;
            blockY = Space.VOL_Y - blockY;
        }
        if(blockZ < 0) {
            volZ--;
            blockZ = Space.VOL_Z - blockZ;
        }

        if(blockX >= Space.VOL_X){
            volX++;
            blockX = blockX - Space.VOL_X;
        }
        if(blockY >= Space.VOL_Y){
            volY++;
            blockY = blockY - Space.VOL_Y;
        }
        if(blockZ >= Space.VOL_Z){
            volZ++;
            blockZ = blockZ - Space.VOL_Z;
        }
    }

    public int getBlockX(){
        return blockX;
    }

    public int getBlockY(){
        return blockY;
    }

    public int getBlockZ(){
        return blockZ;
    }

    public int getVolX(){
        return volX;
    }

    public int getVolY(){
        return volY;
    }

    public int getVolZ(){
        return volZ;
    }

}
