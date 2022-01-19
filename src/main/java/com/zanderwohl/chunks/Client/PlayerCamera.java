package com.zanderwohl.chunks.Client;

import org.joml.Vector3f;

/**
 * A camera styled after the movement found in FPS games.
 * X and Z move based on rotation, but y is always up and down.
 * Move like a person on a plane, or an ant on a sheet of paper.
 * Y exists, but it's not really that relevant.
 */
public class PlayerCamera implements ICamera {

    private final Vector3f position;

    private final Vector3f rotation;

    /**
     * New PlayerCamera at default 0,0,0
     */
    public PlayerCamera(){
        position = new Vector3f();
        rotation = new Vector3f();
    }

    /**
     * New PlayerCamera at specified position and rotation.
     * @param position Initial position.
     * @param rotation Initial rotation.
     */
    public PlayerCamera(Vector3f position, Vector3f rotation){
        this.position = position;
        this.rotation = rotation;
    }

    public Vector3f getPosition(){
        return position;
    }

    public Vector3f getRotation(){
        return rotation;
    }

    public void setPosition(float x, float y, float z){
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void setRotation(float x, float y, float z){
        rotation.x = x;
        rotation.y = y;
        rotation.z = z;
    }

    /**
     * PlayerCamera moves its position like an FPS.
     * @param offsetX
     * @param offsetY
     * @param offsetZ
     */
    public void movePosition(float offsetX, float offsetY, float offsetZ){
        if(offsetZ != 0){
            position.x += (float) Math.sin(Math.toRadians(rotation.y)) * -1.0f * offsetZ;
            position.z += (float) Math.cos(Math.toRadians(rotation.y)) * offsetZ;
        }
        if(offsetX != 0){
            position.x += (float) Math.sin(Math.toRadians(rotation.y - 90)) * -1.0f * offsetX;
            position.z += (float) Math.cos(Math.toRadians(rotation.y - 90)) * offsetX;
        }
        position.y += offsetY;
    }

    public void moveRotation(float offsetX, float offsetY, float offsetZ){
        rotation.x += offsetX;
        rotation.y += offsetY;
        rotation.z += offsetZ;
    }
}
