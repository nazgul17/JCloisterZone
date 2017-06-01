package com.jcloisterzone.feature;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.game.Game;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;

import static com.jcloisterzone.ui.I18nUtils._;

public class City extends CompletableFeature<City> {

    private final int pennants;
    private final Map<TradeResource, Integer> tradeResources;
    private final boolean besieged, cathedral, princess, castleBase;

    public City(Game game, List<FeaturePointer> places, List<Edge> openEdges, int pennants) {
        this(game, places, openEdges, pennants, HashMap.empty(), false, false, false, false);
    }

    public City(Game game, List<FeaturePointer> places, List<Edge> openEdges, int pennants,
            Map<TradeResource, Integer> tradeResources, boolean besieged, boolean cathedral, boolean princess,
            boolean castleBase) {
        super(game, places, openEdges);
        this.pennants = pennants;
        this.tradeResources = tradeResources;
        this.besieged = besieged;
        this.cathedral = cathedral;
        this.princess = princess;
        this.castleBase = castleBase;
    }

    @Override
    public City merge(City city) {
        return new City(
            game,
            mergePlaces(city),
            mergeEdges(city),
            pennants + city.pennants,
            mergeTradeResources(city),
            besieged || city.besieged,
            cathedral || city.cathedral,
            princess || city.princess,
            castleBase && city.castleBase
        );
    }

    @Override
    public Feature placeOnBoard(Position pos) {
        return new City(
            game,
            placeOnBoardPlaces(pos),
            placeOnBoardEdges(pos),
            pennants, tradeResources, besieged, cathedral, princess, castleBase
        );
    }

    protected Map<TradeResource, Integer> mergeTradeResources(City city) {
        // IMMUTABLE TODO
        return null;
    }

    public boolean isBesieged() {
        return besieged;
    }

    public City setBesieged(boolean besieged) {
        if (this.besieged == besieged) return this;
        return new City(game, places, openEdges, pennants, tradeResources, besieged, cathedral, princess, castleBase);
    }

    public boolean isCathedral() {
        return cathedral;
    }

    public City setCathedral(boolean cathedral) {
        if (this.cathedral == cathedral) return this;
        return new City(game, places, openEdges, pennants, tradeResources, besieged, cathedral, princess, castleBase);
    }

    public boolean isPrincess() {
        return princess;
    }

    public City setPrincess(boolean princess) {
        if (this.princess == princess) return this;
        return new City(game, places, openEdges, pennants, tradeResources, besieged, cathedral, princess, castleBase);
    }

    public boolean isCastleBase() {
        return castleBase;
    }

    public City setCastleBase(boolean castleBase) {
        if (this.castleBase == castleBase) return this;
        return new City(game, places, openEdges, pennants, tradeResources, besieged, cathedral, princess, castleBase);
    }

    public int getPennants() {
        return pennants;
    }

    public Map<TradeResource, Integer> getTradeResources() {
        return tradeResources;
    }

    public City setTradeResources(Map<TradeResource, Integer> tradeResourced) {
        return new City(game, places, openEdges, pennants, tradeResources, besieged, cathedral, princess, castleBase);
    }

    @Override
    public CityScoreContext getScoreContext() {
        return new CityScoreContext(game);
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.CITY;
    }

    public static String name() {
        return _("City");
    }
}
