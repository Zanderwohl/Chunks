package com.zanderwohl.chunks.Console;

import com.zanderwohl.chunks.Block.BlockLibrary;
import com.zanderwohl.chunks.Server.SimLoop;
import com.zanderwohl.chunks.World.WorldManager;

public class DefaultCommandsObjects implements CommandManager.ICommandManagerArguments {

    public final WorldManager wm;
    public final SimLoop sl;
    public final BlockLibrary bl;
    private Class<?> commandType;

    public DefaultCommandsObjects(WorldManager w, SimLoop s){
        wm = w;
        sl = s;
        bl = w.getLibrary();
        this.commandType = DefaultCommands.class;
    }

    @Override
    public Class<?> getCommandType() {
        return commandType;
    }
}
