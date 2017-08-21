package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;


public final class SiegeCapability extends Capability<Void> {

    public static final String UNDEPLOY_ESCAPE = "escape";

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (feature instanceof City && attributeBoolValue(xml, "besieged")) {
            City city = (City) feature;
            city.setBesieged(true);
            tileId.setTrigger(TileTrigger.BESIEGED);
        }
    }
}
