package com.zanderwohl.chunks.Client;

/**
 * Client identity includes username. In the future will include authentication information, skin, and character model.
 */
public class ClientIdentity {

    private String username;

    /**
     * Constructs an object that contains data about a particular user and its client.
     * @param username The username of the player.
     */
    public ClientIdentity(String username){
        this.username = username;
    }

    /**
     * Getter for the username.
     * @return The player's username.
     */
    public String getUsername(){
        return username;
    }

    /**
     * Setter for the username.
     * @param username The new username.
     */
    protected void setUsername(String username){
        this.username = username;
    }
}
