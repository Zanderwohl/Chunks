package com.zanderwohl.chunks.Delta;

import java.io.Serializable;

/**
 * Timestamp for a volume that tells how old it is.
 * Old enough volumes will not be given individual deltas for block actions,
 * but rather will be reloaded entirely.
 */
public class VolumeAge extends Delta implements Serializable {

    private static final long serialVersionUID = 32112000007L;

    private final long lastUpdated;

    /**
     * A delta that tells when a particular volume was last updated.
     * @param updatedTime
     */
    //TODO: Add a way to know which volume is being talked about.
    public VolumeAge(long updatedTime){
        lastUpdated = updatedTime;
    }

}
