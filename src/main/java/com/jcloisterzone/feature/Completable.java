package com.jcloisterzone.feature;

import com.jcloisterzone.game.state.GameState;

public interface Completable extends Scoreable {

    boolean isOpen(GameState state);
    default boolean isCompleted(GameState state) {
        return !isOpen(state);
    }

}
