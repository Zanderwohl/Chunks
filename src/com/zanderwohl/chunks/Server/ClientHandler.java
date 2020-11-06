package com.zanderwohl.chunks.Server;

import com.zanderwohl.console.Message;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientHandler extends Thread{

    private Socket socket;
    ConcurrentLinkedQueue<Message> toConsole;

    public ClientHandler(Socket clientSocket, ConcurrentLinkedQueue<Message> toConsole){
        this.socket = clientSocket;
        this.toConsole = toConsole;
    }

    public void run(){
        InputStream in;
        BufferedReader br;
        DataOutputStream out;
        try{
            in = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(in));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            toConsole.add(new Message("source=ClientHandler\nseverity=critical\nmessage="
            + "A client just failed to connect."));
            return;
        }
        String nextLine;
        while(true){
            try{
                nextLine = br.readLine();
                if(nextLine.equalsIgnoreCase("DISCONNECT")){
                    socket.close();
                    toConsole.add(new Message("source=ClientHandler\nseverity=normal\nmessage="
                            + "A client just disconnected."));
                    return;
                } else {
                    toConsole.add(new Message("source=ClientHandler\nseverity=normal\nmessage="
                    + "Received from client: " + nextLine));
                    out.writeBytes("ACK\r\n");
                    out.flush();
                }
            } catch(IOException e){
                toConsole.add(new Message("source=ClientHandler\nseverity=critical\nmessage="
                        + "A client just disconnected due to an error."));
                return;
            }
        }
    }
}
