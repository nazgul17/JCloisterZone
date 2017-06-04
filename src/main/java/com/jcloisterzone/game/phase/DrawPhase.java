package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.AbbeyCapability;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.RiverCapability;
import com.jcloisterzone.ui.GameController;


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

    private boolean makeDebugDraw() {
        if (debugTiles != null && debugTiles.size() > 0) { //for debug purposes only
//            String tileId = debugTiles.remove(0);
//            if (tileId.equals(DEBUG_END_OF_PACK)) {
//                next(GameOverPhase.class);
//                return true;
//            }
//            TilePack tilePack = getTilePack();
//            Tile tile = tilePack.drawTile(tileId);
//            if (tile == null) {
//                logger.warn("Invalid debug draw id: " + tileId);
//            } else {
//                boolean riverActive = tilePack.getGroupState("river-start") == TileGroupState.ACTIVE || tilePack.getGroupState("river") == TileGroupState.ACTIVE;
//                if (game.hasCapability(RiverCapability.class) && tile.getRiver() == null && riverActive) {
//                    game.getCapability(RiverCapability.class).activateNonRiverTiles();
//                    tilePack.setGroupState("river-start", TileGroupState.RETIRED);
//                    game.setCurrentTile(tile); //recovery from lake placement
//                }
//                nextTile(tile);
//                return true;
//            }
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
        TilePack tilePack = game.getTilePack();
        if (tilePack.isEmpty()) {
            if (abbeyCap != null && !getActivePlayer().equals(abbeyCap.getAbbeyRoundLastPlayer())) {
                if (abbeyCap.getAbbeyRoundLastPlayer() == null) {
                    abbeyCap.setAbbeyRoundLastPlayer(game.getPrevPlayer(getActivePlayer()));
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
        TileDefinition tile = tilePack.drawTile(rndIndex);
        nextTile(tile);
    }

    private void nextTile(TileDefinition tile) {
        if (game.getBoard().getAvailablePlacements(tile).isEmpty()) {
            game.replaceState(state ->
                state
                .setDiscardedTiles(state.getDiscardedTiles().append(tile))
                .setDrawnTile(null)
            );
            if (riverCap != null) riverCap.turnPartCleanUp(); //force group activation if neeeded
            next(DrawPhase.class);
            return;
        }
        toggleClock(getActivePlayer());
        //TODO Separate draw event instead
        game.post(new TileEvent(TileEvent.DRAW, getActivePlayer(), tile, null, null));
        next();
    }
}
