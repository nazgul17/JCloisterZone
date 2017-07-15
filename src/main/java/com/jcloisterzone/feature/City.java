package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.GameState;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;

public class City extends CompletableFeature<City> {

    private final int pennants;
    private final Map<TradeResource, Integer> tradeResources;
    private final boolean besieged, cathedral, princess, castleBase;

    public City(List<FeaturePointer> places, List<Edge> openEdges, int pennants) {
        this(places, openEdges, pennants, HashMap.empty(), false, false, false, false);
    }

    public City(List<FeaturePointer> places, List<Edge> openEdges, int pennants,
            Map<TradeResource, Integer> tradeResources, boolean besieged, boolean cathedral, boolean princess,
            boolean castleBase) {
        super(places, openEdges);
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
    public Feature placeOnBoard(Position pos, Rotation rot) {
        return new City(
            placeOnBoardPlaces(pos, rot),
            placeOnBoardEdges(pos, rot),
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
        return new City(places, openEdges, pennants, tradeResources, besieged, cathedral, princess, castleBase);
    }

    public boolean isCathedral() {
        return cathedral;
    }

    public City setCathedral(boolean cathedral) {
        if (this.cathedral == cathedral) return this;
        return new City(places, openEdges, pennants, tradeResources, besieged, cathedral, princess, castleBase);
    }

    public boolean isPrincess() {
        return princess;
    }

    public City setPrincess(boolean princess) {
        if (this.princess == princess) return this;
        return new City(places, openEdges, pennants, tradeResources, besieged, cathedral, princess, castleBase);
    }

    public boolean isCastleBase() {
        return castleBase;
    }

    public City setCastleBase(boolean castleBase) {
        if (this.castleBase == castleBase) return this;
        return new City(places, openEdges, pennants, tradeResources, besieged, cathedral, princess, castleBase);
    }

    public int getPennants() {
        return pennants;
    }

    public Map<TradeResource, Integer> getTradeResources() {
        return tradeResources;
    }

    public City setTradeResources(Map<TradeResource, Integer> tradeResourced) {
        return new City(places, openEdges, pennants, tradeResources, besieged, cathedral, princess, castleBase);
    }

    @Override
    public int getPoints(GameState state, PlayerAttributes player) {
        boolean completed = isCompleted(state);
        int size = getPlaces().size();

        int pointsPerUnit = 2;
        if (besieged) pointsPerUnit--;
        if (completed) {
            if (cathedral) pointsPerUnit++;
        } else {
            if (cathedral) {
                pointsPerUnit = 0;
            } else {
                pointsPerUnit--;
            }
        }
        return getMageAndWitchPoints(state, pointsPerUnit * (size + pennants)) + getLittleBuildingPoints(state);
    }

    @Override
    public PointCategory getPointCategory() {
        return PointCategory.CITY;
    }

    public static String name() {
        return _("City");
    }
}
