package com.jcloisterzone.game.capability;

import static com.jcloisterzone.XMLUtils.attributeBoolValue;

import org.w3c.dom.Element;

import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameSettings;

public class PrincessCapability extends Capability {

    @Override
    public Feature initFeature(GameSettings gs, String tileId, Feature feature, Element xml) {
        if (feature instanceof City && attributeBoolValue(xml, "princess")) {
            feature = ((City)feature).setPrincess(true);
        }
        return feature;
    }

//    @Override
//    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
//        City c = getCurrentTile().getCityWithPrincess();
//        if (c == null || ! c.walk(new IsOccupied().with(Follower.class))) return;
//        Feature cityRepresentative = c.getMaster();
//
//        PrincessAction princessAction = null;
//        for (Meeple m : game.getDeployedMeeples()) {
//            if (!(m.getFeature() instanceof City)) continue;
//            if (m.getFeature().getMaster().equals(cityRepresentative) && m instanceof Follower) {
//                if (princessAction == null) {
//                    princessAction = new PrincessAction();
//                    actions.add(princessAction);
//                }
//                princessAction.add(new MeeplePointer(m));
//            }
//        }
//    }
}
