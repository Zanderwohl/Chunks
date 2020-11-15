package com.zanderwohl.chunks.Delta;

public class VolumeAge extends Delta {

    private long lastUpdated;

    public VolumeAge(long updatedTime){
        lastUpdated = updatedTime;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
