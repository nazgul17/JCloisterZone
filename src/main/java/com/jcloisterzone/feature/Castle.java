package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.List;

public class Castle extends ScoreableFeature {

    private static final long serialVersionUID = 1L;

    public Castle(List<FeaturePointer> places) {
        super(places);
        assert places.size() == 2;
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.CASTLE;
    }

    @Override
    public int getPoints(GameState state, Player player) {
        return 0;
    }

    public Edge getEdge() {
        return new Edge(places.get(0).getPosition(), places.get(1).getPosition());
    }



    //IMMUTABLE TODO

//    public Castle getSecondFeature() {
//        return (Castle) getEdges()[0];
//    }
//
//
//    public Position[] getCastleBase() {
//        Position[] positions = new Position[6];
//        positions[0] = getTile().getPosition();
//        positions[1] = getSecondFeature().getTile().getPosition();
//        return positions;
//    }
//
//    public Position[] getVicinity() {
//        Position[] vicinity = new Position[6];
//        vicinity[0] = getTile().getPosition();
//        vicinity[1] = getSecondFeature().getTile().getPosition();
//        if (vicinity[0].x == vicinity[1].x) {
//            vicinity[2] = vicinity[0].add(Location.W);
//            vicinity[3] = vicinity[0].add(Location.E);
//            vicinity[4] = vicinity[1].add(Location.W);
//            vicinity[5] = vicinity[1].add(Location.E);
//        } else {
//            vicinity[2] = vicinity[0].add(Location.N);
//            vicinity[3] = vicinity[0].add(Location.S);
//            vicinity[4] = vicinity[1].add(Location.N);
//            vicinity[5] = vicinity[1].add(Location.S);
//        }
//        return vicinity;
//    }

    public static String name() {
        return _("Castle");
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        throw new UnsupportedOperationException();
    }

}
