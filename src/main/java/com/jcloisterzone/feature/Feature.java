package com.jcloisterzone.feature;


import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.GameState;

import io.vavr.Predicates;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

public interface Feature {

    List<FeaturePointer> getPlaces();
    Feature placeOnBoard(Position pos, Rotation rot);
    Stream<Meeple> getMeeples(GameState state);

    default Stream<Follower> getFollowers(GameState state) {
        return getMeeples(state)
            .filter(Predicates.instanceOf(Follower.class))
            .map(m -> (Follower) m);
    }

    default Stream<Special> getSpecialMeeples(GameState state) {
        return getMeeples(state)
            .filter(Predicates.instanceOf(Special.class))
            .map(m -> (Special) m);
    }


    default boolean isOccupied(GameState state) {
        return !getMeeples(state).isEmpty();
    }

    default boolean isOccupiedBy(GameState state, PlayerAttributes p) {
        return !getMeeples(state).filter(m -> m.getPlayer().equals(p)).isEmpty();
    }
}
