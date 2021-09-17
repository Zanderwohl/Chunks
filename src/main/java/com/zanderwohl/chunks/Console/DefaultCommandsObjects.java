package com.zanderwohl.chunks.Console;

import com.zanderwohl.chunks.Server.SimLoop;
import com.zanderwohl.chunks.World.WorldManager;

public class DefaultCommandsObjects implements CommandManager.ICommandManagerArguments {
    public WorldManager wm;
    public SimLoop sl;

    public DefaultCommandsObjects(WorldManager w, SimLoop s){
        wm = w;
        sl = s;
    }
}
