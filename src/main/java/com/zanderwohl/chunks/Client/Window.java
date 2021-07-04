package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.StringConstants;
import com.zanderwohl.console.Message;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

import java.util.concurrent.ArrayBlockingQueue;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11C.GL_FALSE;
import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private long windowId;
    private boolean destroyed;

    private ArrayBlockingQueue<Message> toConsole;

    public Window(ArrayBlockingQueue<Message> toConsole){
        destroyed = false;
        this.toConsole = toConsole;
    }

    /**
     * Initialize LWJGL and window.
     * This is a lot. Consider breaking this down.
     */
    protected long init(){
        GLFWErrorCallback.createPrint(System.err).set(); //TODO: Make errors go to console.

        if (!glfwInit()) {
            toConsole.add(new Message("severity=critical\nsource=Client Loop\nmessage=Unable to initialize GLFW."));
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

        int WIDTH = 1280;
        int HEIGHT = 720;

        windowId = glfwCreateWindow(WIDTH, HEIGHT, StringConstants.gameTitle, NULL, NULL);
        if (windowId == NULL) {
            toConsole.add(new Message("severity=critical\nsource=Client Loop\nmessage=Failed to create window."));
        }

        glfwSetKeyCallback(windowId, (windowId, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE){
                glfwSetWindowShouldClose(windowId, true);
            }
        });

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(windowId, (vidMode.width() - WIDTH) / 2, (vidMode.height() - HEIGHT) / 2);

        glfwMakeContextCurrent(windowId);

        glfwSwapInterval(1);

        glfwShowWindow(windowId);

        return windowId;
    }

    public void destroy(){
        glfwFreeCallbacks(windowId);
        glfwDestroyWindow(windowId);
        destroyed = true;
    }

    public void free(){
        if(!destroyed){
            destroy();
        }
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public boolean isKeyPressed(int keyCode){
        return glfwGetKey(windowId, keyCode) == GLFW_PRESS;
    }

    public void swapBuffers(){
        glfwSwapBuffers(windowId);
    }

    public boolean shouldClose(){
        return glfwWindowShouldClose(windowId);
    }
}
