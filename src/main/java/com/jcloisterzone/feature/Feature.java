package com.jcloisterzone.feature;


import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;

import io.vavr.collection.List;
import io.vavr.collection.Stream;

public interface Feature {

    List<FeaturePointer> getPlaces();
    Feature placeOnBoard(Position pos, Rotation rot);
    Stream<MeepleAttributes> getMeeples();

}
