package com.zanderwohl.chunks.Render;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.memFree;

public class Mesh {

    private final int vaoId;

    private final int positionVboId;

    private final int indexVboId;

    private final int vertexCount;

    public Mesh(float[] positions, int[] indices){
        FloatBuffer verticesBuffer = null;
        IntBuffer indexBuffer = null;
        try {
            verticesBuffer = MemoryUtil.memAllocFloat(positions.length);
            vertexCount = indices.length;
            verticesBuffer.put(positions).flip();

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Index VBO
            indexVboId = glGenBuffers();
            indexBuffer = MemoryUtil.memAllocInt(indices.length);
            indexBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
            memFree(indexBuffer);

            // Position VBO
            positionVboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, positionVboId);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3 /* x, y, & z */, GL_FLOAT, false, 0, 0);

            // Unbind VBO then VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
        finally{
            if(verticesBuffer != null){
                memFree(verticesBuffer);
            }
            if(indexBuffer != null){
                memFree(indexBuffer);
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
        glDeleteBuffers(positionVboId);
        glDeleteBuffers(indexVboId);

        // Delete VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
