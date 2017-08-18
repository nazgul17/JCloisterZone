package com.jcloisterzone.game.state;

import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.PlayerAction;

public interface GameStateHelpers {

    // Actions

    default PlayerAction<?> getAction(GameState state) {
        ActionsState as = state.getPlayerActions();
        return as == null ? null : as.getActions().get();
    }

    default GameState appendAction(GameState state, PlayerAction<?> action) {
        assert action != null;
        ActionsState as = state.getPlayerActions();
        return state.setPlayerActions(as.appendAction(action));
    }

}
