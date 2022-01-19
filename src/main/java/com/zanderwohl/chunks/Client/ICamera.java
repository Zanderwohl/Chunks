package com.zanderwohl.chunks.Client;

import org.joml.Vector3f;

public interface ICamera {

    Vector3f getPosition();

    Vector3f getRotation();

    void setPosition(float x, float y, float z);

    void setRotation(float x, float y, float z);

    void movePosition(float offsetX, float offsetY, float offsetZ);

    void moveRotation(float offsetX, float offsetY, float offsetZ);
}
