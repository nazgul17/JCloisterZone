package com.jcloisterzone.feature;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;

public class Quarter extends TileFeature {

    //final Class<? extends Feature> targetFeature;

    public Quarter(Game game, List<FeaturePointer> places) {
        super(game, places);
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        throw new UnsupportedOperationException();
    }
}
