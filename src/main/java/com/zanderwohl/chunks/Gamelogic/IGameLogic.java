package com.zanderwohl.chunks.Gamelogic;

import com.zanderwohl.chunks.Client.Window;

/**
 * Game logic interface - each game must provide these methods.
 */
public interface IGameLogic {

    /**
     *
     * @param window
     * @throws Exception
     */
    void init(Window window) throws Exception;

    /**
     *
     * @param window
     */
    void input(Window window);

    /**
     *
     * @param deltaT
     */
    void update(float deltaT);

    /**
     *
     * @param window
     */
    void render(Window window);

    /**
     * Perform any actions to free memory, save settings and files, etc.
     * Will be called by the framework when the game closes or crashes.
     */
    void cleanup();
}
