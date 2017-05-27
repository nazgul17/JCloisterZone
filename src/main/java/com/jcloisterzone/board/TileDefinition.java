package com.jcloisterzone.board;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;

import io.vavr.Tuple2;
import io.vavr.collection.Map;

public class TileDefinition {

    public static final String ABBEY_TILE_ID = "AM.A";

    private final Expansion origin;
    private final String id;
    private final EdgePattern edgePattern;
    private final TileSymmetry symmetry;

    private final Map<Location, Feature> initialFeatures;

    //expansions data - maybe some map instead ? but still it is only few tiles
    private final TileTrigger trigger;
    private final Location river;
    private final Location flier;
    private final Location windRose;
    private final Class<? extends Feature> cornCircle;


    public TileDefinition(Expansion origin, String id,
        Map<Location, Feature> initialFeatures,
        TileTrigger trigger, Location river, Location flier, Location windRose,
        Class<? extends Feature> cornCircle) {
        this.origin = origin;
        this.id = id;
        this.initialFeatures = initialFeatures;

        this.trigger = trigger;
        this.river = river;
        this.flier = flier;
        this.windRose = windRose;
        this.cornCircle = cornCircle;

        this.edgePattern = computeEdgePattern();
        this.symmetry = this.edgePattern.getSymmetry();
    }

    public boolean isAbbeyTile() {
        return id.equals(ABBEY_TILE_ID);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public Expansion getOrigin() {
        return origin;
    }

    public String getId() {
        return id;
    }


    public EdgePattern getEdgePattern() {
        return edgePattern;
    }


    public TileSymmetry getSymmetry() {
        return symmetry;
    }


    public Map<Location, Feature> getInitialFeatures() {
        return initialFeatures;
    }


    public TileTrigger getTrigger() {
        return trigger;
    }


    public Location getRiver() {
        return river;
    }


    public Location getFlier() {
        return flier;
    }


    public Location getWindRose() {
        return windRose;
    }


    public Class<? extends Feature> getCornCircle() {
        return cornCircle;
    }

    private Edge computeSideEdge(Location loc) {
        if (river != null && loc.isPartOf(river)) {
            return Edge.RIVER;
        }

        Tuple2<Location, Feature> tuple = initialFeatures.find(item -> loc.isPartOf(item._1)).getOrNull();

        if (tuple == null) return Edge.FARM;
        if (tuple._2 instanceof Road) return Edge.ROAD;
        if (tuple._2 instanceof City) return Edge.CITY;

        throw new IllegalArgumentException();
    }

    private EdgePattern computeEdgePattern() {
        return new EdgePattern(
            computeSideEdge(Location.N),
            computeSideEdge(Location.E),
            computeSideEdge(Location.S),
            computeSideEdge(Location.W)
        );
    }
}
