package com.zanderwohl.chunks.Console;

public class Argument {

    private String name;
    private boolean required;
    private String description;

    public Argument (String name, boolean required, String description){
        this.name = name;
        this.required = required;
        this.description = description;
    }

    public String getName(){
        return name;
    }

    public boolean getRequired(){
        return required;
    }

    public String getDescription(){
        return description;
    }

    @Override
    public String toString(){
        String open = "";
        String close = "";
        if(!required){
            open = "[";
            close = "=]";
        }
        return open + name + close;
    }

    public String documentation(){
        return toString() + " - " + description;
    }
}
