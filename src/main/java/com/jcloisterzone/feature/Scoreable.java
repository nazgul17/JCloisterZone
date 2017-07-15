package com.jcloisterzone.feature;

import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.GameState;

import io.vavr.collection.Set;

public interface Scoreable extends Feature {

    PointCategory getPointCategory();

    Set<PlayerAttributes> getOwners(GameState state);
    int getPoints(GameState state, PlayerAttributes player);

    Follower getSampleFollower(GameState state, PlayerAttributes player);



}
