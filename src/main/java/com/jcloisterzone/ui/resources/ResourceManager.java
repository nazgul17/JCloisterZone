package com.jcloisterzone.ui.resources;

import java.awt.Image;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.ui.ImmutablePoint;

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

    //TODO change to 1000x1000
    /** returns meeple offset on tile, normalized to 100x100 tile size */
    ImmutablePoint getMeeplePlacement(TileDefinition tile, Rotation rot, Location loc);
    ImmutablePoint getBarnPlacement();
}
