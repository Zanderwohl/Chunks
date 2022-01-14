package com.zanderwohl.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageUtils {

    public static BufferedImage stitchImages(BufferedImage[] textures){
        // TODO: Assumes all images are the same size;
        int width = textures[0].getWidth() * textures.length;
        int height = textures[0].getHeight();

        BufferedImage atlas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D atlasCanvas = atlas.createGraphics();
        atlasCanvas.setPaint(Color.PINK); // Holes should be obvious
        atlasCanvas.fillRect(0, 0, width, height);

        for(int i = 0; i < textures.length; i++){
            BufferedImage image = textures[i];
            atlasCanvas.drawImage(image, null, i * textures[0].getWidth(), 0);
        }
        atlasCanvas.dispose();
        return atlas;
    }
}
