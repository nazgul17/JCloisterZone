package com.jcloisterzone.game.state.mixins;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;

public interface ActionsStateMixin {

    ActionsState getPlayerActions();
    GameState setPlayerActions(ActionsState actionsState);

    default PlayerAction<?> getAction() {
        ActionsState as = getPlayerActions();
        return as == null ? null : as.getActions().get();
    }

    default GameState appendAction(PlayerAction<?> action) {
        assert action != null;
        ActionsState as = getPlayerActions();
        return setPlayerActions(as.appendAction(action));
    }

}
