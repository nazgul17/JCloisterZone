package com.jcloisterzone.figure;

import com.jcloisterzone.PlayerAttributes;

public class BigFollower extends Follower {

    private static final long serialVersionUID = -5506815500027084904L;

    public BigFollower(Integer idSuffix, PlayerAttributes player) {
        super(idSuffix, player);
    }

    @Override
    public int getPower() {
        return 2;
    }

}
