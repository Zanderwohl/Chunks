package com.zanderwohl.chunks.Image;

import com.zanderwohl.chunks.World.Coord;
import com.zanderwohl.chunks.World.World;
import com.zanderwohl.chunks.Block.Block;
import com.zanderwohl.chunks.Block.BlockLibrary;
import com.zanderwohl.chunks.FileConstants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Creates a top-down image of the world.
 */
public class ImageWorld {

    /**
     * Create unwrapped texture maps of each block, and save them.
     * @param bl A block library to provide the blocks.
     */
    public static void unrwapBlocks(BlockLibrary bl){
        for(int i = 1; i < bl.size(); i++){
            saveImage(imageBlock(bl.getBlockById(i)),i + "-map");
        }
    }

    /**
     * Save a BufferedImage to a file, as given by the file name. PNG.
     * @param image The image to save.
     * @param name The file name.
     */
    public static void saveImage(BufferedImage image, String name){
        new File(FileConstants.screenshotFolder + "/").mkdirs();

        File imageFile = new File( FileConstants.screenshotFolder + "/" + name + "_" + getTime() + "." + FileConstants.screenshot);
        try {
            ImageIO.write(image, FileConstants.screenshot, imageFile);
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Make a simple top-down image of a world.
     * @param w The world to image.
     * @return A buffered image of the world.
     */
    public static BufferedImage makeImage(World w){
        BufferedImage image = new BufferedImage(w.getX(),w.getZ(),BufferedImage.TYPE_INT_RGB);

        int highest = 0;
        int lowest = Integer.MAX_VALUE;
        BlockLibrary library = w.getWorldManager().getLibrary();

        for(int x = 0; x < w.getX(); x++){
            for(int z = 0; z < w.getZ(); z++){
                int value = w.getPeak(x, z);
                if(value > highest){
                    highest = value;
                }
                if(value < lowest){
                    lowest = value;
                }

                //double degree = (((double) w.getPeak(x,z) / (double)w.getY())) + 0.5;
                double degree = (Math.sin(((double) w.getPeak(x,z) / (double)w.getY())) + 1.0) / 2.0;
                Color c = new Color(library.getBlockColor(w.getBlockID(new Coord(x, w.getPeak(x,z), z, Coord.Scale.BLOCK))));
                double red = c.getRed() * degree;
                double blue = c.getBlue() * degree;
                double green = c.getGreen() * degree;
                red = Math.min(red, 255.0);
                green = Math.min(green, 255.0);
                blue = Math.min(blue, 255.0);

                //System.out.println(w.getPeak(x,z));
                Color result = new Color((int)red, (int)green, (int)blue);
                image.setRGB(x,z,result.getRGB());
            }
        }

        //System.out.println("Highest: " + highest + "\nLowest: " + lowest + "\nCeiling: " + w.getY());
        return image;
    }

    /**
     * Generate a top-down image of a portion of the world.
     * @param w The world to image.
     * @param center_x The center of the image on the x-axis
     * @param center_z The center of the image on the z-axis
     * @param scale The scale of the image.
     * @param width The pixel-height of the image.
     * @param height The pixel-width of the image.
     * @return The image.
     */
    public static BufferedImage makeImage2(World w, int center_x, int center_z, int scale, int width, int height){
        Bounds bounds = new Bounds(scale, width, height, center_x, center_z);

        System.out.println("scale: " + scale + "\nx: " + bounds.START_X + "->" + bounds.END_X + "\nz: " + bounds.START_Z + "->" + bounds.END_Z);

        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D map = image.createGraphics();

        int highest = 0;
        int lowest = Integer.MAX_VALUE;
        BlockLibrary library = w.getWorldManager().getLibrary();

        for(int x = bounds.START_X; x < bounds.END_X; x++){
            for(int z = bounds.START_Z; z < bounds.END_Z; z++){
                int thisPeak = w.getPeak(x, z);
                Coord c = new Coord(x, thisPeak, z, Coord.Scale.BLOCK);

                Color shade = blockShade(w, c);
                printBlock(w, c, library, map, bounds);
                //shadeBlock(map, shade, bounds, c);

                highest = Math.max(thisPeak, highest);
                lowest = Math.min(thisPeak, lowest);
            }
        }

        System.out.println("Highest: " + highest + "\nLowest: " + lowest + "\nCeiling: " + w.getY());
        return image;
    }

    private static Color blockShade(World w, Coord c){
        int x = c.getX();
        int z = c.getZ();
        int thisPeak = c.getY();

        int northPeak = w.getPeak(x, z - 1);
        int southPeak = w.getPeak(x, z + 1);
        int eastPeak = w.getPeak(x + 1, z);
        int westPeak = w.getPeak(x - 1, z);

        int northDifference = Math.abs(thisPeak - northPeak);
        int southDifference = Math.abs(thisPeak - southPeak);
        int eastDifference = Math.abs(thisPeak - eastPeak);
        int westDifference = Math.abs(thisPeak - westPeak);

        double northEast = northDifference + eastDifference;
        double southWest = southDifference + westDifference;

        double northEastRatio = clamp(northEast / 7.0);
        double southWestRatio = clamp(southWest / 7.0);
        double combinedRatio = clamp(northEastRatio - southWestRatio + .5);

        int transparency = (int)(combinedRatio * 200);
        Color result = new Color(0, 0, 0, transparency);
        return result;
    }

    private static void printBlock(World w, Coord c, BlockLibrary library, Graphics2D map, Bounds bounds){
        int x = c.getX();
        int y = c.getY();
        int z = c.getZ();

        int blockID = w.getBlockID(new Coord(x, y, z, Coord.Scale.BLOCK));
        Block b = library.getBlockById(blockID);
        map.drawImage(b.getTexture(Block.SIDE.TOP), x * bounds.SCALE + (int)bounds.HALF_WIDTH, z * bounds.SCALE + (int) bounds.HALF_HEIGHT,
                bounds.SCALE, bounds.SCALE,null);
    }

    private static void shadeBlock(Graphics2D map, Color shadeColor, Bounds bounds, Coord c){
        map.setColor(shadeColor);
        map.fillRect(c.getX()  * bounds.SCALE + (int)bounds.HALF_WIDTH, c.getZ() * bounds.SCALE + (int) bounds.HALF_HEIGHT, bounds.SCALE, bounds.SCALE);
    }

    /**
     * Create an unwrapped image of a block's sides.
     * @param b The block to image.
     * @return The unwrapped texture map.
     */
    public static BufferedImage imageBlock(Block b){
        BufferedImage image = new BufferedImage(16*4, 16*3, BufferedImage.TYPE_INT_RGB);
        Graphics2D map = image.createGraphics();

        map.drawImage(b.getTexture(0), 16, 0, null);
        map.drawImage(b.getTexture(1), 16*1, 16, null);
        map.drawImage(b.getTexture(2), 16*0, 16, null);
        map.drawImage(b.getTexture(3), 16*2, 16, null);
        map.drawImage(b.getTexture(4), 16*3, 16, null);
        map.drawImage(b.getTexture(5), 16, 16*2, null);

        return image;
    }

    /**
     * Get the current time as an ISO-style string that can be included in a file name.
     * @return The current time string.
     */
    public static String getTime(){
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
    }

    /**
     * A [0.0, 1.0]-ranged clamp function.
     * @param input The unclamped value.
     * @return The value, but clamped.
     */
    private static double clamp(double input){
        return Math.max(0.0, Math.min(1.0, input));
    }

    private static class Bounds{
        public final int SCALE;
        public final int WIDTH;
        public final int HEIGHT;
        public final double HALF_WIDTH;
        public final double HALF_HEIGHT;
        public final double WIDTH_IN_BLOCKS;
        public final double HEIGHT_IN_BLOCKS;

        public final int CENTER_X;
        public final int CENTER_Z;
        public final int START_X;
        public final int END_X;
        public final int START_Z;
        public final int END_Z;


        public Bounds(int scale, int width, int height, int center_x, int center_z){
            WIDTH = width;
            HEIGHT = height;
            HALF_WIDTH = width / 2.0;
            HALF_HEIGHT = height / 2.0;
            WIDTH_IN_BLOCKS = HALF_WIDTH / scale;
            HEIGHT_IN_BLOCKS = HALF_HEIGHT / scale;
            SCALE = scale;

            CENTER_X = center_x;
            CENTER_Z = center_z;
            START_X = center_x - (int)Math.ceil(WIDTH_IN_BLOCKS);
            END_X = center_x + (int)Math.ceil(WIDTH_IN_BLOCKS);
            START_Z = center_z - (int)Math.ceil(HEIGHT_IN_BLOCKS);
            END_Z = center_z + (int)Math.ceil(HEIGHT_IN_BLOCKS);
        }
    }
}
