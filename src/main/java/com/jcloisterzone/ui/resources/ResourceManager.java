package com.jcloisterzone.ui.resources;

import java.awt.Image;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.ImmutablePoint;

import io.vavr.collection.Map;
import io.vavr.collection.Set;

public interface ResourceManager {

    static final int NORMALIZED_SIZE = 1000;

    TileImage getTileImage(TileDefinition tile, Rotation rot); //use custom rotation
    TileImage getAbbeyImage(Rotation rot);

    //generic image, path is without extension
    Image getImage(String path);
    Image getLayeredImage(LayeredImageDescriptor lid);

    FeatureArea getFeatureArea(TileDefinition tile, Rotation rot, Location loc);
    FeatureArea getBarnArea();
    FeatureArea getBridgeArea(Location bridgeLocation);
//
//
//    Map<Location, FeatureArea> getFeatureAreas(TileDefinition tile, Rotation rotation, int width, int height, Set<Location> locations);
//    Map<Location, FeatureArea> getBarnTileAreas(Set<Location> corners);
//    FeatureArea getBridgeArea(Location bridgeLocation);

    //default Map<Location, FeatureArea> getBridgeArea(Set<Location> brodgeLoca);...
//
//    //TODO migrate to following
//
//    default Map<Location, FeatureArea> getFeatureAreas(TileDefinition tile, Rotation rotation, Set<Location> locations) {
//        return getFeatureAreas(tile, rotation, NORMALIZED_SIZE, NORMALIZED_SIZE, locations);
//    }

    //TODO change to 1000x1000
    /** returns meeple offset on tile, normalized to 100x100 tile size */
    //TODO why there is meeple type? just because of Barn, nice to have to split it into 2 methods
    ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Location loc);
}
