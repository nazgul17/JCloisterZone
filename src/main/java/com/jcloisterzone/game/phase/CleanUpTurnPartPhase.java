package com.jcloisterzone.game.phase;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BuilderCapability;

/**
 *  end of turn part. For double turn, second part starts otherways proceed to real end of turn
 */
public class CleanUpTurnPartPhase extends Phase {

    private final BuilderCapability builderCap;

    public CleanUpTurnPartPhase(Game game) {
        super(game);
        builderCap = game.getCapability(BuilderCapability.class);
    }

    @Override
    public void enter(GameState state) {
        boolean builderTakeAnotherTurn = builderCap != null && builderCap.hasPlayerAnotherTurn();


        // IMMUTABLE TODO Abbeys at end
//        if (getTile() != null) { //after last turn, abbeys can be placed, then cycling through players and tile can be null. Do not delegate on capabilities in such case
//            game.turnPartCleanUp();
//            game.setCurrentTile(null);
//        }

        for (Capability cap : state.getCapabilities().values()) {
            state = cap.turnPartCleanUp(state);
        }

        if (builderTakeAnotherTurn) {
            next(state, game.hasCapability(AbbeyCapability.class) ? AbbeyPhase.class : DrawPhase.class);
        } else {
            next(state);
        }
    }
}
