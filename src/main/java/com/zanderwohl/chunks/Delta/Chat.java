package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.Client.ClientIdentity;

import java.io.Serializable;

public class Chat extends Delta implements Serializable {

    private static final long serialVersionUID = 32112000001L;

    private String fromName;
    private String message;

    private static ClientIdentity serverIdentity = new ClientIdentity("SERVER");

    /**
     * A chat message.
     * @param from
     * @param message
     */
    public Chat(ClientIdentity from, String message){
        this.fromName = from.getDisplayName();
        setFrom(from);
        this.message = message;
    }

    public Chat(String message){
        this.fromName = serverIdentity.getDisplayName();
        setFrom(serverIdentity);
        this.message = message;
    }

    public String toString(){
        String open = "<";
        String close = ">";
        if(fromName.equalsIgnoreCase("server")){
            open = "[";
            close = "]";
        }
        return open + fromName + close + " " + message;
    }
}
