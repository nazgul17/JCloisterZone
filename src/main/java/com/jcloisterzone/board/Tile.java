package com.jcloisterzone.board;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.MultiTileFeature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.feature.visitor.IsOccupied;
import com.jcloisterzone.feature.visitor.IsOccupiedAndUncompleted;
import com.jcloisterzone.feature.visitor.IsOccupiedOrCompleted;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Game;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;

public class Tile /*implements Cloneable*/ {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected final Game game;
    private final Position position;


    public Tile(Game game, Position position) {
        this.game = game;
        this.position = position;
    }

    private Tuple2<TileDefinition, Rotation> getPlacedTile() {
        return game.getState().getPlacedTiles().get(position).getOrNull();
    }

    public TileDefinition getTileDefinition() {
        return getPlacedTile()._1;
    }

    public Rotation getRotation() {
        return getPlacedTile()._2;
    }

    public EdgePattern getEdgePattern() {
        Tuple2<TileDefinition, Rotation> pt = getPlacedTile();
        return pt._1.getEdgePattern().rotate(pt._2);
    }

    public EdgeType getEdgeType(Location side) {
        return getEdgePattern().at(side, getRotation());
    }

    public String getId() {
        return getTileDefinition().getId();
    }

    public Expansion getOrigin() {
        return getTileDefinition().getOrigin();
    }

//    protected boolean check(Tile tile, Location rel, Board board) {
//        return getEdge(rel) == tile.getEdge(rel.rev());
//    }
//
//    public List<Feature> getFeatures() {
//        return features;
//    }

    public Feature getFeature(Location loc) {
        if (loc == Location.ABBOT) loc = Location.CLOISTER;

        return game.getState().getFeatures()
            .get(new FeaturePointer(position, loc))
            .getOrNull();

    }

    public Feature getFeaturePartOf(Location loc) {
        Location normLoc = loc == Location.ABBOT ?
                Location.CLOISTER : loc.rotateCCW(getRotation());

        Tuple2<Location, Feature> initial = getTileDefinition()
            .getInitialFeatures()
            .find(t -> normLoc.isPartOf(t._1))
            .getOrNull();

        if (initial == null) return null;

        return getFeature(initial._1);
    }

    public Stream<Tuple2<Location, Feature>> getFeatures() {
        Rotation rot = getRotation();
        Map<FeaturePointer, Feature> allFeatures = game.getState().getFeatures();
        return Stream.ofAll(getTileDefinition().getInitialFeatures())
            .map(t -> t.update1(t._1.rotateCCW(rot)))
            .map(t -> t.update2(
                allFeatures.get(new FeaturePointer(position, t._1)).get()
            ));
    }

//    /** merge this to another tile - method argument is tile placed before */
//    protected void merge(Tile tile, Location loc) {
//        //if (logger.isDebugEnabled()) logger.debug("Merging " + id + " with " + tile.getId());
//        Location oppositeLoc = loc.rev();
//        MultiTileFeature oppositePiece = (MultiTileFeature) tile.getFeaturePartOf(oppositeLoc);
//        if (oppositePiece != null) {
//            if (tileDefinition.isAbbeyTile()) {
//                oppositePiece.setAbbeyEdge(oppositeLoc);
//            } else {
//                MultiTileFeature thisPiece = (MultiTileFeature) getFeaturePartOf(loc);
//                oppositePiece.setEdge(oppositeLoc, thisPiece);
//                thisPiece.setEdge(loc, oppositePiece);
//            }
//        }
//        for (int i = 0; i < 2; i++) {
//            Location halfSide = i == 0 ? loc.getLeftFarm() : loc.getRightFarm();
//            Location oppositeHalfSide = halfSide.rev();
//            oppositePiece = (MultiTileFeature) tile.getFeaturePartOf(oppositeHalfSide);
//            if (oppositePiece != null) {
//                if (tileDefinition.isAbbeyTile()) {
//                    oppositePiece.setAbbeyEdge(oppositeHalfSide);
//                } else {
//                    MultiTileFeature thisPiece = (MultiTileFeature) getFeaturePartOf(halfSide);
//                    oppositePiece.setEdge(oppositeHalfSide, thisPiece);
//                    thisPiece.setEdge(halfSide, oppositePiece);
//                }
//            }
//        }
//    }

//    protected void unmerge(Tile tile, Location loc) {
//        Location oppositeLoc = loc.rev();
//        MultiTileFeature oppositePiece = (MultiTileFeature) tile.getFeaturePartOf(oppositeLoc);
//        if (oppositePiece != null) {
//            oppositePiece.setEdge(oppositeLoc, null);
//            if (!tileDefinition.isAbbeyTile()) {
//                MultiTileFeature thisPiece = (MultiTileFeature) getFeaturePartOf(loc);
//                if (thisPiece != null) { //can be null for bridge undo
//                    thisPiece.setEdge(loc, null);
//                }
//            }
//        }
//        for (int i = 0; i < 2; i++) {
//            Location halfSide = i == 0 ? loc.getLeftFarm() : loc.getRightFarm();
//            Location oppositeHalfSide = halfSide.rev();
//            oppositePiece = (MultiTileFeature) tile.getFeaturePartOf(oppositeHalfSide);
//            if (oppositePiece != null) {
//                oppositePiece.setEdge(oppositeHalfSide, null);
//                if (!tileDefinition.isAbbeyTile()) {
//                    MultiTileFeature thisPiece = (MultiTileFeature) getFeaturePartOf(halfSide);
//                    thisPiece.setEdge(halfSide, null);
//                }
//            }
//        }
//    }

//    public void setRotation(Rotation rotation) {
//        assert rotation != null;
//        this.rotation =  rotation;
//    }

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

    public Game getGame() {
        return game;
    }

    public Position getPosition() {
        return position;
    }

//    public Bridge getBridge() {
//        return bridge;
//    }
//
//    public void placeBridge(Location bridgeLoc) {
//        assert bridge == null && bridgeLoc != null; //TODO AI support - remove bridge from tile
//        Location normalizedLoc = bridgeLoc.rotateCCW(rotation);
//        bridge = new Bridge();
//        bridge.setId(game.idSequnceNextVal());
//        bridge.setTile(this);
//        bridge.setLocation(normalizedLoc);
//        features.add(bridge);
//        edgePattern = edgePattern.getBridgePattern(normalizedLoc);
//    }
//
//    public void removeBridge(Location bridgeLoc) {
//        Location normalizedLoc = bridgeLoc.rotateCCW(rotation);
//        features.remove(bridge);
//        bridge = null;
//        edgePattern = edgePattern.removeBridgePattern(normalizedLoc);
//    }

    public Stream<Tuple2<Location, Scoreable>> getUnoccupiedScoreables(boolean excludeCompleted) {

        return getFeatures()
            .filter(t -> t._2 instanceof Scoreable)
            .map(t -> t.map2(f -> (Scoreable) f))
            .filter(t -> t._2.getMeeples().isEmpty());



//        Set<Location> locations = new HashSet<>();
//        for (Feature f : features) {
//            if (f instanceof Scoreable) {
//                if (f instanceof Cloister) {
//                    Cloister c = (Cloister) f;
//                    if (c.isMonastery() && c.getMeeples().isEmpty()) {
//                        locations.add(Location.ABBOT);
//                    }
//                }
//                IsOccupied visitor;
//                if (excludeCompleted && f instanceof Completable) {
//                    visitor = new IsOccupiedOrCompleted();
//                } else {
//                    visitor = new IsOccupied();
//                }
//                if (f.walk(visitor)) continue;
//                locations.add(f.getLocation());
//            }
//        }
//        return locations;
    }

    public Set<Location> getPlayerFeatures(Player player, Class<? extends Feature> featureClass) {
        return getPlayerFeatures(player, featureClass, false);
    }

    public Set<Location> getPlayerUncompletedFeatures(Player player, Class<? extends Feature> featureClass) {
        return getPlayerFeatures(player, featureClass, true);
    }


    private Set<Location> getPlayerFeatures(Player player, Class<? extends Feature> featureClass, boolean uncompletedOnly)  {
        Set<Location> locations = new HashSet<>();
        for (Feature f : features) {
            if (!featureClass.isInstance(f)) continue;
            IsOccupied visitor = uncompletedOnly ? new IsOccupiedAndUncompleted() : new IsOccupied();
            if (f.walk(visitor.with(player).with(Follower.class))) {
                locations.add(f.getLocation());
            }
        }
        return locations;
    }

    @Override
    public String toString() {
        return getId() + '(' + getRotation() + ')';
    }

    public TileTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(TileTrigger trigger) {
        this.trigger = trigger;
    }

    public boolean hasTrigger(TileTrigger trigger) {
        return trigger == this.trigger;
    }

    public Class<? extends Feature> getCornCircle() {
        return cornCircle;
    }

    public void setCornCircle(Class<? extends Feature> cornCircle) {
        this.cornCircle = cornCircle;
    }

    public City getCityWithPrincess() {
        for (Feature p : features) {
            if (p instanceof City ) {
                City cp = (City) p;
                if (cp.isPricenss()) {
                    return cp;
                }
            }
        }
        return null;
    }


    public Location getRiver() {
        return river;
    }

    public void setRiver(Location river) {
        this.river = river;
    }


    public Location getFlier() {
        return flier;
    }

    public void setFlier(Location flier) {
        this.flier = flier;
    }

    public Location getWindRose() {
        return windRose;
    }

    public void setWindRose(Location windRose) {
        this.windRose = windRose;
    }

    public boolean isBridgeAllowed(Location bridgeLoc) {
        if (origin == Expansion.COUNT || getBridge() != null) return false;
        return edgePattern.isBridgeAllowed(bridgeLoc, rotation);
    }

}
