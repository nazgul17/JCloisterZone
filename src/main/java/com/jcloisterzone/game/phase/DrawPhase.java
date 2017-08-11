package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePackState;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.event.play.TileDiscardedEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;

import io.vavr.Tuple2;


public class DrawPhase extends ServerAwarePhase {

    private static final String DEBUG_END_OF_PACK = ".";

    private List<String> debugTiles;

    public DrawPhase(Game game, GameController controller) {
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
            //IMMUTABLE TODO
    //        if (bazaarCap != null) {
    //            Tile tile = bazaarCap.drawNextTile();
    //            if (tile != null) {
    //                nextTile(tile);
    //                return;
    //            }
    //        }

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
            if (state.getBoard().getTilePlacements(tile).isEmpty()) {
                state = state
                    .setDiscardedTiles(state.getDiscardedTiles().append(tile))
                    .appendEvent(new TileDiscardedEvent(tile));

                //if (riverCap != null) riverCap.turnPartCleanUp(); //force group activation if neeeded
            } else {
                toggleClock(state.getTurnPlayer());
                next(state);
                return;
            }
        }
    }
}
