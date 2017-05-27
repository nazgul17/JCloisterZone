package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;

public abstract class CompletableFeature<T extends CompletableFeature<?>> extends MultiTileFeature<T> implements Completable {

    public CompletableFeature(Game game, List<FeaturePointer> places, List<Edge> openEdges) {
        super(game, places, openEdges);
    }

    @Override
    public boolean isOpen() {
        return !getOpenEdges().isEmpty();
    }

}