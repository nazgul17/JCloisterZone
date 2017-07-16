package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.GameState;

import io.vavr.control.Option;

public class Pig extends Special {

    private static final long serialVersionUID = 1L;

    public Pig(Player player) {
        super(null, player);
    }

    @Override
    public DeploymentCheckResult isDeploymentAllowed(GameState state, FeaturePointer fp) {
        Feature farm= state.getBoard().get(fp);
        if (!(farm instanceof Farm)) {
            return new DeploymentCheckResult("Pig must be placed on a farm only.");
        }
        Option<Follower> follower = farm.getFollowers(state).find(f -> {
            return f.getPlayer().equals(getPlayer());
        });
        if (follower.isEmpty()) {
            return new DeploymentCheckResult("Feature is not occupied by follower.");
        }
        return super.isDeploymentAllowed(state, fp);
    }


}
