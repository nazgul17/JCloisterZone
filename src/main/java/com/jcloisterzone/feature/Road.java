package com.jcloisterzone.feature;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.visitor.score.RoadScoreContext;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;

import static com.jcloisterzone.ui.I18nUtils._;

public class Road extends CompletableFeature<Road> {

    private final boolean inn;
    /*
     * 0 - no tunnel -1 - open tunnel n - player token n+100 - player token B (2
     * players game)
     */
//    private int tunnelEnd;
//    public static final int OPEN_TUNNEL = -1;
    // IMMUTABLE TODO Tunnel


    public Road(Game game, List<FeaturePointer> places, List<Edge> openEdges, boolean inn) {
        super(game, places, openEdges);
        this.inn = inn;
    }

    @Override
    public Road merge(Road road) {
        return new Road(
            game,
            mergePlaces(road),
            mergeEdges(road),
            inn || road.inn
        );
    }

    @Override
    public Road placeOnBoard(Position pos) {
        return new Road(
            game,
            placeOnBoardPlaces(pos),
            placeOnBoardEdges(pos),
            inn
        );
    }

    public boolean isInn() {
        return inn;
    }

    public Road setInn(boolean inn) {
        return new Road(game, places, openEdges, inn);
    }

//    public int getTunnelEnd() {
//        return tunnelEnd;
//    }
//
//    public void setTunnelEnd(int tunnelEnd) {
//        this.tunnelEnd = tunnelEnd;
//    }
//
//    public boolean isTunnelEnd() {
//        return tunnelEnd != 0;
//    }
//
//    public boolean isTunnelOpen() {
//        return tunnelEnd == OPEN_TUNNEL;
//    }


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
}
