package com.jcloisterzone.game.phase;

import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.reducers.PlaceTile;

import io.vavr.collection.Vector;

//TODO should be merged with DrawPhase ?
public class TilePhase extends Phase {

    private final BridgeCapability bridgeCap;

    public TilePhase(Game game) {
        super(game);
        bridgeCap = game.getCapability(BridgeCapability.class);
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

//    @Override
//    public void loadGame(Snapshot snapshot) {
//         String tileId = snapshot.getNextTile();
//         Tile tile = game.getTilePack().drawTile(tileId);
//         game.setCurrentTile(tile);
//         game.getBoard().refreshAvailablePlacements(tile);
//         game.post(new TileEvent(TileEvent.DRAW, getActivePlayer(), tile, null));
//    }

    @Override
    public void placeTile(Rotation rot, Position pos) {
        game.markUndo();
        GameState state = game.getState();

        TileDefinition tile = state.getDrawnTile();

        //IMMUTABLE TODO bridge
        //boolean bridgeRequired = bridgeCap != null && !getBoard().isPlacementAllowed(tile, p);

        state = (new PlaceTile(tile, pos, rot)).apply(state);
        state = state.setPlayerActions(null);

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
