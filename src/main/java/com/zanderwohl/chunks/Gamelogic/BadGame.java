package com.zanderwohl.chunks.Gamelogic;

import com.zanderwohl.chunks.Block.BlockLibrary;
import com.zanderwohl.chunks.Block.Texture;
import com.zanderwohl.chunks.Client.*;
import com.zanderwohl.chunks.Entity.Entity;
import com.zanderwohl.chunks.Render.Mesh;
import com.zanderwohl.chunks.Render.OBJLoader;
import com.zanderwohl.chunks.World.World;
import com.zanderwohl.console.Message;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.concurrent.ArrayBlockingQueue;

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

    ArrayBlockingQueue<Message> toConsole;

    /**
     * Sets up the renderer.
     */
    public BadGame(ArrayBlockingQueue<Message> toConsole){
        renderer = new Renderer();
        camera = new PlayerCamera();
        cameraInc = new Vector3f();
        this.toConsole = toConsole;
    }

    @Override
    public void setBlockLibrary(BlockLibrary blockLibrary){
        this.blockLibrary = blockLibrary;
    }

    /**
     * Initialize a game. Set up a game world, load textures, etc.
     * @param window The window the game will run in.
     * @throws Exception Probably a graphics error.
     */
    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);
        blockLibrary.loadGLTextures();

        World w = new World("test", null, toConsole, blockLibrary);
        w.initialize();
        entities = new Entity[1];

        Mesh mesh = OBJLoader.loadMesh("models/bunny.obj");
        Texture texture = blockLibrary.getBlockByName("default","grass").getTextureGL();
        //mesh.setTexture(texture);
        Entity e = new Entity(mesh);
        e.setScale(0.5f);
        e.setPosition(0, 0, -2);
        entities[0] = e;
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

    /**
     * Render the game onto the given window.
     * @param window The window.
     */
    @Override
    public void render(Window window) {
        // window.setClearColor(color, color, color, 0.0f);
        renderer.render(window, camera, entities);
    }

    /**
     * Cleanup resources used by the game, such as the renderer.
     */
    @Override
    public void cleanup(){
        renderer.cleanup();
    }
}
