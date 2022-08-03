package com.zanderwohl.chunks.Gamelogic;

import com.zanderwohl.chunks.Block.BlockLibrary;
import com.zanderwohl.chunks.Render.*;
import com.zanderwohl.chunks.Client.*;
import com.zanderwohl.chunks.Entity.Entity;
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
    private Vector3f ambientLight;
    private PointLight[] pointLights;
    private SpotLight[] spotLights;
    private DirectionalLight sun;
    private float sunAngle;
    private float spotAngle = 0;
    private float spotInc = 1;

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

        sunAngle = -90;
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

        float reflectance = 1f;

        World w = new World("test", null, toConsole, blockLibrary);
        w.initialize();
        entities = new Entity[1];

        Mesh mesh = OBJLoader.loadMesh("models/cube.obj");
        Texture texture = blockLibrary.getBlockByName("default","grass").getTextureGL();
        Material material = new Material(texture, reflectance);
        mesh.setMaterial(material);
        Entity e = new Entity(mesh);
        e.setScale(0.5f);
        e.setPosition(0, 0, -2);
        entities[0] = e;

        ambientLight = new Vector3f(1f, 0.5f, 0.5f);
        Vector3f lightColor = new Vector3f(1, 1, 1);
        Vector3f lightPosition = new Vector3f(2, 2, 0);
        float lightIntensity = 10.0f;
        PointLight pointLight = new PointLight(lightColor, lightPosition, lightIntensity);
        PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
        pointLight.setAttenuation(att);
        pointLights = new PointLight[]{ pointLight };

        lightPosition = new Vector3f(-1, 0, 0);
        lightColor = new Vector3f(1, 1, 1);
        sun = new DirectionalLight(lightColor, lightPosition, lightIntensity);

        // Spot Light
        lightPosition = new Vector3f(0, 0.0f, 10f);
        PointLight sl_pointLight = new PointLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
        att = new PointLight.Attenuation(0.0f, 0.0f, 0.02f);
        sl_pointLight.setAttenuation(att);
        Vector3f coneDir = new Vector3f(0, 0, -1);
        float cutoff = (float) Math.cos(Math.toRadians(140));
        SpotLight spotLight = new SpotLight(sl_pointLight, coneDir, cutoff);
        spotLights = new SpotLight[]{ spotLight };
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

        float lightPos = spotLights[0].getPointLight().getPosition().z;
        if (window.isKeyPressed(GLFW_KEY_N)) {
            this.spotLights[0].getPointLight().getPosition().z = lightPos + 0.1f;
        } else if (window.isKeyPressed(GLFW_KEY_M)) {
            this.spotLights[0].getPointLight().getPosition().z = lightPos - 0.1f;
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

        updateSun(deltaT);
    }

    private void updateSun(float _deltaT){
        // Update directional light direction, intensity and colour
        sunAngle += 1.1f;
        if (sunAngle > 90) {
            sun.setIntensity(0);
            if (sunAngle >= 360) {
                sunAngle = -90;
            }
        } else if (sunAngle <= -80 || sunAngle >= 80) {
            float factor = 1 - (float) (Math.abs(sunAngle) - 80) / 10.0f;
            sun.setIntensity(factor);
            sun.getColor().y = Math.max(factor, 0.9f);
            sun.getColor().z = Math.max(factor, 0.5f);
        } else {
            sun.setIntensity(1);
            sun.getColor().x = 1;
            sun.getColor().y = 1;
            sun.getColor().z = 1;
        }
        double angRad = Math.toRadians(sunAngle);
        sun.getDirection().x = (float) Math.sin(angRad);
        sun.getDirection().y = (float) Math.cos(angRad);
    }

    /**
     * Render the game onto the given window.
     * @param window The window.
     */
    @Override
    public void render(Window window) {
        renderer.render(window, camera, entities, ambientLight, pointLights, sun, spotLights);
    }

    /**
     * Cleanup resources used by the game, such as the renderer.
     */
    @Override
    public void cleanup(){
        renderer.cleanup();
    }
}
