package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.game.GameState;

public abstract class Follower extends Meeple {
    private static final long serialVersionUID = 1L;

    //private boolean inPrison;

    public Follower(Integer idSuffix, Player player) {
        super(idSuffix, player);
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

}
