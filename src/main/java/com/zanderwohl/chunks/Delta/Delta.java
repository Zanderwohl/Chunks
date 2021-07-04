package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.Client.ClientIdentity;

public abstract class Delta {

    private static final long serialVersionUID = 32112000000L;

    private transient ClientIdentity from = null;
    private transient ClientIdentity to = null;

    public Delta(){
        //TODO: ALWAYS HAVE A FROM. WHY IS IT IN A SEPARATE METHOD?
    }

    public ClientIdentity getFrom(){
        return from;
    }

    public void setFrom(ClientIdentity newFrom){
        this.from = newFrom;
    }

    public void setTo(ClientIdentity newTo){
        this.to = newTo;
    }

    public ClientIdentity getTo(){
        return to;
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
