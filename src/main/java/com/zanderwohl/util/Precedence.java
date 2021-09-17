package com.zanderwohl.util;

public class Precedence {

    /**
     * Takes in any number of objects and nulls, then returns the last non-null object.
     * @param args The objects you'd like to choose from.
     * @param <T> The type of the objects you're passing in.
     * @return The last object that is non-null.
     */
    public static <T> T lastNonNull(T ...args){
        T answer = null;
        for(T arg: args){
            if(arg != null){
                answer = arg;
            }
        }
        return answer;
    }

    /**
     * Takes any number of strings, some of which may be ints, and returns the last in the list that's an actual int.
     * @param suspectedInts Strings which may or may not be ints.
     * @return The last int, parsed. If no ints given, 0.
     */
    public static int lastInt(String ...suspectedInts){
        int answer = 0;
        for(String suspect: suspectedInts){
            try {
                int n = Integer.parseInt(suspect);
                answer = n;
            } catch (NumberFormatException e){
                // DO NOTHING hahahaha.
            }
        }
        return answer;
    }
}
