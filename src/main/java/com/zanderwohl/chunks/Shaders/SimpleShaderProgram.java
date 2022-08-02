package com.zanderwohl.chunks.Shaders;

import com.zanderwohl.chunks.Render.DirectionalLight;
import com.zanderwohl.chunks.Render.Material;
import com.zanderwohl.chunks.Render.PointLight;
import com.zanderwohl.chunks.Render.SpotLight;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class SimpleShaderProgram {

    private final int programId;

    private int vertexShaderId;

    private int fragmentShaderId;

    private final Map<String,Integer> uniforms;

    public SimpleShaderProgram() throws Exception {
        programId = glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader"); // TODO: Link all errors here to SuperConsole
        }

        uniforms = new HashMap<>();
    }

    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);

        return shaderId;
    }

    public void link() throws Exception {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            glDetachShader(programId, fragmentShaderId);
        }

        glValidateProgram(programId); // 2021-10-12 TODO: Remove at production stage. Unless we expect users to add more shaders, I guess.
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }

    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            glDeleteProgram(programId);
        }
    }

    public void createUniform(String name) throws Exception {
        int uniformLocation = glGetUniformLocation(programId, name);
        if(uniformLocation < 0) {
            throw new Exception("Could not find uniform '" + name + "'.");
        }
        uniforms.put(name, uniformLocation);
    }

    public void setUniform(String name, Matrix4f value){
        // Put the matrix into a float buffer
        try(MemoryStack stack = MemoryStack.stackPush()){
            glUniformMatrix4fv(uniforms.get(name), false, value.get(stack.mallocFloat(16))); // why 16?
        }
    }

    public void setUniform(String name, int value){
        glUniform1i(uniforms.get(name), value);
    }

    public void setUniform(String uniformName, float value) {
        glUniform1f(uniforms.get(uniformName), value);
    }

    public void setUniform(String name, Vector3f value){
        glUniform3f(uniforms.get(name), value.x, value.y, value.z);
    }

    public void setUniform(String uniformName, Vector4f value) {
        glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    public void createPointLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".color");
        createUniform(uniformName + ".position");
        createUniform(uniformName + ".intensity");
        createUniform(uniformName + ".att.constant");
        createUniform(uniformName + ".att.linear");
        createUniform(uniformName + ".att.exponent");
    }

    public void createDirectionalLightUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".color");
        createUniform(uniformName + ".direction");
        createUniform(uniformName + ".intensity");
    }

    public void createSpotLightUniform(String uniformName) throws Exception {
        createPointLightUniform(uniformName + ".pl");
        createUniform(uniformName + ".conedir");
        createUniform(uniformName + ".cutoff");
    }

    public void createMaterialUniform(String uniformName) throws Exception {
        createUniform(uniformName + ".ambient");
        createUniform(uniformName + ".diffuse");
        createUniform(uniformName + ".specular");
        createUniform(uniformName + ".hasTexture");
        createUniform(uniformName + ".reflectance");
    }

    public void setUniform(String uniformName, PointLight pointLight) {
        setUniform(uniformName + ".color", pointLight.getColor() );
        setUniform(uniformName + ".position", pointLight.getPosition());
        setUniform(uniformName + ".intensity", pointLight.getIntensity());
        PointLight.Attenuation att = pointLight.getAttenuation();
        setUniform(uniformName + ".att.constant", att.getConstant());
        setUniform(uniformName + ".att.linear", att.getLinear());
        setUniform(uniformName + ".att.exponent", att.getExponent());
    }

    public void setUniform(String uniformName, DirectionalLight dirLight) {
        setUniform(uniformName + ".color", dirLight.getColor() );
        setUniform(uniformName + ".direction", dirLight.getDirection());
        setUniform(uniformName + ".intensity", dirLight.getIntensity());
    }

    public void setUniform(String uniformName, SpotLight spotLight) {
        setUniform(uniformName + ".pl", spotLight.getPointLight());
        setUniform(uniformName + ".conedir", spotLight.getConeDirection());
        setUniform(uniformName + ".cutoff", spotLight.getCutOff());
    }

    public void setUniform(String uniformName, Material material) {
        setUniform(uniformName + ".ambient", material.getAmbientColor());
        setUniform(uniformName + ".diffuse", material.getDiffuseColor());
        setUniform(uniformName + ".specular", material.getSpecularColor());
        setUniform(uniformName + ".hasTexture", material.isTextured() ? 1 : 0);
        setUniform(uniformName + ".reflectance", material.getReflectance());
    }
}
