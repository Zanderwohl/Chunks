package com.zanderwohl.chunks.Entity;

import com.zanderwohl.chunks.Client.ICamera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Generates transformations for graphics, including camera adjustments and model position adjustments.
 */
public class Transformation {

    private final Matrix4f projectionMatrix;

    private final Matrix4f modelViewMatrix;

    private final Matrix4f viewMatrix;

    /**
     * Create a new transformation, all zeroed.
     */
    public Transformation(){
        modelViewMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
    }

    /**
     *
     * @param fov
     * @param width
     * @param height
     * @param zNear
     * @param zFar
     * @return
     */
    public final Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar){
        return projectionMatrix.setPerspective(fov, width / height, zNear, zFar);
    }

    /**
     * Get the model for this entity, transformed for its position and the camera's position.
     * @param entity The entity to transform.
     * @param viewMatrix The matrix describing the camera position and rotation.
     * @return The transformed matrix.
     */
    public Matrix4f getModelViewMatrix(Entity entity, Matrix4f viewMatrix){
        Vector3f rotation = entity.getRotation();
        modelViewMatrix.identity().translate(entity.getPosition())
                .rotateX((float) Math.toRadians(-rotation.x))
                .rotateY((float) Math.toRadians(-rotation.y))
                .rotateZ((float) Math.toRadians(-rotation.z))
                .scale(entity.getScale());
        Matrix4f viewCurr = new Matrix4f(viewMatrix);
        return viewCurr.mul(modelViewMatrix);
    }

    /**
     * Get the viewmatrix, a matrix which can be used to adjust things based on the camera.
     * @param camera The camera to view from.
     * @return The viewMatrix of the provided camera.
     */
    public Matrix4f getViewMatrix(ICamera camera){
        Vector3f cameraPos = camera.getPosition();
        Vector3f cameraRot = camera.getRotation();

        viewMatrix.identity();
        viewMatrix.rotate((float) Math.toRadians(cameraRot.x), new Vector3f(1, 0,0 ))
                .rotate((float) Math.toRadians(cameraRot.y), new Vector3f(0, 1, 0));
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }
}
