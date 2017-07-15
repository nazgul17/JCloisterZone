package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class YagaCapability extends Capability {

    @Override
    public Feature initFeature(String tileId, Feature feature, Element xml) {
        if (feature instanceof Cloister) {
            feature = ((Cloister)feature).setYagaHut(attributeBoolValue(xml, "yaga"));
        }
        return feature;
    }

}
