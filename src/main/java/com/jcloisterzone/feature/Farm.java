package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;

public class Farm extends TileFeature implements MultiTileFeature<Farm> {

    // for unplaced features, references is to (0, 0)
    protected final List<FeaturePointer> adjoiningCities; //or castles
    protected final boolean adjoiningCityOfCarcassonne;
    protected final int pigHerds;


    public Farm(Game game, List<FeaturePointer> places, List<FeaturePointer> adjoiningCities) {
        this(game, places, adjoiningCities, false, 0);
    }

    public Farm(Game game, List<FeaturePointer> places, List<FeaturePointer> adjoiningCities,
            boolean adjoiningCityOfCarcassonne, int pigHerds) {
        super(game, places);
        this.adjoiningCities = adjoiningCities;
        this.adjoiningCityOfCarcassonne = adjoiningCityOfCarcassonne;
        this.pigHerds = pigHerds;
    }

    @Override
    public Farm merge(Farm farm) {
        return new Farm(
            game,
            mergePlaces(farm),
            mergeAdjoiningCities(farm),
            adjoiningCityOfCarcassonne || farm.adjoiningCityOfCarcassonne,
            pigHerds + farm.pigHerds
        );
    }

    @Override
    public Feature placeOnBoard(Position pos) {
        return new Farm(
            game,
            placeOnBoardPlaces(pos),
            placeOnBoardAdjoiningCities(pos),
            adjoiningCityOfCarcassonne,
            pigHerds
        );
    }

    public List<FeaturePointer> getAdjoiningCities() {
        return adjoiningCities;
    }

    public Farm setAdjoiningCities(List<FeaturePointer> adjoiningCities) {
        return new Farm(game, places, adjoiningCities, adjoiningCityOfCarcassonne, pigHerds);
    }

    public int getPigHerds() {
        return pigHerds;
    }

    public Farm setPigHerds(int pigHerds) {
        return new Farm(game, places, adjoiningCities, adjoiningCityOfCarcassonne, pigHerds);
    }

    public boolean isAdjoiningCityOfCarcassonne() {
        return adjoiningCityOfCarcassonne;
    }

    public Farm setAdjoiningCityOfCarcassonne(boolean adjoiningCityOfCarcassonne) {
        return new Farm(game, places, adjoiningCities, adjoiningCityOfCarcassonne, pigHerds);
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.FARM;
    }

    @Override
    public FarmScoreContext getScoreContext() {
        return new FarmScoreContext(getGame());
    }

    public static String name() {
        return _("Farm");
    }

    // immutable helpers

    protected List<FeaturePointer> mergeAdjoiningCities(Farm obj) {
        return this.adjoiningCities.appendAll(obj.adjoiningCities).distinct();
    }

    protected List<FeaturePointer> placeOnBoardAdjoiningCities(Position pos) {
        return this.adjoiningCities.map(fp -> fp.translate(pos));
    }
}
