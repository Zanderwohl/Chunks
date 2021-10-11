package com.zanderwohl.chunks.Gamelogic;

import com.zanderwohl.chunks.Client.Window;

public interface IGameLogic {

    void init(Window window) throws Exception;

    void input(Window window);

    void update(float deltaT);

    void render(Window window);
}
