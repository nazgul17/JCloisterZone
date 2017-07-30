package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.GameState.Flag;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.BuilderCapability.BuilderState;

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
        BuilderCapability builderCap = state.getCapability(BuilderCapability.class);
        boolean builderTakeAnotherTurn = builderCap != null && builderCap.getBuilderState() == BuilderState.USED;


        // IMMUTABLE TODO Abbeys at end
//        if (getTile() != null) { //after last turn, abbeys can be placed, then cycling through players and tile can be null. Do not delegate on capabilities in such case
//            game.turnPartCleanUp();
//            game.setCurrentTile(null);
//        }

        //TODO make flag from builder state and remove handler?
        for (Capability cap : state.getCapabilities().values()) {
            state = cap.turnPartCleanUp(state);
        }

        if (!state.getFlags().isEmpty()) {
            state = state.setFlags(state.getFlags()
                .remove(Flag.PORTAL_USED)
                .remove(Flag.PRINCESS_USED)
            );
        }

        if (builderTakeAnotherTurn) {
            next(state, game.hasCapability(AbbeyCapability.class) ? AbbeyPhase.class : DrawPhase.class);
        } else {
            next(state);
        }
    }
}
