package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarCapabilityModel;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameState.Flag;
import com.jcloisterzone.reducers.PlaceTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PlaceTileMessage;

import io.vavr.collection.Vector;

//TODO should be merged with DrawPhase ?
public class TilePhase extends ServerAwarePhase {

    public TilePhase(Game game, GameController gc) {
        super(game, gc);
    }

    @Override
    public void enter(GameState state) {
        TileDefinition tile = state.getDrawnTile();
        TilePlacementAction action = new TilePlacementAction(tile, state.getBoard().getTilePlacements(tile).toSet());

        state = state.setPlayerActions(new ActionsState(
            state.getTurnPlayer(),
            Vector.of(action),
            false
        ));

        toggleClock(state.getTurnPlayer());
        promote(state);
    }

    @WsSubscribe
    public void handlePlaceTile(PlaceTileMessage msg) {
        game.markUndo();
        GameState state = game.getState();

        TileDefinition tile = state.getDrawnTile();

        //IMMUTABLE TODO bridge
        //boolean bridgeRequired = bridgeCap != null && !getBoard().isPlacementAllowed(tile, p);

        assert tile.getId().equals(msg.getTileId());

        state = (new PlaceTile(tile, msg.getPosition(), msg.getRotation())).apply(state);
        state = clearActions(state);

        //IMMUTABLE TODO bridge
//        if (tile.getTower() != null) {
//            game.getCapability(TowerCapability.class).registerTower(p);
//        }

//        if (bridgeRequired) {
//            BridgeAction action = bridgeCap.prepareMandatoryBridgeAction();
//
//            assert action.getOptions().size() == 1;
//            FeaturePointer bp = action.getOptions().iterator().next();
//
//            bridgeCap.decreaseBridges(getActivePlayer());
//            bridgeCap.deployBridge(bp.getPosition(), bp.getLocation(), true);
//        }
        //getBoard().mergeFeatures(tile);

        if (tile.getTrigger() == TileTrigger.BAZAAR) {
            BazaarCapabilityModel model = state.getCapabilities().getModel(BazaarCapability.class);
            //Do not trigger another auction is current is not resolved
            if (model.getSupply() == null) {
                state = state.addFlag(Flag.BAZAAR_AUCTION);
            }
        }

        next(state);
    }
}
