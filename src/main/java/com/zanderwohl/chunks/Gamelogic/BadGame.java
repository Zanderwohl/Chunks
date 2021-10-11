package com.zanderwohl.chunks.Gamelogic;

import com.zanderwohl.chunks.Client.Renderer;
import com.zanderwohl.chunks.Client.Window;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.opengl.GL11.glViewport;

public class BadGame implements IGameLogic {

    private int direction = 0;

    private float color = 0.0f;

    private final Renderer renderer;

    public BadGame(){
        renderer = new Renderer();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init();
    }

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
        renderer.clear();
    }
}
