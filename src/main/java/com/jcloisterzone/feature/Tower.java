package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;


public class Tower extends TileFeature {

    private final int height;

    public Tower(Game game, List<FeaturePointer> places) {
        this(game, places, 0);
    }

    public Tower(Game game, List<FeaturePointer> places, int height) {
        super(game, places);
        this.height = height;
    }

    @Override
    public Tower placeOnBoard(Position pos, Rotation rot) {
        return new Tower(game, placeOnBoardPlaces(pos, rot), height);
    }

    public Tower increaseHeight() {
        return new Tower(game, places, height + 1);
    }

    public int getHeight() {
        return height;
    }

    public static String name() {
        return _("Tower");
    }
}
