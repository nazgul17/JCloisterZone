package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.PrincessAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameSettings;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.GameState.Flag;

import io.vavr.collection.Set;

public class PrincessCapability extends Capability {

    @Override
    public Feature initFeature(GameSettings gs, String tileId, Feature feature, Element xml) {
        if (feature instanceof City && attributeBoolValue(xml, "princess")) {
            feature = ((City)feature).setPrincess(true);
        }
        return feature;
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        if (state.getFlags().contains(Flag.PRINCESS_USED)) {
            return state;
        }

        Tile tile = state.getBoard().getLastPlaced();
        Set<MeeplePointer> options = tile.getScoreables(false).filter(t -> {
            if (t._2 instanceof City) {
                City part = (City) tile.getInitialFeaturePartOf(t._1);
                return part.isPrincess();
            } else {
                return false;
            }
        }).flatMap(featureTuple -> {
            City cityWithPrincess = (City) featureTuple._2;
            return cityWithPrincess.getFollowers2(state).map(t -> new MeeplePointer(t._2, t._1.getId()));
        }).toSet();

        if (options.isEmpty()) {
            return state;
        }

        ActionsState as = state.getPlayerActions();
        return state.setPlayerActions(as.appendAction(new PrincessAction(options)));
    }
}
