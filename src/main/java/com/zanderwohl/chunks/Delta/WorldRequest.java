package com.zanderwohl.chunks.Delta;

import java.io.Serializable;

public class WorldRequest extends Delta implements Serializable {

    public final String requestedWorld;

    private static final long serialVersionUID = 32112000009L;

    /**
     * A request for the metadata about a world.
     * @param name
     */
    public WorldRequest(String name){
        this.requestedWorld = name;
    }
}
