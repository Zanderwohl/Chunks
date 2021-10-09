package com.zanderwohl.chunks.Delta;

import java.io.Serializable;

/**
 * A little hello from the server with some basic metadata.
 */
public class Hello extends Delta implements Serializable {

    public final String NAME;
    public final String MOTD;

    public Hello(String name, String motd){
        NAME = name;
        MOTD = motd;
    }
}
