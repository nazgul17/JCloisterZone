package com.jcloisterzone.feature;


import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;

public abstract class MultiTileFeature<T extends MultiTileFeature<?>> extends TileFeature implements Scoreable {

    protected final List<Edge> openEdges;

    public MultiTileFeature(Game game, List<FeaturePointer> places, List<Edge> openEdges) {
        super(game, places);
        this.openEdges = openEdges;
    }

    public abstract T merge(T f);


    public List<Edge> getOpenEdges() {
        return openEdges;
    }

    // immutable helpers

    protected List<Edge> mergeEdges(T obj) {
        return openEdges.appendAll(obj.openEdges).distinct();
    }

    protected List<Edge> placeOnBoardEdges(Position pos) {
        return openEdges.map(edge -> edge.translate(pos));
    }

}
