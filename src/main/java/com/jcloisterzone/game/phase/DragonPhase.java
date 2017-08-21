package com.jcloisterzone.game.phase;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.DragonCapability;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;

public class DragonPhase extends Phase {

    public DragonPhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive(CapabilitiesState capabilities) {
        return capabilities.contains(DragonCapability.class);
    }

    @Override
    public void enter(GameState state) {
        TileDefinition tile = state.getLastPlaced().getTile();
        if (tile.getTrigger() == TileTrigger.DRAGON) {
            Position pos = state.getNeutralFigures().getDragonDeployment();
            if (pos != null) {
                next(state, DragonMovePhase.class);
                return;
            }
        }
        next(state);
    }
}
