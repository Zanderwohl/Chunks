package com.zanderwohl.chunks.Delta;

import com.zanderwohl.chunks.Client.ClientIdentity;

import java.io.Serializable;

public class Disconnect extends Delta implements Serializable {

    private static final long serialVersionUID = 32112000002L;

    public enum DisconnectReason {
        ClientClosed,
        ClientQuit
    }

    public final DisconnectReason reason;
    public final String token;

    public Disconnect(ClientIdentity ci, DisconnectReason reason){
        super();
        this.token = ci.getToken();
        this.reason = reason;
    }
}
