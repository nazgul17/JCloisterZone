package com.jcloisterzone.game.state;

import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.PlayerAction;

public interface GameStateHelpers {

    // Actions

    default GameState appendAction(GameState state, PlayerAction<?> action) {
        assert action != null;
        ActionsState as = state.getPlayerActions();
        return state.setPlayerActions(as.appendAction(action));
    }

}
