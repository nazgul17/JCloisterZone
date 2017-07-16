package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.reducers.SetNextPlayer;

/**
 * real end of turn and switch to next player
 */
public class CleanUpTurnPhase extends Phase {

    public CleanUpTurnPhase(Game game) {
        super(game);
    }

    @Override
    public void enter(GameState state) {

        for (Capability cap : state.getCapabilities().values()) {
            state = cap.turnCleanUp(state);
        }
        state = (new SetNextPlayer()).apply(state);

        next(state);
    }
}
