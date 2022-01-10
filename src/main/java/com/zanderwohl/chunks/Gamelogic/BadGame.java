package com.zanderwohl.chunks.Gamelogic;

import com.zanderwohl.chunks.Client.Renderer;
import com.zanderwohl.chunks.Client.Window;
import com.zanderwohl.chunks.Render.Mesh;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * A basic, very primitive game.
 */
public class BadGame implements IGameLogic {

    private int direction = 0;

    private float color = 0.0f;

    private final Renderer renderer;

    private Mesh mesh;

    /**
     * Sets up the renderer.
     */
    public BadGame(){
        renderer = new Renderer();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init();
        float[] positions = new float[]{
                -0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f, };
        int[] indices = new int[]{
                0, 1, 3, 3, 1, 2,};
        mesh = new Mesh(positions, indices);
    }

    /**
     * Handles user input each frame.
     * @param window The window this input function should listen to.
     */
    @Override
    public void input(Window window) {
        if(window.isKeyPressed(GLFW_KEY_UP)){
            direction = 1;
        } else if(window.isKeyPressed(GLFW_KEY_DOWN)){
            direction = -1;
        } else {
            direction = 0;
        }
    }

    /**
     * Do updates on game logic.
     * @param deltaT The time since the last logical frame.
     */
    @Override
    public void update(float deltaT) {
        color += direction * 0.01f;
        if (color > 1) {
            color = 1.0f;
        } else if ( color < 0 ) {
            color = 0.0f;
        }
    }

    @Override
    public void render(Window window) {
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }
        window.setClearColor(color, color, color, 0.0f);
        renderer.render(window, mesh);
    }

    @Override
    public void cleanup(){
        renderer.cleanup();
    }
}
