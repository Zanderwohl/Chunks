package com.zanderwohl.chunks.Server;

public class Chat implements java.io.Serializable {

    private String from;
    private String message;

    public Chat(String from, String message){
        this.from = from;
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
