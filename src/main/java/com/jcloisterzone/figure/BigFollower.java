package com.jcloisterzone.figure;

import com.jcloisterzone.Player;

public class BigFollower extends Follower {

    private static final long serialVersionUID = -5506815500027084904L;

    public BigFollower(Integer idSuffix, Player player) {
        super(idSuffix, player);
    }

    @Override
    public int getPower() {
        return 2;
    }

}
