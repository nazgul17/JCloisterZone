package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameSettings;

public class InnCapability extends Capability {

    private static final long serialVersionUID = 1L;

    @Override
    public Feature initFeature(GameSettings gs, String tileId, Feature feature, Element xml) {
        if (feature instanceof Road) {
            feature = ((Road) feature).setInn(attributeBoolValue(xml, "inn"));
        }
        return feature;
    }
}
