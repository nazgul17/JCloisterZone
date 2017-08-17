package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePackState;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.event.play.TileDiscardedEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarCapabilityModel;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameState.Flag;
import com.jcloisterzone.reducers.PlaceTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PlaceTileMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Queue;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;


public class TilePhase extends ServerAwarePhase {

    private static final String DEBUG_END_OF_PACK = ".";

    private List<String> debugTiles;

    public TilePhase(Game game, GameController controller) {
        super(game, controller);
        DebugConfig debugConfig = getDebugConfig();
        if (debugConfig != null) {
            List<String> draw = debugConfig.getDraw();
            if (draw != null && !draw.isEmpty()) {
                debugTiles = new ArrayList<String>(draw);
            }
        }
    }

    public GameState drawTile(GameState state, int index) {
        TilePackState tps = state.getTilePack();
        Tuple2<TileDefinition, TilePackState> t = tps.drawTile(index);
        return state.setTilePack(t._2).setDrawnTile(t._1);
    }

    public GameState drawTile(GameState state, String tileId) {
        TilePackState tps = state.getTilePack();
        Tuple2<TileDefinition, TilePackState> t = tps.drawTile(tileId);
        return state.setTilePack(t._2).setDrawnTile(t._1);
    }

    private String pullDebugDrawTileId() {
//        boolean riverActive = tilePack.getGroupState("river-start") == TileGroupState.ACTIVE || tilePack.getGroupState("river") == TileGroupState.ACTIVE;
//        if (game.hasCapability(RiverCapability.class) && tile.getRiver() == null && riverActive) {
//            game.getCapability(RiverCapability.class).activateNonRiverTiles();
//            tilePack.setGroupState("river-start", TileGroupState.RETIRED);
//            game.setCurrentTile(tile); //recovery from lake placement
//        }
        if (debugTiles != null && debugTiles.size() > 0) { //for debug purposes only
            return debugTiles.remove(0);
        }
        return null;
    }


    private boolean isDebugForcedEnd() {
        return debugTiles != null && !debugTiles.isEmpty() && debugTiles.get(0).equals(DEBUG_END_OF_PACK);
    }

    @Override
    public void enter(GameState state) {
        for (;;) {
            BazaarCapabilityModel bazaarModel = state.getCapabilities().getModel(BazaarCapability.class);
            if (bazaarModel != null) {
                Queue<BazaarItem> supply = bazaarModel.getSupply();
                if (supply != null) {
                    Tuple2<BazaarItem, Queue<BazaarItem>> t = supply.dequeue();
                    BazaarItem item = t._1;
                    supply = t._2;
                    bazaarModel = bazaarModel.setSupply(supply);
                    state = state.setCapabilityModel(BazaarCapability.class, bazaarModel);
                    state = state.setDrawnTile(item.getTile());
                    next(state);
                    return;
                }
            }

            TilePackState tilePack = state.getTilePack();
            boolean packIsEmpty = tilePack.isEmpty() || isDebugForcedEnd();

            //Abbey special case, every player has opportunity to place own abbey at the end.
            if (packIsEmpty && state.getCapabilities().contains(AbbeyCapability.class)) {
                Integer endPlayerIdx = state.getCapabilities().getModel(AbbeyCapability.class);
                Player turnPlayer = state.getTurnPlayer();
                if (endPlayerIdx == null) {
                    //tile pack has been depleted jut now
                    endPlayerIdx = turnPlayer.getPrevPlayer(state).getIndex();
                    state = state.setCapabilityModel(AbbeyCapability.class, endPlayerIdx);
                }
                if (endPlayerIdx != turnPlayer.getIndex()) {
                    next(state, CleanUpTurnPartPhase.class);
                    return;
                }
                // otherwise proceed to game over
            }

            // Tile Pack is empty
            if (packIsEmpty) {
                next(state, GameOverPhase.class);
                return;
            }

            // Handle forced debug draw
            String debugDrawTileId = pullDebugDrawTileId();
            boolean makeRegularDraw = true;
            if (debugDrawTileId != null) {
                try {
                    state = drawTile(state, debugDrawTileId);
                    makeRegularDraw = false;
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid debug draw id: " + debugDrawTileId);
                }
            }

            if (makeRegularDraw) {
                int rndIndex = game.getRandom().nextInt(tilePack.size());
                state = drawTile(state, rndIndex);
            }

            TileDefinition tile = state.getDrawnTile();
            Set<TilePlacement> placements = state.getBoard().getTilePlacements(tile).toSet();

            if (placements.isEmpty()) {
                state = state
                    .setDiscardedTiles(state.getDiscardedTiles().append(tile))
                    .appendEvent(new TileDiscardedEvent(tile));

                //if (riverCap != null) riverCap.turnPartCleanUp(); //force group activation if neeeded
            } else {
                TilePlacementAction action = new TilePlacementAction(tile, placements);

                state = state.setPlayerActions(new ActionsState(
                    state.getTurnPlayer(),
                    Vector.of(action),
                    false
                ));

                toggleClock(state.getTurnPlayer());
                promote(state);
                return;
            }
        }
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
