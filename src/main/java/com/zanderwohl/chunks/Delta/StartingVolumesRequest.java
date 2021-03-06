package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.World.Coord;

public class StartingVolumesRequest extends Delta {

    private static final long serialVersionUID = 32112000006L;

    public final Coord location;

    /**
     * A request to get a few volumes surrounding a newly-connected player.
     * @param location
     */
    public StartingVolumesRequest(Coord location){
        this.location = location;
    }
}
