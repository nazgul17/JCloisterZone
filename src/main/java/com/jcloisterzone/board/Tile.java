package com.jcloisterzone.board;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;

public class Tile {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected final GameState state;
    private final Position position;

    private final PlacedTile placedTile;


    public Tile(GameState state, Position position) {
        this(state, position, state.getPlacedTiles().get(position).getOrNull());
    }

    public Tile(GameState state, Position position, PlacedTile placedTile) {
        this.state = state;
        this.position = position;
        this.placedTile = placedTile;
    }

    private PlacedTile getPlacedTile() {
        return placedTile;
    }

    public TileDefinition getTileDefinition() {
        return placedTile.getTile();
    }

    public Rotation getRotation() {
        return placedTile.getRotation();
    }

    public EdgePattern getEdgePattern() {
        return placedTile.getEdgePattern();
    }

    public String getId() {
        return getTileDefinition().getId();
    }

    public Expansion getOrigin() {
        return getTileDefinition().getOrigin();
    }

    public Feature getFeature(Location loc) {
        if (loc == Location.ABBOT) loc = Location.CLOISTER;

        return state.getFeatureMap()
            .get(new FeaturePointer(position, loc))
            .getOrNull();

    }

    public Feature getFeaturePartOf(Location loc) {
        if (loc == Location.ABBOT) loc = Location.CLOISTER;

        FeaturePointer fp = new FeaturePointer(position, loc);
        state.getFeatureMap().find(t -> fp.isPartOf(t._1)).map(Tuple2::_2);
        return state.getFeatureMap()
            .find(t -> fp.isPartOf(t._1))
            .map(Tuple2::_2)
            .getOrNull();
    }

    public Stream<Tuple2<Location, Feature>> getFeatures() {
        Rotation rot = getRotation();
        Map<FeaturePointer, Feature> allFeatures = state.getFeatureMap();
        return Stream.ofAll(getTileDefinition().getInitialFeatures())
            .map(t -> t.update1(t._1.rotateCW(rot)))
            .map(t -> t.update2(
                allFeatures.get(new FeaturePointer(position, t._1)).get()
            ));
    }

    public Stream<Tuple2<Location, Completable>> getCompletableFeatures() {
        return getFeatures()
            .filter(t -> t._2 instanceof Completable)
            .map(t -> t.map2(f -> (Completable) f));
    }

    public boolean isAbbeyTile() {
        return getTileDefinition().isAbbeyTile();
    }

    public TileSymmetry getSymmetry() {
        return getTileDefinition().getSymmetry();
    }

    public boolean hasCloister() {
        return getFeature(Location.CLOISTER) != null;
    }

    public Cloister getCloister() {
        return (Cloister) getFeature(Location.CLOISTER);
    }

    public Tower getTower() {
        return (Tower) getFeature(Location.TOWER);
    }

    public Position getPosition() {
        return position;
    }


    private boolean isValueNotCompleted(Tuple2<Location, ? extends Feature> t) {
        if (t._2 instanceof Completable) {
            return !((Completable)t._2).isCompleted(state);
        } else {
            return true;
        }
    }


    public Stream<Tuple2<Location, Feature>> getPlayerFeatures(Player player, Class<? extends Feature> featureClass) {
        return getFeatures()
            .filter(t -> featureClass == null || featureClass.isInstance(t._2))
            .filter(t -> t._2.isOccupiedBy(state, player));
    }

    public Stream<Tuple2<Location, Feature>> getPlayerUncompletedFeatures(Player player, Class<? extends Feature> featureClass) {
        return getPlayerFeatures(player, featureClass).filter(this::isValueNotCompleted);
    }

    @Override
    public String toString() {
        return getId() + '(' + getRotation() + ')';
    }

    public TileTrigger getTrigger() {
        return getTileDefinition().getTrigger();
    }


    public boolean hasTrigger(TileTrigger trigger) {
        return getTrigger() == trigger;
    }

    public Class<? extends Feature> getCornCircle() {
        return getTileDefinition().getCornCircle();
    }

//    public City getCityWithPrincess() {
//        for (Feature p : features) {
//            if (p instanceof City ) {
//                City cp = (City) p;
//                if (cp.isPricenss()) {
//                    return cp;
//                }
//            }
//        }
//        return null;
//    }


    public Location getRiver() {
       Location loc =  getTileDefinition().getRiver();
       return loc == null ? null : loc.rotateCCW(getRotation());
    }

    public Location getFlier() {
        Location loc =  getTileDefinition().getFlier();
        return loc == null ? null : loc.rotateCCW(getRotation());
    }

    public Location getWindRose() {
        Location loc =  getTileDefinition().getWindRose();
        return loc == null ? null : loc.rotateCCW(getRotation());
    }

//    public boolean isBridgeAllowed(Location bridgeLoc) {
//        if (origin == Expansion.COUNT || getBridge() != null) return false;
//        return edgePattern.isBridgeAllowed(bridgeLoc, rotation);
//    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Tile)) return false;
        Tile tile = (Tile) obj;
        return Objects.equals(position, tile.position);
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }

}
