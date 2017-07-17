package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Game;

public class Builder extends Special {

    private static final long serialVersionUID = 1L;

    public Builder(Player player) {
        super(null, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(Feature f) {
        if (!(f instanceof City || f instanceof Road) ) {
            return new DeploymentCheckResult("Builder must be placed in city or on road only.");
        }
        Completable cf = (Completable) f;
        if (cf.isCompleted()) {
            return new DeploymentCheckResult("Feature is completed.");
        }
        if (cf.getMeeples().find(m -> Follower.class.isInstance(m)).isEmpty()) {
            return new DeploymentCheckResult("Feature is not occupied by follower.");
        }
        return super.isDeploymentAllowed(f);
    }

}
