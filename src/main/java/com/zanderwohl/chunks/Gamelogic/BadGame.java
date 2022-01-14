package com.zanderwohl.chunks.Gamelogic;

import com.zanderwohl.chunks.Block.Block;
import com.zanderwohl.chunks.Block.BlockLibrary;
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

    private BlockLibrary blockLibrary;

    /**
     * Sets up the renderer.
     */
    public BadGame(){
        renderer = new Renderer();
    }

    @Override
    public void setBlockLibrary(BlockLibrary blockLibrary){
        this.blockLibrary = blockLibrary;
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);
        blockLibrary.loadGLTextures();
        float[] positions = new float[] {
                // V0
                -0.5f, 0.5f, 0.5f,
                // V1
                -0.5f, -0.5f, 0.5f,
                // V2
                0.5f, -0.5f, 0.5f,
                // V3
                0.5f, 0.5f, 0.5f,
                // V4
                -0.5f, 0.5f, -0.5f,
                // V5
                0.5f, 0.5f, -0.5f,
                // V6
                -0.5f, -0.5f, -0.5f,
                // V7
                0.5f, -0.5f, -0.5f,

                // For text coords in top face
                // V8: V4 repeated
                -0.5f, 0.5f, -0.5f,
                // V9: V5 repeated
                0.5f, 0.5f, -0.5f,
                // V10: V0 repeated
                -0.5f, 0.5f, 0.5f,
                // V11: V3 repeated
                0.5f, 0.5f, 0.5f,

                // For text coords in right face
                // V12: V3 repeated
                0.5f, 0.5f, 0.5f,
                // V13: V2 repeated
                0.5f, -0.5f, 0.5f,
                // V14: V5 repeated
                0.5f, 0.5f, -0.5f,
                // V15: V7 repeated
                0.5f, -0.5f, -0.5f,

                // For text coords in left face
                // V16: V0 repeated
                -0.5f, 0.5f, 0.5f,
                // V17: V1 repeated
                -0.5f, -0.5f, 0.5f,

                // For text coords in bottom face
                // V18: V6 repeated
                -0.5f, -0.5f, -0.5f,
                // V19: V7 repeated
                0.5f, -0.5f, -0.5f,
                // V20: V1 repeated
                -0.5f, -0.5f, 0.5f,
                // V21: V2 repeated
                0.5f, -0.5f, 0.5f,

                // V22: V4 repeated
                -0.5f, 0.5f, -0.5f,
                // V23: V5 repeated
                0.5f, 0.5f, -0.5f,
                // V24: V6 repeated
                -0.5f, -0.5f, -0.5f,
                // V25: V7 repeated
                0.5f, -0.5f, -0.5f,
        };
        float sixth = 1.0f / 6.0f;
        float[] textCoords = new float[]{
                1 * sixth, 0.0f,
                1 * sixth, 1.0f,
                2 * sixth, 1.0f,
                2 * sixth, 0.0f,

                2 * sixth, 0.0f,
                3 * sixth, 0.0f,
                2 * sixth, 1.0f,
                3 * sixth, 1.0f,

                // For text coords in top face
                0.0f, 0.0f,
                sixth, 0.0f,
                0.0f, 1.0f,
                sixth, 1.0f,

                // For text coords in right face
                3 * sixth, 0.0f,
                3 * sixth, 1.0f,
                4 * sixth, 0.0f,
                4 * sixth, 1.0f,

                // For text coords in left face
                3 * sixth, 0.0f,
                3 * sixth, 1.0f,

                // For text coords in bottom face
                5 * sixth, 1.0f,
                6 * sixth, 1.0f,
                5 * sixth, 0.0f,
                6 * sixth, 0.0f,
                /*
                5 * sixth, 0.0f,
                5 * sixth, 1.0f,
                6 * sixth, 0.0f,
                6 * sixth, 1.0f,
                 */

                // Text coords in back face
                5 * sixth, 0.0f,
                4 * sixth, 0.0f,
                5 * sixth, 1.0f,
                4 * sixth, 1.0f,
        };
        int[] indices = new int[]{
                // Front face
                0, 1, 3, 3, 1, 2,
                // Top Face
                8, 10, 11, 9, 8, 11,
                // Right face
                12, 13, 15, 14, 12, 15,
                // Left face
                16, 17, 6, 4, 16, 6,
                // Bottom face
                18, 20, 21, 19, 18, 21,
                // Back face
                22, 23, 24, 24, 23, 25};
        Mesh mesh = new Mesh(positions, indices, textCoords, blockLibrary.getBlockById(blockLibrary.getIdByName("debug", "grass")).getTextureGL());
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

            float rotation = entity.getRotation().y + 0.5f;
            if(rotation > 360){
                rotation -= 360;
            }
            entity.setRotation(0.0f, rotation, 0.0f);

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
