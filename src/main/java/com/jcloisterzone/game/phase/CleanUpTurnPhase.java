package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.Game;
import com.jcloisterzone.reducers.ForEachCapability;
import com.jcloisterzone.reducers.SetNextPlayer;

/**
 * real end of turn and switch to next player
 */
public class CleanUpTurnPhase extends Phase {

    public CleanUpTurnPhase(Game game) {
        super(game);
    }

    @Override
    public void enter() {
        game.replaceState(
            new ForEachCapability((cap, s) -> cap.turnCleanUp(s)),
            new SetNextPlayer()
        );
        next();
    }
}
