package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.Client.ClientIdentity;

public class Chat extends Delta implements java.io.Serializable {

    private String from;
    private String message;

    private static ClientIdentity serverIdentity = new ClientIdentity("SERVER");

    public Chat(ClientIdentity from, String message){
        this.from = from.getDisplayName();
        this.message = message;
    }

    public Chat(String message){
        this.from = serverIdentity.getDisplayName();
        this.message = message;
    }

    public String toString(){
        String open = "<";
        String close = ">";
        if(from.equalsIgnoreCase("server")){
            open = "[";
            close = "]";
        }
        return open + from + close + " " + message;
    }
}
