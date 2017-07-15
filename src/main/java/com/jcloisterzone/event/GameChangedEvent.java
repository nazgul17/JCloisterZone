package com.jcloisterzone.event;

import com.jcloisterzone.game.GameState;

//temporary event for transition to new architecture?
public class GameChangedEvent extends Event {

    private final GameState prev;
    private final GameState curr;

    public GameChangedEvent(GameState prev, GameState curr) {
        this.prev = prev;
        this.curr = curr;
    }

    public GameState getCurrentState() {
        return curr;
    }

    public GameState getPrevState() {
        return prev;
    }

    public boolean hasPlayerActionsChanged() {
        return prev.getPlayerActions() != curr.getPlayerActions();
    }

    public boolean hasDiscardedTilesChanged() {
        return prev.getDiscardedTiles() != curr.getDiscardedTiles();
    }
}
