package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.BuilderCapability.BuilderState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.PlaceTileMessage;

public class AbbeyPhase extends ServerAwarePhase {

    private AbbeyCapability abbeyCap;
    private BazaarCapability bazaarCap;
    private BuilderCapability builderCap;

    public AbbeyPhase(Game game, GameController controller) {
        super(game, controller);
        abbeyCap = game.getCapability(AbbeyCapability.class);
        bazaarCap = game.getCapability(BazaarCapability.class);
        builderCap = game.getCapability(BuilderCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(AbbeyCapability.class);
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
