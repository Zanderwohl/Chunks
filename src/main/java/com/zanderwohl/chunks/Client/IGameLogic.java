package com.zanderwohl.chunks.Client;

import com.zanderwohl.chunks.Block.BlockLibrary;

public interface IGameLogic {
    void init() throws Exception;

    void input(Window window);

    void update(float deltaTime);

    void render(Window window);

    void setBlockLibrary(BlockLibrary blockLibrary);
}
