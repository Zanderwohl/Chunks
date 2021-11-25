package com.zanderwohl.chunks.Client;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

    private final int programId;

    private int vertexShaderId;

    private int fragmentShaderId;

    public ShaderProgram() throws Exception {
        programId = glCreateProgram();

        if(programId == 0){
            throw new Exception("Could not create shader program.");
        }
    }
}
