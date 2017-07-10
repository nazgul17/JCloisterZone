package com.jcloisterzone.event;

import com.jcloisterzone.game.GameState;

//temporary event for transition to new architecture?
public class GameChangedEvent extends Event {

    private final GameState prev;

    public GameChangedEvent(GameState prev, GameState curr) {
        this.prev = prev;
        setGameState(curr);
    }

    public boolean hasPlayerActionsChanged() {
        return prev.getPlayerActions() != gameState.getPlayerActions();
    }
}
