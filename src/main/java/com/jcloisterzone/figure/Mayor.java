package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.state.GameState;

@Immutable
public class Mayor extends Follower {

    private static final long serialVersionUID = 1L;

    public Mayor(String id, Player player) {
        super(id, player);
    }

    @Override
    public int getPower(GameState state, Scoreable feature) {
        return ((City)feature).getPennants();
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp, Feature feature) {
        if (!(feature instanceof City)) {
            return new DeploymentCheckResult("Mayor must be placed in city only.");
        }
        return super.isDeploymentAllowed(state, fp, feature);
    }

}
