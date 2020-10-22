package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileLoader {

    String name, extension, contents = "";

    public FileLoader(String fileName) throws FileNotFoundException {
        String[] split = fileName.split("\\.");
        name = split[0];
        extension = split[1];
        contents = fileToString();

    }

    public String getFullName(){
        return name + "." + extension;
    }

    public String getName(){
        return name;
    }

    public String getExtension(){
        return extension;
    }

    public String getFile(){
        return contents;
    }

    /**
     * Converts the file specified to a single string, with line breaks.
     * @return A string containing the entire contents of the file.
     * @throws FileNotFoundException If the file specified is not found.
     */
    public String fileToString() throws FileNotFoundException {
        String fileContents = "";
        Scanner file = new Scanner(new File(getFullName()));
        while(file.hasNext()) {
            fileContents += file.nextLine() + "\n";
        }
        file.close();
        return fileContents;
    }


}
