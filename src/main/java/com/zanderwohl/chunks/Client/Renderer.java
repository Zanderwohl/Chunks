package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Entity.Entity;
import com.zanderwohl.chunks.Entity.Transformation;
import com.zanderwohl.chunks.Shaders.SimpleShaderProgram;
import com.zanderwohl.util.FileLoader;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.glBufferData;

/**
 * Handles the rendering of each frame, geometry and shaders.
 */
public class Renderer {

    private SimpleShaderProgram shaderProgram;

    private int vboId;

    private int vaoId;

    private static final float FOV = (float) Math.toRadians(90.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.0f;

    private Matrix4f projectionMatrix;

    private final Transformation transformation;

    public Renderer(){
        transformation = new Transformation();
    }

    /**
     * Initialize the renderer and allocate memory and other resources.
     * Compiles the vertex and fragment shaders.
     * @throws Exception A crash from here means no-go. // TODO: This should be more specific.
     */
    public void init(Window window) throws Exception {
        shaderProgram = new SimpleShaderProgram();
        shaderProgram.createVertexShader((new FileLoader("/shaders/vertex.vs", true)).getFile());
        shaderProgram.createFragmentShader((new FileLoader("/shaders/fragment.fs", true)).getFile());
        shaderProgram.link();

        float aspectRatio = (float) window.getWidth() / window.getHeight();
        projectionMatrix = new Matrix4f().setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("worldMatrix");

        shaderProgram.createUniform("texture_sampler");

        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Render a frame.
     * @param window The window to render the frame onto.
     */
    public void render(Window window, Entity[] entities){
        clear();

        if(window.isResized()){
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.bind();

        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(),
                Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        shaderProgram.setUniform("texture_sampler", 0);
        for(Entity entity: entities){
            // Set world matrix for this item
            Matrix4f worldMatrix = transformation.getWorldMatrix(
                    entity.getPosition(),
                    entity.getRotation(),
                    entity.getScale());
            shaderProgram.setUniform("worldMatrix", worldMatrix);
            entity.getMesh().render();
        }

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
