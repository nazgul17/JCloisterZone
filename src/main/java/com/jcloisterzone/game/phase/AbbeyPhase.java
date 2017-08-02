package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BuilderState;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.PlaceTileMessage;

public class AbbeyPhase extends ServerAwarePhase {

    public AbbeyPhase(Game game, GameController controller) {
        super(game, controller);
    }

    @Override
    public boolean isActive(CapabilitiesState capabilities) {
        return capabilities.hasCapability(AbbeyCapability.class);
    }

    @Override
    public void enter() {
        boolean baazaarInProgress = bazaarCap != null && bazaarCap.getBazaarSupply() != null;
        boolean builderSecondTurnPart = builderCap != null && builderCap.getBuilderState() == BuilderState.SECOND_TURN;
        if (builderSecondTurnPart || !baazaarInProgress) {
            if (abbeyCap.hasUnusedAbbey(getActivePlayer()) && !getBoard().getHoles().isEmpty()) {
                toggleClock(getActivePlayer());

                game.post(
                    new SelectActionEvent(
                        getActivePlayer(),
                        new AbbeyPlacementAction(
                            getBoard().getHoles().map(t -> t._1).toSet()
                        ),
                        true
                    )
                );
                return;
            }
        }
        next();
    }

    @WsSubscribe
    public void handlePass(PassMessage msg) {
        next();
    }

    @WsSubscribe
    public void handlePlaceTile(PlaceTileMessage msg) {
        abbeyCap.useAbbey(getActivePlayer());

        Tile nextTile = game.getTilePack().drawTile("inactive", Tile.ABBEY_TILE_ID);
        game.setCurrentTile(nextTile);
        nextTile.setRotation(msg.getRotation());
        getBoard().add(nextTile, msg.getPosition());
        getBoard().mergeFeatures(nextTile);

        game.post(new TileEvent(TileEvent.PLACEMENT, getActivePlayer(), nextTile, position));
        next(ActionPhase.class);
    }
}
