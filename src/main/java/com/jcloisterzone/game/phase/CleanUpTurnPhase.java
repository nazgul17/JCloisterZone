package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.play.PlayerTurnEvent;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;

/**
 * real end of turn and switch to next player
 */
public class CleanUpTurnPhase extends Phase {

    public CleanUpTurnPhase(Game game) {
        super(game);
    }

    @Override
    public void enter() {
        GameState state = game.getState();

        for (Capability cap: state.getCapabilities().values()) {
            state = cap.turnCleanUp(state);
        }

        Player player = game.getNextPlayer();
        state = state.setTurnPlayer(player.getIndex());
        state = state.appendEvent(new PlayerTurnEvent(player));

        game.replaceState(state);
        next();
    }
}
