package com.jcloisterzone.feature;


import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.List;

public interface Feature {

    List<FeaturePointer> getPlaces();
    Feature placeOnBoard(Position pos);

}
