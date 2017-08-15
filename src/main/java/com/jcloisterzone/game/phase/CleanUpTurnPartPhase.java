package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.BuilderState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameState.Flag;

import io.vavr.collection.HashSet;

/**
 *  End of turn part. For builder double repeat turn otherwise proceed to real end of turn.
 */
public class CleanUpTurnPartPhase extends Phase {

    public CleanUpTurnPartPhase(Game game) {
        super(game);
    }

    @Override
    public void enter(GameState state) {
        BuilderState builderState = state.getCapabilities().getModel(BuilderCapability.class);
        boolean builderTakeAnotherTurn = builderState == BuilderState.USED;

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.onTurnPartCleanUp(state);
        }

        if (!state.getFlags().isEmpty()) {
            state = state.setFlags(state.getFlags()
                .remove(Flag.PORTAL_USED)
                .remove(Flag.PRINCESS_USED)
            );
        }

        if (builderTakeAnotherTurn) {
            next(state, state.getCapabilities().contains(AbbeyCapability.class) ? AbbeyPhase.class : DrawPhase.class);
        } else {
            next(state);
        }
    }
}
