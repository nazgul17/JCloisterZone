package com.jcloisterzone.feature;


import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;

import io.vavr.collection.List;
import io.vavr.collection.Stream;

public interface Feature {

    List<FeaturePointer> getPlaces();
    Feature placeOnBoard(Position pos, Rotation rot);
    Stream<Meeple> getMeeples();

    default boolean isOccupied() {
        return !getMeeples().isEmpty();
    }

    default boolean isOccupiedBy(PlayerAttributes p) {
        return !getMeeples().filter(m -> m.getPlayer().equals(p)).isEmpty();
    }
}
