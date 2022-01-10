package com.zanderwohl.chunks.Render;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private final int vaoId;

    private final int vboId;

    private final int vertexCount;

    public Mesh(float[] positions){
        FloatBuffer verticesBuffer = null;
        try {
            verticesBuffer = MemoryUtil.memAllocFloat(positions.length);
            vertexCount = positions.length / 3;
            verticesBuffer.put(positions).flip();

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3 /* x, y, & z */, GL_FLOAT, false, 0, 0);

            // Unbind VBO then VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
        finally{
            if(verticesBuffer != null){
                MemoryUtil.memFree(verticesBuffer);
            }
        }
    }

    public int getVaoId(){
        return vaoId;
    }

    public int getVertexCount(){
        return vertexCount;
    }

    public void cleanUp(){
        glDisableVertexAttribArray(0);

        // Delete VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);

        // Delete VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
