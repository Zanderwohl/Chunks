package com.zanderwohl.chunks.Gamelogic;

import com.zanderwohl.chunks.Block.BlockLibrary;
import com.zanderwohl.chunks.Block.Texture;
import com.zanderwohl.chunks.Client.*;
import com.zanderwohl.chunks.Entity.Entity;
import com.zanderwohl.chunks.Render.Mesh;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

/**
 * A basic, very primitive game.
 */
public class BadGame implements IGameLogic {

    private final Renderer renderer;

    private Entity[] entities;

    private Window window;

    private BlockLibrary blockLibrary;

    private ICamera camera;
    private final Vector3f cameraInc;
    private final float SPEED_WALK = .05f;
    private final float SPEED_SPRINT = .15f;
    private float currentSpeed;

    public static final float MOUSE_SENSITIVITY = 0.2f;

    /**
     * Sets up the renderer.
     */
    public BadGame(){
        renderer = new Renderer();
        camera = new PlayerCamera();
        cameraInc = new Vector3f();
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
        ArrayList<Mesh> meshes = new ArrayList<>();
        {
            int i = 1;
            while (blockLibrary.getBlockById(i) != null) {
                Texture t = blockLibrary.getBlockById(i).getTextureGL();
                meshes.add(new Mesh(positions, indices, textCoords, t));
                i++;
            }
        }
        int n = meshes.size();
        entities = new Entity[(n * (n+1))/2];
        int total = 0;
        for(int x = 0; x < meshes.size(); x++){
            for(int y = 0; y < x + 1; y++) {
                entities[total] = new Entity(meshes.get(x));
                entities[total].setPosition(x, y, -2);
                total++;
            }
        }
    }

    /**
     * Handles user input each frame.
     * @param window The window this input function should listen to.
     */
    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0);
        currentSpeed = SPEED_WALK;
        if(window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)){
            currentSpeed = SPEED_SPRINT;
        }
        if(window.isKeyPressed(GLFW_KEY_W)){
            cameraInc.z -= 1;
        }
        if(window.isKeyPressed(GLFW_KEY_S)){
            cameraInc.z += 1;
        }
        if(window.isKeyPressed(GLFW_KEY_A)){
            cameraInc.x -= 1;
        }
        if(window.isKeyPressed(GLFW_KEY_D)){
            cameraInc.x += 1;
        }
        if(window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)){
            cameraInc.y -= 1;
        }
        if(window.isKeyPressed(GLFW_KEY_SPACE)){
            cameraInc.y += 1;
        }
    }

    /**
     * Do updates on game logic.
     * @param deltaT The time since the last logical frame.
     */
    @Override
    public void update(float deltaT, MouseInput mouseInput) {
        camera.movePosition(
                cameraInc.x * currentSpeed,
                cameraInc.y * currentSpeed,
                cameraInc.z * currentSpeed
        );

        if(mouseInput.isRightButtonPressed()){
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }
    }

    @Override
    public void render(Window window) {
        // window.setClearColor(color, color, color, 0.0f);
        renderer.render(window, camera, entities);
    }

    @Override
    public void cleanup(){
        renderer.cleanup();
    }
}
