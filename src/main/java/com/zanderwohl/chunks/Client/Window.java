package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.StringConstants;
import com.zanderwohl.console.Message;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

import java.util.concurrent.ArrayBlockingQueue;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_FALSE;
import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.opengl.GL;

/**
 * Window for playing the game, displaying graphics and getting user input, etc.
 */
public class Window {

    private long windowId;
    private boolean destroyed;

    private ArrayBlockingQueue<Message> toConsole;
    private int width;
    private int height;
    private String title;
    private boolean resized = false;
    private boolean vsync = true;

    private Renderer renderer;

    private boolean closed;

    public Window(ArrayBlockingQueue<Message> toConsole){
        destroyed = false;
        this.toConsole = toConsole;
        this.title = "ALEXANDER GAME";
        this.renderer = new Renderer();
        closed = false;
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
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        
        int WIDTH = 1280;
        int HEIGHT = 720;

        windowId = glfwCreateWindow(WIDTH, HEIGHT, StringConstants.gameTitle, NULL, NULL);
        if (windowId == NULL) {
            toConsole.add(new Message("severity=critical\nsource=Client Loop\nmessage=Failed to create window."));
        }

        glfwSetFramebufferSizeCallback(windowId, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });

        glfwSetKeyCallback(windowId, (windowId, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE){
                glfwSetWindowShouldClose(windowId, true);
            }
        });

        glfwSetWindowCloseCallback(windowId, onClose);

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(windowId, (vidMode.width() - WIDTH) / 2, (vidMode.height() - HEIGHT) / 2);

        glfwMakeContextCurrent(windowId);

        if(isVSync()) {
            glfwSwapInterval(1);
        }


        glfwShowWindow(windowId);

        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        return windowId;
    }

    public void setClearColor(float r, float g, float b, float alpha){
        glClearColor(r, g, b, alpha);
    }

    /**
     * Call when the window is resized. Is reset on Renderer.render for this window.
     * @param resized If the window was resized.
     */
    public void setResized(boolean resized) {
        this.resized = resized;
    }

    /**
     * Destroy this window, and mark it as destroyed.
     */
    public void destroy(){
        glfwFreeCallbacks(windowId);
        glfwDestroyWindow(windowId);
        destroyed = true;
        closed = true;
    }

    /**
     * Free resources for this window, and destroy if needed.
     */
    public void free(){
        if(!destroyed){
            destroy();
        }
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * See if the key is being pressed this update frame.
     * @param keyCode Key code by GLFW key code.
     * @return True if key is pressed.
     */
    public boolean isKeyPressed(int keyCode){
        return glfwGetKey(windowId, keyCode) == GLFW_PRESS;
    }

    public boolean shouldClose(){
        return glfwWindowShouldClose(windowId) || closed;
    }

    public boolean isVSync(){
        return vsync;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        //TODO: implement title setting
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public boolean isResized(){
        return resized;
    }

    public void setVsync(boolean vsync){
        this.vsync = vsync;
    }

    public void update(){
        glfwSwapBuffers(windowId);
        glfwPollEvents();
    }

    private final GLFWWindowCloseCallback onClose = new GLFWWindowCloseCallback() {
        @Override
        public void invoke(long windowId) {
            toConsole.add(new Message("source=Window\nmessage=Window closed."));
            glfwSetWindowShouldClose(windowId, true);
        }
    };

    public long getWindowId(){
        return windowId;
    }
}
