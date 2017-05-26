package com.jcloisterzone.board;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.feature.Feature;

import io.vavr.collection.HashMap;

public class TileDefinition {

    public static final String ABBEY_TILE_ID = "AM.A";

    private final Expansion origin;
    private final String id;
    private final EdgePattern edgePattern;
    private final TileSymmetry symmetry;

    private final HashMap<Location, Feature> features;

    //expansions data - maybe some map instead ? but still it is only few tiles
    private final TileTrigger trigger;
    private final Location river;
    private final Location flier;
    private final Location windRose;
    private final Class<? extends Feature> cornCircle;


    public TileDefinition(Expansion origin, String id,
        HashMap<Location, Feature> features,
        TileTrigger trigger, Location river, Location flier, Location windRose,
        Class<? extends Feature> cornCircle) {
        this.origin = origin;
        this.id = id;
        this.features = features;

        this.trigger = trigger;
        this.river = river;
        this.flier = flier;
        this.windRose = windRose;
        this.cornCircle = cornCircle;

     // IMMUTABLE TODO
        this.edgePattern = null;
        this.symmetry = null;
    }


    @Override
    public int hashCode() {
        return id.hashCode();
    }


    public boolean isAbbeyTile() {
        return id.equals(ABBEY_TILE_ID);
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


    public HashMap<Location, Feature> getFeatures() {
        return features;
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


}
