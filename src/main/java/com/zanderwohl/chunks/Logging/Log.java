package com.zanderwohl.chunks.Logging;

import com.zanderwohl.console.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;

public class Log {
    public static PrintWriter openLog(String logNameWithPath, ArrayBlockingQueue<Message> toConsole){
        File logFile = new File(logNameWithPath);
        PrintWriter log = null; //IMPORTANT: Only use .append() as to not overwrite this file.
        try{
            if(!logFile.exists()){
                toConsole.add(new Message("source=Sim Loop\nmessage=Created new log file as \"" + logFile.getName() + "\"."));
                logFile.createNewFile();
            }
            log = new PrintWriter(new FileWriter(logFile, true));
        } catch (IOException e) {
            toConsole.add(new Message("source=Sim Loop\nseverity=critical\nWas unable to create new log file."));
        }
        return log;
    }

    public static void closeLog(PrintWriter log){
        if(log != null){
            log.close();
        }
    }

    public static boolean append(PrintWriter log, String data, boolean condition){
        if(log != null && condition){
            log.append(data + "\n");
        }
        return condition;
    }

    public static boolean append(PrintWriter log, String data){
        return append(log, data, true);
    }
}
