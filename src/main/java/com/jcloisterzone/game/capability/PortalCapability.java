package com.jcloisterzone.game.capability;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.SnapshotCorruptedException;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class PortalCapability extends Capability {

    boolean portalUsed = false;

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("portal").getLength() > 0) {
            tile = tile.setTileTrigger(TileTrigger.PORTAL);
        }
        return tile;
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

    @Override
    public void turnPartCleanUp() {
        portalUsed = false;
    }

    public boolean isPortalUsed() {
        return portalUsed;
    }

    public void setPortalUsed(boolean portalUsed) {
        this.portalUsed = portalUsed;
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (portalUsed) {
            node.setAttribute("portalUsed", "true");
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) throws SnapshotCorruptedException {
        if (XMLUtils.attributeBoolValue(node, "portalUsed")) {
            portalUsed = true;
        }
    }
}
