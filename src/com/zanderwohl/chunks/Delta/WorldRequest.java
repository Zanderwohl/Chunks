package com.zanderwohl.chunks.Delta;

public class WorldRequest extends Delta{

    public final String requestedWorld;

    public WorldRequest(String name){
        this.requestedWorld = name;
    }
}
