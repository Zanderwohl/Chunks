package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.Client.ClientIdentity;

import java.io.Serializable;

public class Kick extends Delta implements Serializable {

    private String reason;

    /**
     * An delta that kicks a player form a server.
     * @param reason
     */
    public Kick(String reason) {
        this.reason = reason;
    }

    public String getReason(){
        return reason;
    }

}
