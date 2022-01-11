package com.zanderwohl.chunks.Entity;

import com.zanderwohl.chunks.Render.Mesh;
import com.zanderwohl.chunks.World.Coord;
import org.joml.Vector3f;

public class Entity {

    private final Mesh mesh;

    private final Vector3f position;

    private float scale;

    private final Vector3f rotation;

    public Entity(Mesh mesh){
        this.mesh = mesh;
        position = new Vector3f();
        scale = 1;
        rotation = new Vector3f();
    }

    public Vector3f getPosition(){
        return position;
    }

    public void setPosition(float x, float y, float z){
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public void setPosition(Coord c){
        this.position.x = c.getX();
        this.position.y = c.getY();
        this.position.z = c.getZ();
    }

    public float getScale(){
        return scale;
    }

    public void setScale(float scale){
        this.scale = scale;
    }

    public Vector3f getRotation(){
        return rotation;
    }

    public void setRotation(float x, float y, float z){
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public Mesh getMesh(){
        return mesh;
    }
}
