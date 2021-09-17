package com.zanderwohl.chunks.Console;

public class CommandSet {

    /**
     * An exception to be thrown when a string is left open.
     */
    public static class WrongArgumentsObjectException extends Exception {
        public WrongArgumentsObjectException(String message) {
            super(message);
        }
    }
}
