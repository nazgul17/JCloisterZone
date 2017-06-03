package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;

public abstract class CompletableFeature<T extends CompletableFeature<?>> extends TileFeature implements Completable, MultiTileFeature<T> {

    protected final List<Edge> openEdges;

    public CompletableFeature(Game game, List<FeaturePointer> places, List<Edge> openEdges) {
        super(game, places);
        this.openEdges = openEdges;
    }

    @Override
    public boolean isOpen() {
        return !getOpenEdges().isEmpty();
    }

    public List<Edge> getOpenEdges() {
        return openEdges;
    }


 // immutable helpers

    protected List<Edge> mergeEdges(T obj) {
        return openEdges.appendAll(obj.openEdges).distinct();
    }

    protected List<Edge> placeOnBoardEdges(Position pos, Rotation rot) {
        return openEdges.map(edge -> edge.rotateCW(Position.ZERO, rot).translate(pos));
    }

}