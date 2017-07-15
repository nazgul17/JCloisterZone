package com.jcloisterzone.game.phase;

import java.util.Map.Entry;
import java.util.Set;

import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.reducers.PlaceTile;

import io.vavr.collection.Vector;

public class TilePhase extends Phase {

    private final BridgeCapability bridgeCap;

    public TilePhase(Game game) {
        super(game);
        bridgeCap = game.getCapability(BridgeCapability.class);
    }

    @Override
    public void enter() {
        TileDefinition tile = game.getState().getDrawnTile();
        TilePlacementAction action = new TilePlacementAction(tile, getBoard().getTilePlacements(tile).toSet());

        game.replaceState(
            state -> state.setPlayerAcrions(new ActionsState(
                game.getTurnPlayer(),
                Vector.of(action),
                false
            ))
        );
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
        TileDefinition tile = game.getState().getDrawnTile();

        //IMMUTABLE TODO bridge
        //boolean bridgeRequired = bridgeCap != null && !getBoard().isPlacementAllowed(tile, p);

        game.replaceState(new PlaceTile(tile, pos, rot));

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

        next();
    }
}
