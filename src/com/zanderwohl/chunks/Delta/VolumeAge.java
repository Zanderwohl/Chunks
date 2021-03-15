package com.zanderwohl.chunks.Delta;

public class VolumeAge extends Delta {

    private long lastUpdated;

    /**
     * A delta that tells when a particular volume was last updated.
     * @param updatedTime
     */
    //TODO: Add a way to know which volume is being talked about.
    public VolumeAge(long updatedTime){
        lastUpdated = updatedTime;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
