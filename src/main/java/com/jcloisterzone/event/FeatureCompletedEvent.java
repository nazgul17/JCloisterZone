package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.feature.Completable;

public class FeatureCompletedEvent extends PlayEvent {

    private final Completable feature;

    public FeatureCompletedEvent(Player triggeringPlayer, Completable feature) {
        super(triggeringPlayer, null);
        this.feature = feature;
    }

    public Completable getFeature() {
        return feature;
    }

    @Override
    public String toString() {
        return super.toString() + " feature:" + feature;
    }

}
