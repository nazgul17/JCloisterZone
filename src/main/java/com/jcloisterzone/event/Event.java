package com.jcloisterzone.event;

import com.jcloisterzone.game.GameState;

/**
 * Ancestor for all events including non-game events like setup and chat.
 */
public abstract class Event {

    @Deprecated
    private final int type;

    protected GameState gameState;

    public Event() {
        this(0);
    }

    public Event(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public String toString() {
        if (type != 0) {
            return getClass().getSimpleName() + "/" + type;
        }
        return getClass().getSimpleName();
    }

}
