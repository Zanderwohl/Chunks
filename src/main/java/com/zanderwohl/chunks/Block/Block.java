package com.zanderwohl.chunks.Block;

import com.zanderwohl.console.Message;
import com.zanderwohl.util.FileLoader;
import org.json.JSONObject;
import com.zanderwohl.chunks.FileConstants;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * An object containing all the information about a block required by the game.
 * A class that contains information about a single type of block, including textures.
 *
 * Blocks are not hard-coded. Rather, they are loaded in from jason files found in that block's domain folder
 * (e.g. "default" or "colors") in the folder "/[domain]/blocks/". A block existing in that folder will not add it into the
 * game. It must also be listed in the domain.json file found at the root of that domain's folder.
 */
public class Block {

    /**
     * The sides of the blocks are each represented by integers.
     * This enum makes it easy to remember which is which.
     */
    public enum SIDE{
        TOP(0),
        FRONT(1),
        LEFT(2),
        RIGHT(3),
        BACK(4),
        BOTTOM(5);

        private int val;

        SIDE(int val){
            this.val = val;
        }

        public int getVal(){
            return val;
        }
    }

    /* The name of this block. */
    private final String name;
    /* The domain this belongs to. */
    private final String domain;
    /* Internal id of this block. Changes based on what domains have been added. Generated on runtime. */
    private int id;
    /* simple color used for debugging and mapping purposes. */
    private final Color color;

    private JSONObject blockJSON;

    private static final int sides = 6; //cubes generally have six sides, but I'm open to higher dimension ports.
    private final BufferedImage[] textures = new BufferedImage[sides]; //top, front, left, right, back, bottom
    private final Texture[] textures_ = new Texture[sides];

    private final ArrayBlockingQueue<Message> toConsole;

    /**
     * Creates a very simplistic block that has a color but no textures.
     * @param id The id of this block. Should not be already taken.
     * @param name The name of this block. A string with no spaces. Ideally all lowercase words separated by
     *             underscores.
     * @param toConsole The queue to send messages to the console.
     * @param color The color of the block for use in maps.
     */
    public Block(int id, String name, ArrayBlockingQueue<Message> toConsole, Color color){
        this.name = name;
        this.id = id;
        this.color = color;
        domain = "internal";
        this.toConsole = toConsole;
    }

    /**
     * Creates a block based on a json block description. Information on this is provided in the README.md for this
     * folder. Constructor receives this information from a json file whose name is supplied as the argument.
     * @param path The name of the json file as found in the /[domain]/blocks folder. Format like "[blockName].json".
     * @param domain The name of the domain this block is from.
     * @param toConsole The queue to send messages to the console.
     */
    public Block(String path, String domain, ArrayBlockingQueue<Message> toConsole) throws BlockException {
        this.domain = domain;
        this.toConsole = toConsole;
        String jsonString;
        JSONObject json;
        try {
            FileLoader blockFile = new FileLoader(path);
            jsonString = blockFile.getFile();
            json = new JSONObject(jsonString);
        } catch (FileNotFoundException e){
            toConsole.add(new Message("source=Block\nseverity=warning\nmessage=File '"
            + path + "' not found! Details: " + e.getMessage()));
            throw new BlockException("Block could not be created.");
            //e.printStackTrace();
        } catch (IOException e) {
            toConsole.add(new Message("source=Block\nseverity=warning\nmessage=Loading block from file '"
                    + path + "' failed! Details: " + e.getMessage()));
            throw new BlockException("Block could not be created.");
            //ioException.printStackTrace();
        }
        blockJSON = json;
        name = json.getString("name");
        int r = json.getJSONObject("color").getInt("r");
        int g = json.getJSONObject("color").getInt("g");
        int b = json.getJSONObject("color").getInt("b");
        color = new Color(r, g, b);
        loadTextures(json);
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

    public void loadTextures_(){
        if(blockJSON != null) {
            loadTextures_(blockJSON);
        }
    }

    private void loadTextures_(JSONObject json){
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
            textures_[i] = new Texture(fullFilePath(fileURLs[i]), toConsole);
        }
    }

    /**
     * Attempt to load in a texture file based on a file name.
     * @param fileName The name of the file, as found in "/[domainName]/textures/".
     * @return The texture, unless the file cannot be found. Then it will print an error and return null.
     */
    private BufferedImage readImage(String fileName){
        try {
            File file = new File(fullFilePath(fileName));
            return ImageIO.read(file);
        } catch (MalformedURLException e){
            toConsole.add(new Message("message=" + fileName + ", a specified texture for the block \""
                    + getFullName() + "\" does not exist, or is named wrongly.\nsource=Block " + name
                    + "\nseverity=critical"));
        } catch (IOException e){
            toConsole.add(new Message("message=Cannot find file \" + fileName + \"!\nsource=Block " + name
                    + "\nseverity=critical"));

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
     * @param newId The id to set the block to.
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

    /**
     * Gets the texture from a side. Uses the SIDES enum.
     * @param side The side to get.
     * @return A BufferedImage of that texture.
     */
    public BufferedImage getTexture(SIDE side){
        return getTexture(side.getVal());
    }

    public void cleanup(){

    }

    /**
     * Error message for when blocks go wrong.
     */
    public class BlockException extends RuntimeException {

        /**
         * Default constructor.
         * @param errorMessage Any details.
         */
        public BlockException(String errorMessage) {
            super(errorMessage);
        }
    }

    private String fullFilePath(String fileName){
        return FileConstants.domainFolder + "/" + domain + "/" + FileConstants.textureFolder + "/" + fileName;
    }
}
