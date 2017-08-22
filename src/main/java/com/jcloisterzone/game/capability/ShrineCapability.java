package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;


public final class ShrineCapability extends Capability<Void> {

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof Cloister) {
            ((Cloister)feature).setShrine(attributeBoolValue(xml, "shrine"));
        }
    }

    public void makeCloisterChallenged(Cloister cloister) {
        boolean first = true;
        for (Meeple m : cloister.getMeeples()) {
            if (first) {
                game.post(new ScoreEvent(cloister, 0, PointCategory.CLOISTER, m));
                first = false;
            }
            m.undeploy();
        }
    }


    public void resolveChallengedCloisters(Cloister cloister) {
        Position p = cloister.getTile().getPosition();
        for (Tile nt : game.getBoard().getAdjacentAndDiagonalTiles2(p)) {
            if (nt.hasCloister()) {
                Cloister nextCloister = nt.getCloister();
                if (cloister.isShrine() ^ nextCloister.isShrine()) {
                    //opposite cloisters
                    if (nextCloister.isOpen()) {
                        makeCloisterChallenged(nextCloister);
                    }
                }
            }
        }
    }

    @Override
    public GameState onCompleted(GameState state, Completable feature) {
        if (feature instanceof Cloister) {
            resolveChallengedCloisters((Cloister) feature);
        }
    }

    @Override
    public boolean isTilePlacementAllowed(Tile tile, Position p) {
        if (tile.hasCloister()) {
            Cloister opposite = null;
            for (Tile nt: getBoard().getAdjacentAndDiagonalTiles2(p)) {
                if (nt.hasCloister()) {
                    if  (tile.getCloister().isShrine() ^ nt.getCloister().isShrine()) {
                        if (opposite == null) {
                            opposite = nt.getCloister();
                        } else {
                            //second opposite, not allowed by rules
                            return false;
                        }
                    }
                }
            }
            if (opposite != null) {
                //we must also check if second "same" feature is not place next to "oposite"
                for (Tile nt: getBoard().getAdjacentAndDiagonalTiles2(opposite.getTile().getPosition())) {
                    if (nt.hasCloister()) {
                        if  (!(tile.getCloister().isShrine() ^ nt.getCloister().isShrine())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

}
