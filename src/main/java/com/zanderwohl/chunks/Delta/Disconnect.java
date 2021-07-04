package com.zanderwohl.chunks.Delta;

public class Disconnect extends Delta{

    private static final long serialVersionUID = 32112000002L;

    public enum DisconnectReason {
        ClientClosed,
        ClientQuit
    }

    private DisconnectReason reason;

    public Disconnect(DisconnectReason reason){
        super();
        this.reason = reason;
    }
}
