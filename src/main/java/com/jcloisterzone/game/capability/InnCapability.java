package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;

public class InnCapability extends Capability {

    @Override
    public Feature initFeature(String tileId, Feature feature, Element xml) {
        if (feature instanceof Road) {
            feature = ((Road) feature).setInn(attributeBoolValue(xml, "inn"));
        }
        return feature;
    }
}
