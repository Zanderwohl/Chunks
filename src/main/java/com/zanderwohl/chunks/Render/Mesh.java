package com.zanderwohl.chunks.Render;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

public class Mesh {

    private final int vaoId;

    private final int positionVboId;

    private final int indexVboId;

    private final int colorVboId;

    private final int vertexCount;

    public Mesh(float[] positions, int[] indices, float[] colors){
        FloatBuffer verticesBuffer = null;
        IntBuffer indexBuffer = null;
        FloatBuffer colorBuffer = null;
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

            // Position VBO
            positionVboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, positionVboId);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3 /* x, y, & z */, GL_FLOAT, false, 0, 0);

            // Color VBO
            colorVboId = glGenBuffers();
            colorBuffer = memAllocFloat(colors.length);
            colorBuffer.put(colors).flip();
            glBindBuffer(GL_ARRAY_BUFFER, colorVboId);
            glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3 /* x, y, & z */, GL_FLOAT, false, 0, 0);

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
            if(colorBuffer != null){
                memFree(colorBuffer);
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
        glDeleteBuffers(colorVboId);

        // Delete VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}
