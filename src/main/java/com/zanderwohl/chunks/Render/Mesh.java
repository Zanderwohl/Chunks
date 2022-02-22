package com.zanderwohl.chunks.Render;

import com.zanderwohl.chunks.Block.Texture;
import org.joml.Vector3f;
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

    private static final Vector3f DEFAULT_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);

    private final int vaoId;

    private final int positionVboId;
    private final int indexVboId;
    private final int textureVboId;
    private final int normalVboId;

    private final int vertexCount;

    private Texture texture;

    private Vector3f color;

    public Mesh(float[] positions, int[] indices, float[] textCoords, float[] normals){
        FloatBuffer verticesBuffer = null;
        IntBuffer indexBuffer = null;
        FloatBuffer colorBuffer = null;
        FloatBuffer textureCoordsBuffer = null;
        FloatBuffer vecNormalsBuffer = null;
        try {
            color = Mesh.DEFAULT_COLOR;

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
            glVertexAttribPointer(1, 2 /* u, v*/, GL_FLOAT, false, 0, 0);

            // Normal VBO
            normalVboId = glGenBuffers();
            vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
            vecNormalsBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, normalVboId);
            glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

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
            if(vecNormalsBuffer != null){
                memFree(vecNormalsBuffer);
            }
        }
    }

    public void setTexture(Texture t){
        texture = t;
    }

    public boolean isTextured(){
        return texture != null;
    }

    public void setColor(Vector3f color){
        this.color = color;
    }

    public Vector3f getColor(){
        return color;
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
        glDeleteBuffers(normalVboId);

        if(texture != null){
            texture.cleanup();
        }

        // Delete VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void render(){
        if(texture != null) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }

        glBindVertexArray(getVaoId());

        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
