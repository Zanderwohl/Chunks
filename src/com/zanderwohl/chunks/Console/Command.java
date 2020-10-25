package com.zanderwohl.chunks.Console;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
  A command object that contains information input by the user such as arguments.
 */
public class Command {

    private String command = "";
    private String[] arguments = {};
    private String argumentsString = "";

    /**
     * Constructs a command out of a string that contains a command plus arguments.
     * @param message The string, which may include space-separated arguments, or arguments with spaces enclosed
     *                within quotes.
     * @throws OpenStringException If an odd number of quotes are used.
     */
    public Command(String message) throws OpenStringException{
        String[] split = split(message);

        command = split[0].toUpperCase(); //place 0th item as command
        arguments = Arrays.copyOfRange(split, 1, split.length); //place 1-end as arguments.

        try {
            argumentsString = message.split("\\s", 2)[1];
        } catch (ArrayIndexOutOfBoundsException e){
            argumentsString = "";
        }
    }

    /**
     * More complex split that allows arguments to be enclosed in quotes to allow for spaces in them.
     * This is a really convoluted solution and needs to be done by someone who knows how to write parsers.
     * @param s The string to be parsed.
     * @return An array of each token.
     * @throws OpenStringException If an odd number of quotes is present.
     */
    private String[] split(String s) throws OpenStringException{
        ArrayList<String> list = new ArrayList<String>();

        int index = 0;
        boolean inString = false;
        String token = "";

        while(index < s.length()){  //loop through the string
            String character = s.charAt(index) + "";    //take each character
            if(!inString && character.equals(" ")){ //if not in an enclosed quote and a space this is a completed token.
                list.add(token);    //add the token
                token = "";         //clear the token's contents for the next one
            } else {                //otherwise,
                if(character.equals("\"")){ //if a quote
                    if(!inString){              //and not yet in a string
                        inString = true;            //enter the string mode
                    } else {                    //if already in a string
                        if(index > 0 && s.charAt(index - 1) != '\\'){   //and the previous isn't an escape
                            inString = false;       //exit string mode
                        }
                    }
                } else {
                    token += character;     //add this character to the current token.
                }
            }

            index++;    //move to the next character
        }
        if(token.length() > 0){ //if there is a token left over not yet added, add it
            list.add(token);
        }
        if(inString && token.length() > 1 && token.charAt(token.length() - 1) == '\"'){ //if last char is quote
            inString = false;                                                           //exit string mode
        }
        if(inString){                               //if still in string
            throw new OpenStringException(token);       //crash
        }

        String[] return_list = new String[list.size()]; //move ArrayList to array before sending back
        for(int i = 0; i < list.size(); i++){
            return_list[i] = list.get(i);
        }
        return return_list;
    }

    /**
     * Shows contents of Command as a string. Form will change.
     * @return A list of contents useful for a human but not for a computer.
     */
    @Override
    public String toString() {
        return "Command{" +
                "command='" + command + '\'' +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }

    /**
     * Gets just the command, sans arguments.
     * @return The command.
     */
    public String getCommand(){
        return command;
    }

    /**
     * Get the arguments in array form.
     * @return The arguments.
     */
    public String[] getArguments(){
        return arguments.clone();
    }

    /**
     * Get an argument by index. Agnostic to particular command structure.
     * @param index The index of the wanted command.
     * @return The argument at the given index.
     */
    public String getArgument(int index){
        try {
            return arguments[index];
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    /**
     * Gets the list of arguments in a single string, whitespace-delimited.
     * @return The full list of arguments in a single string.
     */
    public String getArgumentsString(){
        return argumentsString;
    }

    /**
     * Gets the number of arguments.
     * @return The number of arguments.
     */
    public int getArgumentsLength(){
        return arguments.length;
    }

    /**
     * An exception to be thrown when a string is left open.
     */
    public static class OpenStringException extends Exception {
        public OpenStringException(String faultyToken) {
            super("Unclosed string in command \u2192 " + faultyToken);
        }
    }
}
