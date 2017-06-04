package com.jcloisterzone.figure;

import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.game.Game;

public class BigFollower extends Follower {

    private static final long serialVersionUID = -5506815500027084904L;

    public BigFollower(Game game, Integer idSuffix, PlayerAttributes player) {
        super(game, idSuffix, player);
    }

    @Override
    public int getPower() {
        return 2;
    }

}
