package com.zanderwohl.chunks.Delta;

public class WorldRequest extends Delta{

    public final String requestedWorld;

    /**
     * A request for the metadata about a world.
     * @param name
     */
    public WorldRequest(String name){
        this.requestedWorld = name;
    }
}
