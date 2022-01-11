package com.zanderwohl.chunks.Gamelogic;

import com.zanderwohl.chunks.Client.Renderer;
import com.zanderwohl.chunks.Client.Window;
import com.zanderwohl.chunks.Entity.Entity;
import com.zanderwohl.chunks.Render.Mesh;
import org.joml.Vector3f;
import org.lwjgl.system.CallbackI;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * A basic, very primitive game.
 */
public class BadGame implements IGameLogic {

    private int xInc = 0;

    private int yInc = 0;

    private int zInc = 0;

    private int scaleInc = 0;

    private float color = 0.0f;

    private final Renderer renderer;

    private Entity[] entities;

    private Window window;

    /**
     * Sets up the renderer.
     */
    public BadGame(){
        renderer = new Renderer();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);
        float[] positions = new float[]{
                -0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f,
        };
        int[] indices = new int[]{
                0, 1, 3, 3, 1, 2,
        };
        float[] colors = new float[]{
                0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
                0.0f, 0.0f, 0.5f,
                0.0f, 0.5f, 0.5f,
        };
        Mesh mesh = new Mesh(positions, indices, colors);
        Entity entity = new Entity(mesh);
        entity.setPosition(0, 0, -2);
        entities = new Entity[]{ entity };
    }

    /**
     * Handles user input each frame.
     * @param window The window this input function should listen to.
     */
    @Override
    public void input(Window window) {
        xInc = 0;
        yInc = 0;
        zInc = 0;
        scaleInc = 0;
        if(window.isKeyPressed(GLFW_KEY_W)){
            zInc += 1;
        }
        if(window.isKeyPressed(GLFW_KEY_S)){
            zInc -= 1;
        }
        if(window.isKeyPressed(GLFW_KEY_A)){
            xInc += 1;
        }
        if(window.isKeyPressed(GLFW_KEY_D)){
            xInc -= 1;
        }
        if(window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)){
            yInc += 1;
        }
        if(window.isKeyPressed(GLFW_KEY_SPACE)){
            yInc -= 1;
        }
        if(window.isKeyPressed(GLFW_KEY_R)){
            scaleInc += 1;
        }
        if(window.isKeyPressed(GLFW_KEY_F)){
            scaleInc -= 1;
        }
    }

    /**
     * Do updates on game logic.
     * @param deltaT The time since the last logical frame.
     */
    @Override
    public void update(float deltaT) {
        for(Entity entity: entities){
            Vector3f position = entity.getPosition();
            float posX = position.x + xInc * 0.01f;
            float posY = position.y + yInc * 0.01f;
            float posZ = position.z + zInc * 0.01f;
            entity.setPosition(posX, posY, posZ);

            float scale = entity.getScale();
            scale += scaleInc * 0.05f;
            if(scale < 0){
                scale = 0;
            }
            entity.setScale(scale);

            float rotation = entity.getRotation().x + 1.5f;
            if(rotation > 360){
                rotation -= 360;
            }
            entity.setRotation(rotation, 0, 0);

        }
    }

    @Override
    public void render(Window window) {
        // window.setClearColor(color, color, color, 0.0f);
        renderer.render(window, entities);
    }

    @Override
    public void cleanup(){
        renderer.cleanup();
    }
}
