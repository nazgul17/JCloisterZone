package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.PlaceTile;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PlaceTileMessage;

import io.vavr.collection.Vector;

//TODO should be merged with DrawPhase ?
public class TilePhase extends Phase {

    public TilePhase(Game game) {
        super(game);
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

        next(state);
    }
}
