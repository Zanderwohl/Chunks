package com.zanderwohl.chunks.Image;

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

    public static void main(String[] args){
        World w;
        w = new World("image_test");
        w.prepare();
        w.addDomain("color");
        w.initialize();
        BufferedImage image = ImageWorld.makeImage(w);
        ImageWorld.saveImage(image, w.getName());
        BlockLibrary b = w.getLibrary();
        unrwapBlocks(b);
    }

    public static void unrwapBlocks(BlockLibrary bl){
        for(int i = 1; i < bl.size(); i++){
            saveImage(imageBlock(bl.getBlockById(i)),i + "-map");
        }
    }

    public static void saveImage(BufferedImage image, String name){
        new File(FileConstants.screenshotFolder + "/").mkdirs();

        File imageFile = new File( FileConstants.screenshotFolder + "/" + name + "_" + getTime() + "." + FileConstants.screenshot);
        try {
            ImageIO.write(image, FileConstants.screenshot, imageFile);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static BufferedImage makeImage(World w){
        BufferedImage image = new BufferedImage(w.getX(),w.getZ(),BufferedImage.TYPE_INT_RGB);

        int highest = 0;
        int lowest = Integer.MAX_VALUE;
        BlockLibrary library = w.getLibrary();

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
                Color c = new Color(library.getBlockColor(w.getBlock(x, w.getPeak(x,z), z)));
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

    public static BufferedImage makeImage2(World w, int center_x, int center_z, int scale, int width, int height){
        double halfWidth = width / 2.0;
        double halfHeight = height / 2.0;
        double widthBlocks = halfWidth / scale;
        double heightBlocks = halfHeight / scale;

        int startX = center_x - (int)Math.ceil(widthBlocks);
        int endX = center_x + (int)Math.ceil(widthBlocks);
        int startZ = center_z - (int)Math.ceil(heightBlocks);
        int endZ = center_z + (int)Math.ceil(heightBlocks);

        System.out.println("scale: " + scale + "\nx: " + startX + "->" + endX + "\nz: " + startZ + "->" + endZ);

        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D map = image.createGraphics();

        int highest = 0;
        int lowest = Integer.MAX_VALUE;
        BlockLibrary library = w.getLibrary();

        for(int x = startX; x < endX; x++){
            for(int z = startZ; z < endZ; z++){
                int value = w.getPeak(x, z);
                if(value > highest){
                    highest = value;
                }
                if(value < lowest){
                    lowest = value;
                }

                int thisPeak = w.getPeak(x, z);
                int otherPeak = w.getPeak(x, z - 1);
                int otherOtherPeak = w.getPeak(x, z + 1);
                int difference = thisPeak - otherPeak - otherOtherPeak;
                double degree;

                //uses both difference with north block and height to calculate color.
                //double degree1 = 1.0 - ((difference + 10.0) / 20.0);
                double degree1 = 0.5 + (1.0/20.0) * difference;
                //double degree2 = (double)thisPeak / (double)w.getY();
                //double degree2 = (thisPeak % 20) / 20.0;
                double degree2 = (Math.sin(thisPeak / 10.0) / 2.0) + 0.5;
                if(degree1 < 0){
                    degree1 = 0;
                }
                if(degree1 > 1){
                    degree1 = 1;
                }
                degree = degree1 * 1.0 + degree2 * 0.0;

                int transparency = (int)(degree * 200);
                //System.out.println(transparency);
                //Color result = new Color((int)red, (int)green, (int)blue,transparency);//weird effect tho.
                Color result = new Color(0, 0, 0,transparency);

                System.out.println(x + " " + z);
                int blockID = w.getBlock(x, thisPeak, z);
                Block b = library.getBlockById(blockID);
                map.drawImage(b.getTexture(0), x, z, scale, scale,null);
                //map.setColor(result);
                //map.fillRect(x, z, scale, scale);
            }
        }

        System.out.println("Highest: " + highest + "\nLowest: " + lowest + "\nCeiling: " + w.getY());
        return image;
    }

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

    public static String getTime(){
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
    }
}
