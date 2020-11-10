package com.zanderwohl.chunks.Block;

import com.zanderwohl.console.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import com.zanderwohl.chunks.FileConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An object that manages all the blocks in particular world. It is responsible for assigning block ids to each block.
 *
 * Receives blocks through the addition of a domain. Takes all blocks from the domain.json file and loads their properties
 * by loading the json files of each block.
 *
 * It also manages multiple domains and can find block ids by name and vice-versa.
 */
public class BlockLibrary {

    private ArrayList<Block> list = new ArrayList<Block>();

    private ArrayBlockingQueue<Message> toConsole;

    /**
     * Creates a block library, by default blank except for containing air at id 0.
     * @param toConsole The queue of Messages to the Console.
     */
    public BlockLibrary(ArrayBlockingQueue<Message> toConsole){
        list.add(new Block(0, "air", toConsole, new Color(222, 53, 191))); //ALWAYS add air. ALWAYS.
        this.toConsole = toConsole;
    }

    /**
     * Adds a "domain" - a set of blocks and textures that can be added to a world.
     * Each domain has a name, and each block is referenced by the format "domainName:blockName",
     * which is a consistent key for that block. Block ids may change world to world, depending on which domains
     * exist in that world. References to a particular block should ALWAYS be domain:block, as number ids may change.
     * @param domain A json object that contains all domain information. See README.
     */
    public void addDomain(JSONObject domain){
        toConsole.add(new Message("message=Adding domain '" + domain.getString("name") + "'.\n"
                + "source=Block Library"));
        JSONArray blocks = domain.getJSONArray("blocks");
        for(int i = 0; i < blocks.length(); i++){
            String domainName = domain.getString("name");
            addBlock(FileConstants.domainFolder + "/" + domainName + "/" + FileConstants.blockFolder + "/"
                    + blocks.getString(i) + "." + FileConstants.block, domainName);
        }
        toConsole.add(new Message("message=Added domain '" + domain.getString("name") + "'.\n"
                + "source=Block Library"));
    }

    /**
     * Adds a block to the library, and gives it an id. Note that a block cannot belong to two libraries.
     * @param path Filename of that block, relative to the root of the "[domain]/blocks/" folder.
     * @param domainName The name of the domain this block belongs to.
     */
    public void addBlock(String path, String domainName){
        toConsole.add(new Message("message=Adding block from " + path + "!\nsource=Block Library"));
        Block block = new Block(path, domainName, toConsole);
        block.setID(list.size());
        list.add(block);
    }

    /**
     * Constructs a BlockLibrary from an existing save file.
     * Needs to be written.
     * @param saveFile The save file to load the block library from.
     * @param toConsole The queue of Messages to the Console.
     * @return A block library loaded from the file.
     */
    public static BlockLibrary load(String saveFile, ArrayBlockingQueue<Message> toConsole){
        BlockLibrary library = new BlockLibrary(toConsole);

        return library;
    }

    /**
     * Gets a block's name based on its id.
     * @param id The id of the block in question.
     * @return The name with domain.
     */
    public String getNameById(int id){
        if(id >= list.size()){
            return "";
        }
        return list.get(id).getFullName();
    }

    /**
     * Get the id, given a domain and name.
     * @param domain The domain.
     * @param name The name.
     * @return The id, if it exists, or 0, the id of air if it does not.
     */
    public int getIdByName(String domain, String name){
        int id = 0;
        for(Block block: list){
            if(block.getName().equals(name) && block.getDomain().equals(domain)){
                return block.getId();
            }
        }
        return 0;
    }

    /**
     * Get the color of the block by id.
     * @param id The id.
     * @return The color of the block as an RGB integer.
     */
    public int getBlockColor(int id){
        if(id >= list.size()){
            return Color.BLACK.getRGB();
        }
        return list.get(id).getRGB();
    }

    /**
     * Get the number of items in the library.
     * @return The size of the library.
     */
    public int size(){
        return list.size();
    }

    /**
     * Gets the Block object by its id.
     * @param id The id.
     * @return The Block.
     */
    public Block getBlockById(int id){
        return list.get(id);
    }
}
