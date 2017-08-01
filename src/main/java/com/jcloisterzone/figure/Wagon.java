package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.game.Game;

public class Wagon extends Follower {

    private static final long serialVersionUID = 1L;

    public Wagon(String id, Player player) {
        super(id, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(Feature f) {
        if (f instanceof Tower) {
            return new DeploymentCheckResult("Cannot place wagon on the tower.");
        }
        if (f instanceof Farm) {
            return new DeploymentCheckResult("Cannot place wagon on the farm.");
        }
        return super.isDeploymentAllowed(f);
    }
}
