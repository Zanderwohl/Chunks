package com.zanderwohl.chunks.Server;

import com.zanderwohl.chunks.Client.ClientIdentity;
import com.zanderwohl.console.Message;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientHandler implements Runnable{

    private Socket socket;
    ConcurrentLinkedQueue<Message> toConsole;
    ClientIdentity identity;

    public ClientHandler(Socket clientSocket, ConcurrentLinkedQueue<Message> toConsole){
        this.socket = clientSocket;
        this.toConsole = toConsole;
    }

    public void run(){
        InputStream in;
        ObjectInputStream oin;
        BufferedReader br;
        DataOutputStream out;
        try{
            in = socket.getInputStream();
            oin = new ObjectInputStream(in);
            br = new BufferedReader(new InputStreamReader(in));
            out = new DataOutputStream(socket.getOutputStream());
            identity = (ClientIdentity) oin.readObject();
        } catch (IOException e) {
            toConsole.add(new Message("source=Client Handler\nseverity=critical\nmessage="
            + "A client just failed to connect."));
            return;
        } catch (ClassNotFoundException e){
            toConsole.add(new Message("source=Client Handler\nseverity=critical\nmessage="
            + "The first communication from a client was NOT its identity! Disconnecting."));
            return;
        }
        toConsole.add(new Message("source=Client Handler\nmessage=" +
                "User " + identity.getUsername() + " connected to the server!"));
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
            } catch (NullPointerException e){

            }
        }
    }
}
