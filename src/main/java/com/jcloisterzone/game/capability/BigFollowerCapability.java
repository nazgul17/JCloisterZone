package com.jcloisterzone.game.capability;

import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Capability;

import io.vavr.collection.List;

public class BigFollowerCapability extends Capability {

    @Override
    public List<Follower> createPlayerFollowers(PlayerAttributes p) {
        return List.of((Follower) new BigFollower(null, p));
    }
}
