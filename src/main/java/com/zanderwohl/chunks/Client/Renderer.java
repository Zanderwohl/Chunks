package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Shaders.SimpleShaderProgram;
import com.zanderwohl.util.FileLoader;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class Renderer {

    private SimpleShaderProgram shaderProgram;

    public void init() throws Exception {
        shaderProgram = new SimpleShaderProgram();
        shaderProgram.createVertexShader((new FileLoader("/shaders/vertex.vs")).getFile());
        shaderProgram.createFragmentShader((new FileLoader("/shaders/fragment.vs")).getFile());
        shaderProgram.link();
    }

    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

}
