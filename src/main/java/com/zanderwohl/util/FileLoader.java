package com.zanderwohl.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * A wrapper class for text files.
 */
public class FileLoader {

    private String name, extension, contents;

    /**
     * Open a file based on its name. Cannot handle directories.
     * //TODO: handle directories.
     * @param fileName The name of the file, with its extension.
     * @throws FileNotFoundException If the file is not found.
     */
    public FileLoader(String fileName) {
        String[] split = fileName.split("\\.");
        name = split[0];
        extension = split[1];
        contents = null;
    }

    /**
     * The name of the file with its extension.
     * @return The full name of the file.
     */
    public String getFullName(){
        return name + "." + extension;
    }

    /**
     * The name of the file, without the extension.
     * @return The name of the file.
     */
    public String getName(){
        return name;
    }

    /**
     * Get the file extension of this file.
     * @return The extension without a .
     */
    public String getExtension(){
        return extension;
    }

    /**
     * Returns the content of the file in a single string with line breaks.
     * @return A string containing the entire contents of the file.
     */
    public String getFile() throws FileNotFoundException{
        if(contents == null){
            contents = fileToString();
        }
        return contents;
    }

    /**
     * Returns the content of the file if available, an empty string if not.
     * @return A string containing the contents of the file or empty string.
     */
    public String getFileSafe(){
        try {
            return getFile();
        } catch (FileNotFoundException e){
            return "";
        }
    }

    /**
     * Converts the file specified to a single string, with line breaks.
     * @return A string containing the entire contents of the file.
     * @throws FileNotFoundException If the file specified is not found.
     */
    private String fileToString() throws FileNotFoundException {
        String fileContents = "";
        Scanner file = new Scanner(new File(getFullName()));
        while(file.hasNext()) {
            fileContents += file.nextLine() + "\n";
        }
        file.close();
        return fileContents;
    }


}
