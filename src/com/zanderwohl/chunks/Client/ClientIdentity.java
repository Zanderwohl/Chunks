package com.zanderwohl.chunks.Client;

public class ClientIdentity {

    private String username;

    public ClientIdentity(String username){
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    protected void setUsername(String username){
        this.username = username;
    }
}
