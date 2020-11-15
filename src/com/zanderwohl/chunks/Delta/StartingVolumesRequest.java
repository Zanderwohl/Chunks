package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.World.Coord;

public class StartingVolumesRequest extends Delta {

    public final Coord location;

    public StartingVolumesRequest(Coord location){
        this.location = location;
    }
}
