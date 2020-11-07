package com.zanderwohl.chunks.Client;

import java.io.Serializable;

/**
 * Client identity includes username. In the future will include authentication information, skin, and character model.
 */
public class ClientIdentity implements java.io.Serializable {

    private String username;
    private String nickname = null;
    private String token;

    /**
     * Constructs an object that contains data about a particular user and its client.
     * @param username The username of the player.
     */
    public ClientIdentity(String username){
        this.username = username;
        this.token = "" + username.hashCode(); //TODO: Find a method to actually authenticate people.
    }

    /**
     * Constructs an object that contains data about a particular user and its client.
     * @param username The username of the player.
     * @param nickname A preferred name for the player.
     */
    public ClientIdentity(String username, String nickname){
        this.username = username;
        this.nickname = nickname;
        this.token = "" + username.hashCode(); //TODO: Find a method to actually authenticate people.
    }

    /**
     * Getter for the user's token.
     * @return The token.
     */
    public String getToken(){
        return token;
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

    /**
     * Getter for the nickname.
     * @return The nickname.
     */
    public String getNickname(){
        return nickname;
    }

    /**
     * Setter for the nickname.
     * @param newNickname The nickname to change to.
     */
    public void setNickname(String newNickname){
        this.nickname = newNickname;
    }

    /**
     * Get the user-facing display name for th user.
     * @return The nickname if there is one, the username otherwise.
     */
    public String getDisplayName(){
        if(nickname == null){
            return username;
        } else {
            return nickname;
        }
    }
}
