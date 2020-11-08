package com.zanderwohl.chunks.Client;

import javax.swing.*;

public class ClientWindow {

    JFrame gameWindow;

    public ClientWindow(){
        gameWindow = new JFrame();
    }

    public void start(){
        gameWindow.setVisible(true);
    }
}
