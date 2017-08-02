package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.List;


public class Cloister extends ScoreableFeature implements Completable {

    private static final long serialVersionUID = 1L;

    protected final boolean shrine;
    protected final boolean monastery;
    protected final boolean yagaHut;

    public Cloister(List<FeaturePointer> places) {
        this(places, false, false, false);
    }

    public Cloister(List<FeaturePointer> places, boolean shrine, boolean monastery, boolean yagaHut) {
        super(places);
        this.shrine = shrine;
        this.monastery = monastery;
        this.yagaHut = yagaHut;
    }

    @Override
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new Cloister(
            placeOnBoardPlaces(pos, rot),
            shrine, monastery, yagaHut
        );
    }

    public boolean isShrine() {
        return shrine;
    }

    public Cloister setShrine(boolean shrine) {
        if (this.shrine == shrine) return this;
        return new Cloister(places, shrine, monastery, yagaHut);
    }

    public boolean isMonastery() {
        return monastery;
    }

    public Cloister setMonastery(boolean monastery) {
        if (this.monastery == monastery) return this;
        return new Cloister(places, shrine, monastery, yagaHut);
    }

    public boolean isYagaHut() {
        return yagaHut;
    }

    public Cloister setYagaHut(boolean yagaHut) {
        if (this.yagaHut == yagaHut) return this;
        return new Cloister(places, shrine, monastery, yagaHut);
    }

    @Override
    public boolean isOpen(GameState state) {
        Position p = places.get().getPosition();
        return state.getBoard().getAdjacentAndDiagonalTiles(p).size() < 8;
    }

    @Override
    public int getPoints(GameState state, Player player) {
        Position p = places.get().getPosition();
        return state.getBoard().getAdjacentAndDiagonalTiles(p).size() + 1;
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.CLOISTER;
    }

    public static String name() {
        return _("Cloister");
    }

}
