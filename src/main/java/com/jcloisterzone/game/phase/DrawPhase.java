package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;

import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePackState;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.event.play.TileDiscardedEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.ui.GameController;

import io.vavr.Tuple2;


public class DrawPhase extends ServerAwarePhase {

    private static final String DEBUG_END_OF_PACK = ".";

    private List<String> debugTiles;
    private final BazaarCapability bazaarCap;
    private final AbbeyCapability abbeyCap;
    private final RiverCapability riverCap;

    public DrawPhase(Game game, GameController controller) {
        super(game, controller);
        DebugConfig debugConfig = getDebugConfig();
        if (debugConfig != null) {
            List<String> draw = debugConfig.getDraw();
            if (draw != null && !draw.isEmpty()) {
                debugTiles = new ArrayList<String>(draw);
            }
        }
        bazaarCap = game.getCapability(BazaarCapability.class);
        abbeyCap = game.getCapability(AbbeyCapability.class);
        riverCap = game.getCapability(RiverCapability.class);
    }

    public TileDefinition drawTile(int index) {
        TilePackState tps = game.getState().getTilePack();
        Tuple2<TileDefinition, TilePackState> t = tps.drawTile(index);
        game.replaceState(state -> state.setTilePack(t._2).setDrawnTile(t._1));
        return t._1;
    }

    public TileDefinition drawTile(String tileId) {
        TilePackState tps = game.getState().getTilePack();
        Tuple2<TileDefinition, TilePackState> t = tps.drawTile(tileId);
        game.replaceState(state -> state.setTilePack(t._2).setDrawnTile(t._1));
        return t._1;
    }

    private boolean makeDebugDraw() {
        if (debugTiles != null && debugTiles.size() > 0) { //for debug purposes only
            String tileId = debugTiles.remove(0);
            if (tileId.equals(DEBUG_END_OF_PACK)) {
                next(GameOverPhase.class);
                return true;
            }
            try {
                TileDefinition tile = drawTile(tileId);
//                boolean riverActive = tilePack.getGroupState("river-start") == TileGroupState.ACTIVE || tilePack.getGroupState("river") == TileGroupState.ACTIVE;
//                if (game.hasCapability(RiverCapability.class) && tile.getRiver() == null && riverActive) {
//                    game.getCapability(RiverCapability.class).activateNonRiverTiles();
//                    tilePack.setGroupState("river-start", TileGroupState.RETIRED);
//                    game.setCurrentTile(tile); //recovery from lake placement
//                }
                nextTile(tile);
                return true;
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid debug draw id: " + tileId);
            }
        }
        return false;
    }

    @Override
    public void enter() {
        //IMMUTABLE TODO
//        if (bazaarCap != null) {
//            Tile tile = bazaarCap.drawNextTile();
//            if (tile != null) {
//                nextTile(tile);
//                return;
//            }
//        }

        GameState state = game.getState();
        TilePackState tilePack = state.getTilePack();
        if (tilePack.isEmpty()) {
            if (abbeyCap != null && !state.getActivePlayer().equals(abbeyCap.getAbbeyRoundLastPlayer())) {
                if (abbeyCap.getAbbeyRoundLastPlayer() == null) {
                    abbeyCap.setAbbeyRoundLastPlayer(game.getPrevPlayer(state.getActivePlayer()));
                }
                next(CleanUpTurnPartPhase.class);
                return;
            }
            next(GameOverPhase.class);
            return;
        }
        if (makeDebugDraw()) {
            return;
        }
        int rndIndex = game.getRandom().nextInt(tilePack.size());
        TileDefinition tile = drawTile(rndIndex);
        nextTile(tile);
    }

    private void nextTile(TileDefinition tile) {
        GameState state = game.getState();
        if (state.getBoard().getAvailablePlacements(tile).isEmpty()) {
            state = state
                .setDiscardedTiles(state.getDiscardedTiles().append(tile))
                .setDrawnTile(null)
                .appendEvent(new TileDiscardedEvent(tile));

            game.replaceState(state);

            if (riverCap != null) riverCap.turnPartCleanUp(); //force group activation if neeeded
            next(DrawPhase.class);
            return;
        }
        toggleClock(state.getActivePlayer());
        //TODO Separate draw event instead
//        state = state.appendEvent(
//            new TileEvent(TileEvent.DRAW, getActivePlayer(), tile, null, null)
//        );
        game.replaceState(state);
        next();
    }
}
