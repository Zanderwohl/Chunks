package com.zanderwohl.chunks.Delta;

import java.io.Serializable;

public class ServerClose extends Delta implements Serializable {

    public final String closeMessage;

    /**
     * A delta that signals that the server has closed and no further messages will be sent or processed.
     */
    public ServerClose(){
        closeMessage = "No close message was given.";
    }

    public ServerClose(String reason){
        closeMessage = reason;
    }
}
