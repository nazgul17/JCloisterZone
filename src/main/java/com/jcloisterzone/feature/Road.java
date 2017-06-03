package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.visitor.score.RoadScoreContext;
import com.jcloisterzone.game.Game;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;

public class Road extends CompletableFeature<Road> {

    private final boolean inn;
    private final Map<FeaturePointer, TunnelEnd> tunnelEnds;

    public Road(Game game, List<FeaturePointer> places, List<Edge> openEdges) {
        this(game, places, openEdges, false, HashMap.<FeaturePointer, TunnelEnd>empty());
    }

    public Road(Game game, List<FeaturePointer> places, List<Edge> openEdges, boolean inn, Map<FeaturePointer, TunnelEnd> tunnelEnds) {
        super(game, places, openEdges);
        this.inn = inn;
        this.tunnelEnds = tunnelEnds;
    }

    @Override
    public Road merge(Road road) {
        return new Road(
            game,
            mergePlaces(road),
            mergeEdges(road),
            inn || road.inn,
            mergeTunnelEnds(road)
        );
    }

    @Override
    public Road placeOnBoard(Position pos, Rotation rot) {
        return new Road(
            game,
            placeOnBoardPlaces(pos, rot),
            placeOnBoardEdges(pos, rot),
            inn,
            placeOnBoardTunnelEnds(pos, rot)
        );
    }

    public boolean isInn() {
        return inn;
    }

    public Road setInn(boolean inn) {
        if (this.inn == inn) return this;
        return new Road(game, places, openEdges, inn, tunnelEnds);
    }

    public Map<FeaturePointer, TunnelEnd> getTunnelEnds() {
        return tunnelEnds;
    }

    public Road setTunnelEnds(Map<FeaturePointer, TunnelEnd> tunnelEnds) {
        return new Road(game, places, openEdges, inn, tunnelEnds);
    }

//
//    public boolean isTunnelEnd() {
//        return tunnelEnd != 0;
//    }
//
//    public boolean isTunnelOpen() {
//        return tunnelEnd == OPEN_TUNNEL;
//    }
//
//    public void setTunnelEdge(MultiTileFeature f) {
//        edges[edges.length - 1] = f;
//    }

    @Override
    public RoadScoreContext getScoreContext() {
        return new RoadScoreContext(game);
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.ROAD;
    }

    public static String name() {
        return _("Road");
    }

    // immutable helpers

    protected Map<FeaturePointer, TunnelEnd> mergeTunnelEnds(Road road) {
        return tunnelEnds.merge(road.tunnelEnds);
    }

    protected Map<FeaturePointer, TunnelEnd> placeOnBoardTunnelEnds(Position pos, Rotation rot) {
        return tunnelEnds.mapKeys(fp -> fp.rotateCW(rot).translate(pos));
    }
}
