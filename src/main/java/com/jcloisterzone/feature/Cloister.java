package com.jcloisterzone.feature;


import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.visitor.score.CloisterScoreContext;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;

import static com.jcloisterzone.ui.I18nUtils._;


public class Cloister extends TileFeature implements Completable {

    protected final boolean shrine;
    protected final boolean monastery;
    protected final boolean yagaHut;

    public Cloister(Game game, List<FeaturePointer> places, boolean shrine, boolean monastery, boolean yagaHut) {
        super(game, places);
        this.shrine = shrine;
        this.monastery = monastery;
        this.yagaHut = yagaHut;
    }

    @Override
    public Feature placeOnBoard(Position pos) {
        return new Cloister(
            game,
            placeOnBoardPlaces(pos),
            shrine, monastery, yagaHut
        );
    }

    public boolean isShrine() {
        return shrine;
    }

    public Cloister setShrine(boolean shrine) {
        return new Cloister(game, places, shrine, monastery, yagaHut);
    }

    public boolean isMonastery() {
        return monastery;
    }

    public Cloister setMonastery(boolean monastery) {
        return new Cloister(game, places, shrine, monastery, yagaHut);
    }

    public boolean isYagaHut() {
        return yagaHut;
    }

    public Cloister setYagaHut(boolean yagaHut) {
        return new Cloister(game, places, shrine, monastery, yagaHut);
    }

    @Override
    public boolean isOpen() {
        Position p = places.get().getPosition();
        return game.getBoard().getAdjacentAndDiagonalTiles(p).size() < 8;
    }

    @Override
    public CompletableScoreContext getScoreContext() {
        return new CloisterScoreContext(getGame());
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.CLOISTER;
    }

    public static String name() {
        return _("Cloister");
    }

}
