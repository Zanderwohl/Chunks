package com.zanderwohl.Block;

import Utils.FileLoader;
import org.json.JSONObject;
import com.zanderwohl.FileConstants;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * An object containing all the information about a block required by the game.
 * A class that contains information about a single type of block, including textures.
 *
 * Blocks are not hard-coded. Rather, they are loaded in from jason files found in that block's domain folder
 * (e.g. "default" or "colors") in the folder "/[domain]/blocks/". A block existing in that folder will not add it into the
 * game. It must also be listed in the domain.json file found at the root of that domain's folder.
 */
public class Block {

    private String name;
    private String domain;
    private int id;
    private Color color;

    private static final int sides = 6; //cubes generally have six sides, but I'm open to higher dimension ports.
    private BufferedImage[] textures = new BufferedImage[sides]; //top, front, left, right, back, bottom

    /**
     * Creates a very simplistic block that has a color but no textures.
     * @param id The id of this block. Should not be already taken.
     * @param name The name of this block. A string with no spaces. Ideally all lowercase words separated by
     *             underscores.
     * @param color The color of the block for use in maps.
     */
    public Block(int id, String name, Color color){
        this.name = name;
        this.id = id;
        this.color = color;
        domain = "internal";
    }

    /**
     * Creates a block based on a json block description. Information on this is provided in the README.md for this
     * folder. Constructor receives this information from a json file whose name is supplied as the argument.
     * @param path The name of the json file as found in the /[domain]/blocks folder. Format like "[blockName].json".
     * @param domain The name of the domain this block is from.
     */
    public Block(String path, String domain){
        this.domain = domain;
        String jsonString;
        JSONObject json = new JSONObject();
        try {
            FileLoader blockFile = new FileLoader(path);
            jsonString = blockFile.fileToString();
            json = new JSONObject(jsonString);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }
        name = json.getString("name");
        int r = json.getJSONObject("color").getInt("r");
        int g = json.getJSONObject("color").getInt("g");
        int b = json.getJSONObject("color").getInt("b");
        //System.out.println(r + " " + g + " " + b);
        color = new Color(r, g, b);
        loadTextures(json);
        System.out.println("\tAdded block " + getFullName() + "!");
    }

    /**
     * Load into memory all the textures this block uses.
     * @param json The json object that contains the information about texture locations. Format can be found in this
     *             folder's README.md.
     */
    private void loadTextures(JSONObject json){
        String[] fileURLs = new String[sides];
        if(json.get("texture") instanceof JSONObject){
            fileURLs = readMultisided(json);
        } else {
            String fileName = json.getString("texture");
            for(int i = 0; i < textures.length; i++){
                fileURLs[i] = fileName;
            }
        }
        for(int i = 0; i < textures.length; i++){
            textures[i] = readImage(fileURLs[i]);
        }
    }

    /**
     * Attempt to load in a texture file based on a file name.
     * @param fileName The name of the file, as found in "/[domainName]/textures/".
     * @return The texture, unless the file cannot be found. Then it will print an error and return null.
     */
    private BufferedImage readImage(String fileName){
        try {
            File file = new File(FileConstants.domainFolder + "/" + domain + "/" + FileConstants.textureFolder + "/" + fileName);
            //System.out.println(file.toString());
            return ImageIO.read(file);
        } catch (MalformedURLException e){
            System.err.println(fileName + ", a specified texture for the block \"" + getFullName() + "\" does not exist, or is named wrongly.");
        } catch (IOException e){
            System.err.println("Cannot find file " + fileName + "!");
        }
        return null;
    }

    /**
     * Helper function to read a list of textures for each side.
     * @param json The value (all of it) of a multisided block's texture list.
     * @return A list of URLS.
     */
    private String[] readMultisided(JSONObject json){
        String[] fileURLs = new String[sides];
        JSONObject textureList = json.getJSONObject("texture");
        for(int i = 0; i < textures.length; i++){
            fileURLs[i] = textureList.getString("top");
        }
        if(textureList.has("sides")){
            fileURLs[1] = textureList.getString("sides");
            fileURLs[2] = textureList.getString("sides");
            fileURLs[3] = textureList.getString("sides");
            fileURLs[4] = textureList.getString("sides");
        }
        if(textureList.has("front")) {
            fileURLs[1] = textureList.getString("front");
        }
        if(textureList.has("left")) {
            fileURLs[2] = textureList.getString("left");
        }
        if(textureList.has("right")) {
            fileURLs[3] = textureList.getString("right");
        }
        if(textureList.has("back")) {
            fileURLs[4] = textureList.getString("back");
        }
        if(textureList.has("bottom")){
            fileURLs[5] = textureList.getString("bottom");
        }
        return fileURLs;
    }

    /**
     * Gets an RGB version of the block's color.
     * @return The RGB value.
     */
    public int getRGB(){
        return color.getRGB();
    }

    /**
     * Gets this block's id, in this instance of the game.
     * @return The block's id. There is nothing more to say.
     */
    public int getId(){
        return id;
    }

    /**
     * Sets the block's id.
     * @param newId
     */
    protected void setID(int newId){
        this.id = newId;
    }

    /**
     * Gets the name of this block, without the domain.
     * @return The name of this block.
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the domain of this block.
     * @return The name of the domain.
     */
    public String getDomain(){
        return domain;
    }

    /**
     * Gets the name of the block with the domain.
     * @return A String of the format "[domainName]:[blockName]".
     */
    public String getFullName(){
        return getDomain() + ":" + getName();
    }

    /**
     * Gets the texture from a side. The sides are in this order:
     * 0: Top, 1: Front, 2: Left, 3: Right, 4: Back, 5: Bottom.
     * @param side Integer of a side number. Which are which?
     * @return A BufferedImage of that texture.
     */
    public BufferedImage getTexture(int side){
        return textures[side];
    }
}
