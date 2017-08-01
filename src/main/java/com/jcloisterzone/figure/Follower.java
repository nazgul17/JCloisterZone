package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.GameState;

@Immutable
public abstract class Follower extends Meeple {

    private static final long serialVersionUID = 1L;

    //private boolean inPrison;

    public Follower(String id, Player player) {
        super(id, player);
    }

    public int getPower() {
        return 1;
    }

    @Override
    public boolean canBeEatenByDragon(GameState state) {
        return !(getFeature(state) instanceof Castle);
    }

    public boolean isInPrison(GameState state) {
        //IMMUTABLE TOOD
        //return inPrison;
        return false;
    }

    @Override
    public boolean isInSupply(GameState state) {
        return !isInPrison(state) && super.isInSupply(state);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Feature feature) {
        if (feature.isOccupied(state)) {
            return new DeploymentCheckResult("Feature is occupied");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }

}
