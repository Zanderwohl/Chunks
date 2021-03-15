package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.Client.ClientIdentity;

public abstract class Delta implements java.io.Serializable {

    private transient ClientIdentity from = null;

    public Delta(){

    }

    public ClientIdentity getFrom(){
        return from;
    }

    public void setFrom(ClientIdentity newFrom){
        this.from = newFrom;
    }

    public String toString(){
        ClientIdentity fromPerson = from;
        String fromString = "";
        if(from == null){
            fromString = "unspecified";
        } else {
            fromString = fromPerson.toString();
        }
        return "{ type: \"unspecified\", from: \"" + fromString+ "\" }";
    }
}
