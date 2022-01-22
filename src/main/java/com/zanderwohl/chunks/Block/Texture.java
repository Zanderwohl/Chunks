package com.zanderwohl.chunks.Block;

import com.zanderwohl.console.Message;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

/**
 * Object that loads and manages a texture, placing it in graphics memory.
 */
public class Texture {

    private final int id;

    /**
     * Default constructor. Takes in a file name. Loads texture into memory.
     * @param fileName The file to containing the texture.
     * @param toConsole Messages to console.
     */
    public Texture(String fileName, ArrayBlockingQueue<Message> toConsole) {
        this(loadTexture(fileName, toConsole));
    }

    /**
     * Inner constructor that keeps track of ID.
     * @param id ID given by GL calls.
     */
    private Texture(int id){
        this.id = id;
    }

    /**
     * Bind this texture.
     */
    public void bind(){
        glBindTexture(GL_TEXTURE_2D, id);
    }

    /**
     * Load the texture into memory with the GL stuff.
     * @param fileName The file to load.
     * @param toConsole Message stream to console.
     * @return The ID of the texture in GL.
     */
    private static int loadTexture(String fileName, ArrayBlockingQueue<Message> toConsole) {
        int width;
        int height;
        ByteBuffer buffer;

        try(MemoryStack stack = MemoryStack.stackPush()){
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            buffer = stbi_load(fileName, w, h, channels, 4);
            if(buffer == null){
                toConsole.add(new Message("source=Texture\nseverity=warning\nmessage=" + stbi_failure_reason()));
            }

            width = w.get();
            height = h.get();

            int textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);

            // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

            //glGenerateMipmap(GL_TEXTURE_2D);

            stbi_image_free(buffer);

            return textureId;
        }
    }

    /**
     * Get the texture's ID
     * @return ID of texture as given by GL.
     */
    public int getId(){
        return id;
    }

    /**
     * Delete the texture and clean up.
     */
    public void cleanup(){
        glDeleteTextures(id);
    }
}
