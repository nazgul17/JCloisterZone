package com.jcloisterzone.figure;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;

@Immutable
public class BigFollower extends Follower {

    private static final long serialVersionUID = 1L;

    public BigFollower(String id, Player player) {
        super(id, player);
    }

    @Override
    public int getPower() {
        return 2;
    }

}
