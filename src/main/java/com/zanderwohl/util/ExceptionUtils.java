package com.zanderwohl.util;

public class ExceptionUtils {

    public static String errorSource(Exception e){
        StackTraceElement topOfStack = e.getStackTrace()[0];
        return topOfStack.getFileName() + ":" + topOfStack.getLineNumber();
    }

    public static String boxMessage(Exception e){
        return "(" + e.getMessage() + ")";
    }
}
