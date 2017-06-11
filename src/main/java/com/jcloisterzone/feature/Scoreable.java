package com.jcloisterzone.feature;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.feature.visitor.score.ScoreContext;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Game;

import io.vavr.collection.Set;

public interface Scoreable extends Feature {

    PointCategory getPointCategory();

    Set<Player> getOwners();
    int getPoints(Player player);

    Follower getSampleFollower(Player player);



}
