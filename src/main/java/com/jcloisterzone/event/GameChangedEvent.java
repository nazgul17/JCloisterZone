package com.jcloisterzone.event;

import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.game.GameState;

import io.vavr.collection.Queue;

//temporary event for transition to new architecture?
public class GameChangedEvent extends Event {

    private final GameState prev;
    private final GameState curr;

    private transient Queue<PlayEvent> newEvents;

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

    public Queue<PlayEvent> getNewPlayEvents() {
        if (newEvents == null) {
            if (prev == null || prev.getEvents() == null) {
                newEvents = curr.getEvents();
            } else {
                newEvents = curr.getEvents().removeAll(prev.getEvents());
            }
        }
        return newEvents;
    }

    public boolean hasPlayerActionsChanged() {
        return prev.getPlayerActions() != curr.getPlayerActions();
    }

    public boolean hasDiscardedTilesChanged() {
        return prev.getDiscardedTiles() != curr.getDiscardedTiles();
    }

    public boolean hasMeeplesChanged() {
        return prev.getDeployedMeeples() != curr.getDeployedMeeples();
    }

    public boolean hasNeutralFiguresChanged() {
        return prev.getNeutralFigures().getDeployedNeutralFigures() !=
            curr.getNeutralFigures().getDeployedNeutralFigures();
    }
}
