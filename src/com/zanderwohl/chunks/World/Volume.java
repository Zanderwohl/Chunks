package com.zanderwohl.chunks.World;

import util.FileLoader;
import com.zanderwohl.chunks.Generator.Generator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import static com.zanderwohl.chunks.World.Space.*;

/**
 * A unit of blocks in three dimensions that has a height, width and depth. Is stored as a single file, and is given a
 * generator in order to generate and populate its own contents when called to do so.
 */
public class Volume {

    int x, y, z;
    int swb_x, swb_y, swb_z; //Southwestbottom corner x, y, and z block coordinates to world.
    int[][][] blocks;
    int[][] maximums;
    int[][] maxBlocks;

    World w;

    /**
     * Empty volume of nothingness. Has no position or anything.
     * @param w
     */
    public Volume(World w){
        this.w = w;

        setX(0);
        setY(0);
        setZ(0);
        blocks = new int[VOL_X][VOL_Y][VOL_Z];
        maximums = new int[VOL_X][VOL_Z];
        maxBlocks = new int[VOL_X][VOL_Z];
        calcMaximums();
    }

    public Volume(int x, int y, int z, Generator g, World w){
        this.w = w;

        setX(x);
        setY(y);
        setZ(z);

        blocks = new int[VOL_X][VOL_Y][VOL_Z];

        for(int y1 = 0; y1 < VOL_Y; y1++){
            for(int x1 = 0; x1 < VOL_X; x1++){
                for(int z1 = 0; z1 < VOL_Z; z1++){
                    blocks[x1][y1][z1] = g.eval(x1 + swb_x, y1 + swb_y, z1 + swb_z);
                }
            }
        }

        maximums = new int[VOL_X][VOL_Z];
        maxBlocks = new int[VOL_X][VOL_Z];
        calcMaximums();
    }

    public void load(String location) throws FileNotFoundException {
        FileLoader volumeFile = new FileLoader(location);
        String volumeString = volumeFile.fileToString();
        String[] volumeArray = volumeString.split("\\s");
        int[] blockList = new int[volumeArray.length];
        for(int i = 3; i < volumeArray.length; i++){
            blockList[i] = Integer.parseInt(volumeArray[i]);
        }
        x = Integer.parseInt(volumeArray[0].split(":")[1]);
        y = Integer.parseInt(volumeArray[1].split(":")[1]);
        z = Integer.parseInt(volumeArray[2].split(":")[1]);

        swb_x = Space.volXToX(x);
        swb_y = Space.volYToY(y);
        swb_z = Space.volZToZ(z);

        for(int y = 0; y < VOL_Y; y++){
            for(int x = 0; x < VOL_X; x++){
                for(int z = 0; z < VOL_Z; z++){
                    blocks[x][y][z] = blockList[y * VOL_Y * VOL_X + x * VOL_X + z];
                    //blocks[x][y][z] = Character.toString((char)blockList[y * VOL_Y * VOL_X + x * VOL_X + z]);
                }
            }
        }
    }

    public void save(String saveName) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(saveName + "/" + toString() + "." + World.fileType);
        out.write("x:" + x + "\n");
        out.write("y:" + y + "\n");
        out.write("z:" + z + "\n");
        for(int y = 0; y < VOL_Y; y++){
            for(int x = 0; x < VOL_X; x++){
                for(int z = 0; z < VOL_Z; z++){
                    out.write(blocks[x][y][z] + "");
                    //out.write(Character.toString((char)blocks[x][y][z]));
                    if(z < VOL_Z - 1){
                        out.write("\t");
                    }
                }
                out.write("\n");
            }
            out.write("\n");
        }
        out.close();
    }

    public String toString(){
        int save_x = x;
        int save_y = y;
        int save_z = z;
        return save_x + "_" + save_y + "_" + save_z;
    }

    public int getBlock(int x, int y, int z){
        //System.out.println(x + " " + y + " " + z);
        return blocks[x][y][z];
    }

    /**
     * MUST be called before getMaxHeight or getMaxBlock after every update of this Volume's contents.
     */
    private void calcMaximums(){
        for(int x = 0; x < VOL_X; x++) {
            for (int z = 0; z < VOL_Z; z++) {
                int y = VOL_Y - 1;
                while(blocks[x][y][z] == 0 && y > 0){
                    y--;
                }
                maximums[x][z] = y;
                maxBlocks[x][z] = blocks[x][y][z];
            }
        }
    }

    public int getMaxHeight(int x, int z){
        return maximums[x][z];
    }

    public int getMaxBlock(int x, int z){
        return maxBlocks[x][z];
    }

    public World getWorld(){
        return w;
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

    public void setX(int x){
        this.x = x;
        swb_x = Space.volXToX(x);
    }

    public void setY(int y){
        this.y = y;
        swb_y = Space.volYToY(y);
    }

    public void setZ(int z){
        this.z = z;
        swb_z = Space.volZToZ(z);
    }
}
