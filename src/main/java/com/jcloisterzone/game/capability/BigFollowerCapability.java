package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;

public class BigFollowerCapability extends Capability {

    public BigFollowerCapability(Game game) {
        super(game);
    }

    @Override
    public List<Follower> createPlayerFollowers(PlayerAttributes p) {
        return List.of((Follower) new BigFollower(null, p));
    }
}
