package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Entity.Entity;
import com.zanderwohl.chunks.Entity.Transformation;
import com.zanderwohl.chunks.Render.Mesh;
import com.zanderwohl.chunks.Render.PointLight;
import com.zanderwohl.chunks.Shaders.SimpleShaderProgram;
import com.zanderwohl.util.FileLoader;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.glBufferData;

/**
 * Handles the rendering of each frame, geometry and shaders.
 */
public class Renderer {

    private SimpleShaderProgram shaderProgram;

    private float specularPower;

    private int vboId;

    private int vaoId;

    private static final float FOV = (float) Math.toRadians(90.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.0f;

    private Matrix4f projectionMatrix;

    private final Transformation transformation;

    public Renderer(){
        transformation = new Transformation();
        specularPower = 10f;
    }

    /**
     * Initialize the renderer and allocate memory and other resources.
     * Compiles the vertex and fragment shaders.
     * @throws Exception A crash from here means no-go. // TODO: This should be more specific.
     */
    public void init(Window window) throws Exception {
        shaderProgram = new SimpleShaderProgram();
        shaderProgram.createVertexShader((new FileLoader("/shaders/vertex.vert", true)).getFile());
        shaderProgram.createFragmentShader((new FileLoader("/shaders/fragment.frag", true)).getFile());
        shaderProgram.link();

        float aspectRatio = (float) window.getWidth() / window.getHeight();
        projectionMatrix = new Matrix4f().setPerspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrix");

        shaderProgram.createUniform("texture_sampler");

        // Create uniform for material
        shaderProgram.createMaterialUniform("material");

        // Create lighting related uniforms
        shaderProgram.createUniform("specularPower");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createPointLightUniform("pointLight");

        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Render a frame.
     * @param window The window to render the frame onto.
     */
    public void render(Window window, ICamera camera, Entity[] entities, Vector3f ambientLight, PointLight pointLight){
        clear();

        if(window.isResized()){
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.bind();

        projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(),
                Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        // Update Light Uniforms
        shaderProgram.setUniform("ambientLight", ambientLight);
        shaderProgram.setUniform("specularPower", specularPower);
        // Get a copy of the light object and transform its position to view coordinates
        PointLight currPointLight = new PointLight(pointLight);
        Vector3f lightPos = currPointLight.getPosition();
        Vector4f aux = new Vector4f(lightPos, 1);
        aux.mul(viewMatrix);
        lightPos.x = aux.x;
        lightPos.y = aux.y;
        lightPos.z = aux.z;
        shaderProgram.setUniform("pointLight", currPointLight);

        shaderProgram.setUniform("texture_sampler", 0);
        for(Entity entity: entities){
            if(entity == null){
                continue;
            }
            Mesh mesh = entity.getMesh();
            // Set world matrix for this item
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(entity, viewMatrix);
            shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
            shaderProgram.setUniform("material", mesh.getMaterial());
            entity.getMesh().render();
        }

        shaderProgram.unbind();
    }

    /**
     * Calls glClear.
     */
    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Clean up resources, closing files and freeing memory.
     */
    public void cleanup(){
        if(shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }

}
