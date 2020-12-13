package com.zanderwohl.chunks.Delta;

import java.io.Serializable;

public class ServerClose extends Delta implements Serializable {

    public final String closeMessage;

    public ServerClose(){
        closeMessage = "No close message was given.";
    }

    public ServerClose(String reason){
        closeMessage = reason;
    }
}
