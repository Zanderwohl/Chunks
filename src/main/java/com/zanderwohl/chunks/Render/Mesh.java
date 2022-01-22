package com.zanderwohl.chunks.Render;

import com.zanderwohl.chunks.Block.Texture;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * A mesh; a 3D model of something. Should be shared by all objects using the same model.
 */
public class Mesh {

    private final int vaoId;

    private final int positionVboId;
    private final int indexVboId;
    private final int textureVboId;

    private final int vertexCount;

    private final Texture texture;

    public Mesh(float[] positions, int[] indices, float[] textCoords, Texture texture){
        FloatBuffer verticesBuffer = null;
        IntBuffer indexBuffer = null;
        FloatBuffer colorBuffer = null;
        FloatBuffer textureCoordsBuffer = null;
        this.texture = texture;
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

            // Texture VBO
            textureVboId = glGenBuffers();
            textureCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            textureCoordsBuffer.put(textCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, textureVboId);
            glBufferData(GL_ARRAY_BUFFER, textureCoordsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

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
            if(textureCoordsBuffer != null){
                memFree(textureCoordsBuffer);
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
        glDeleteBuffers(textureVboId);

        // Delete VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void render(){
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        glBindVertexArray(getVaoId());

        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
    }
}
