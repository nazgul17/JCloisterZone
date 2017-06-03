package com.jcloisterzone.feature;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Game;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;

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

    protected List<Edge> mergeEdges(CompletableFeature obj) {
        Set<Edge> s1 = HashSet.ofAll(openEdges);
        Set<Edge> s2 = HashSet.ofAll(obj.openEdges);
        Set<Edge> connectedEdges = s1.intersect(s2);
        return openEdges.removeAll(connectedEdges)
            .appendAll(obj.openEdges.removeAll(connectedEdges));
    }

    protected List<Edge> placeOnBoardEdges(Position pos, Rotation rot) {
        return openEdges.map(edge -> edge.rotateCW(Position.ZERO, rot).translate(pos));
    }

}