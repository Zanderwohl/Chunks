package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Render.Mesh;
import com.zanderwohl.chunks.Shaders.SimpleShaderProgram;
import com.zanderwohl.util.FileLoader;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL30.*;

/**
 * Handles the rendering of each frame, geometry and shaders.
 */
public class Renderer {

    private SimpleShaderProgram shaderProgram;

    private int vboId;

    private int vaoId;

    /**
     * Initialize the renderer and allocate memory and other resources.
     * Compiles the vertex and fragment shaders.
     * @throws Exception A crash from here means no-go. // TODO: This should be more specific.
     */
    public void init() throws Exception {
        shaderProgram = new SimpleShaderProgram();
        shaderProgram.createVertexShader((new FileLoader("/shaders/vertex.vs", true)).getFile());
        shaderProgram.createFragmentShader((new FileLoader("/shaders/fragment.fs", true)).getFile());
        shaderProgram.link();
    }

    /**
     * Render a frame.
     * @param window The window to render the frame onto.
     */
    public void render(Window window, Mesh mesh){
        clear();

        if(window.isResized()){
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.bind();

        glBindVertexArray(mesh.getVaoId());
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);

        shaderProgram.unbind();
    }

    /**
     * Calls glClear.
     */
    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Clean up resources, closing files and freeing memory.
     */
    public void cleanup(){
        if(shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }

}
