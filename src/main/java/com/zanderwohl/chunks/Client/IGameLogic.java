package com.zanderwohl.chunks.Client;

public interface IGameLogic {
    void init() throws Exception;

    void input(Window window);

    void update(float deltaTime);

    void render(Window window);
}
