package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Set;

public interface Scoreable extends Feature {

    PointCategory getPointCategory();

    Set<Player> getOwners(GameState state);
    int getPoints(GameState state, Player player);

    Follower getSampleFollower(GameState state, Player player);



}
