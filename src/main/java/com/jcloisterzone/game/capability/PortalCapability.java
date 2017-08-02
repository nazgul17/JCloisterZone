package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameState.Flag;

@Immutable
public class PortalCapability extends Capability {

    private static final long serialVersionUID = 1L;

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("portal").getLength() > 0) {
            tile = tile.setTileTrigger(TileTrigger.PORTAL);
        }
        return tile;
    }

    public boolean isPortalUsed(GameState state) {
        return state.hasFlag(Flag.PORTAL);
    }

//    @Override
//    public void handleEvent(Event event) {
//        if (event.isUndo() && event instanceof MeepleEvent) {
//            MeepleEvent ev = (MeepleEvent) event;
//            if (ev.getTo() == null && game.getCurrentTile().hasTrigger(TileTrigger.PORTAL)) {
//                portalUsed = false;
//            }
//        }
//    }
/*

    @Override
    public Set<FeaturePointer> extendFollowOptions(Set<FeaturePointer> followerOptions) {
        if (game.getCurrentTile().hasTrigger(TileTrigger.PORTAL)) {
            if (game.getActivePlayer().hasFollower()) {
                return prepareMagicPortal(followerOptions);
            }
        }
        return followerOptions;
    }

    public Set<FeaturePointer> prepareMagicPortal(Set<FeaturePointer> followerOptions) {
        if (portalUsed) return followerOptions;

        java.util.Set<FeaturePointer> mutable = followerOptions.toJavaSet();
        getBoard().getPlacedTiles()
            //current tile is already contained in original followerOptions
            .filter(tile -> !tile.equals(game.getCurrentTile()))
            .forEach(tile -> {
                Set<FeaturePointer> ptrs = game.prepareFollowerLocations(tile, true);
                mutable.addAll(ptrs.toJavaSet());
            });
        return HashSet.ofAll(mutable);
    }



*/
}
