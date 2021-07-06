package com.zanderwohl.chunks.Console;

import com.zanderwohl.console.Message;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

public class Command {

    private HashMap<String, Argument> argumentMap;
    private ArrayList<Argument> argumentList;
    private int requiredArgumentsLength = 0;
    private String name;
    private String description;
    private BiConsumer<HashMap<String, String>, ArrayBlockingQueue<Message>> action;
    boolean argumentListSorted = false;

    public Command(String name, String description, BiConsumer<HashMap<String, String>,
            ArrayBlockingQueue<Message>> action){
        argumentMap = new HashMap<>();
        argumentList = new ArrayList<>();
        this.name = name;
        this.description = description;
        this.action = action;
    }

    public void addArgument(Argument argument){
        argumentMap.put(argument.getName(), argument);
        argumentList.add(argument);
        argumentListSorted = false;
        if(argument.getRequired()){
            argumentMap.put(requiredArgumentsLength + "", argument);
            requiredArgumentsLength++;
        }
    }

    /**
     * Gives a skeletal map of the command, where no arguments have values.
     * @return The skeleton.
     */
    public String skeleton(){
        String skeleton = name;
        for(Map.Entry<String, Argument> argument: argumentMap.entrySet()){
            skeleton += "  [" + argument.getValue().getName() + "]";
        }
        return "";
    }

    public void addArgument(String name, boolean required, String description){
        Argument argument = new Argument(name, required, description);
        addArgument(argument);
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String toString(){
        return name + " - " + description;
    }

    public String documentation(){
        if(!argumentListSorted){
            //argumentList.sort(Comparator.comparing(Argument::getName));
            argumentListSorted = true;
        }
        String help = name + " - " + description;
        for(Argument a: argumentList){
            help += "\\n" + a.documentation();
        }
        System.out.println(help); //TODO: Remove when command details are created.
        return help;
    }

    private HashMap<String, String> arrangeArguments(String[] arguments) throws ArgumentOrderException {
        HashMap<String, String> arrangedArgs = new HashMap<>();
        boolean switchedToOrderless = false;
        for(int i = 0; i < arguments.length; i++){
            String argument = arguments[i];
            boolean isOrderless = arguments[i].matches(".*=.*");
            if(!isOrderless){
                if(switchedToOrderless){
                    throw new ArgumentOrderException("An ordered argument cannot come after an orderless argument.");
                } else {
                    Argument arg = argumentMap.get(i + "");
                    if(arg == null){
                        throw new ArgumentOrderException("Too many ordered arguments! (" + argument + ")");
                    }
                    String argName = arg.getName();
                    arrangedArgs.put(argName, argument);
                }
            } else {
                switchedToOrderless = true;
                String[] splitArg = argument.split("=", 2);
                String argName = splitArg[0];
                String argBody = splitArg[1];
                if(argumentMap.get(argName) != null){
                    arrangedArgs.put(argName, argBody);
                } else {
                    throw new ArgumentOrderException("The unordered argument " + argName + " does not exist.");
                }
            }
        }
        return arrangedArgs;
    }

    /**
     * Exception thrown when an argument is in the wrong order, there are too many arguments, or too few arguments.
     */
    public class ArgumentOrderException extends Exception {

        private Message message;

        public ArgumentOrderException(String message){
            this.message = new Message("source=Command\nseverity=warning\nmessage=" + message);
        }

        public Message getErrorMessage(){
            return message;
        }
    }

    public void accept(UserCommand command, ArrayBlockingQueue<Message> toConsole) {
        try {
            HashMap<String, String> arguments = arrangeArguments(command.getArguments());
            action.accept(arguments, toConsole);
        } catch (Command.ArgumentOrderException e){
            toConsole.add(e.getErrorMessage());
        }
    }
}
