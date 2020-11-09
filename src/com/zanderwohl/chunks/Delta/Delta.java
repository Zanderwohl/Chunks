package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.Client.ClientIdentity;

public abstract class Delta implements java.io.Serializable {

    private transient ClientIdentity from;

    public Delta(ClientIdentity fromClient){
        this.from = fromClient;
    }

    public ClientIdentity getFrom(){
        return from;
    }

    public void setFrom(ClientIdentity newFrom){
        this.from = newFrom;
    }

}
