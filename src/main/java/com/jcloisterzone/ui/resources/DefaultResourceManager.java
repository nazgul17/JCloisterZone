package com.jcloisterzone.ui.resources;

import java.awt.Image;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.ui.ImmutablePoint;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class DefaultResourceManager implements ResourceManager {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final ImageLoader imgLoader;

    public static final Map<Location, ImmutablePoint> COUNT_OFFSETS;

    //TODO use point def instead
    static {
        COUNT_OFFSETS = HashMap.of(
           Location.QUARTER_CASTLE, new ImmutablePoint(40, -40),
           Location.QUARTER_MARKET, new ImmutablePoint(100, 50),
           Location.QUARTER_BLACKSMITH, new ImmutablePoint(60, 130),
           Location.QUARTER_CATHEDRAL, new ImmutablePoint(-80, 5)
        );
    }

    public DefaultResourceManager() {
        ImageLoader imgLoader = null;
        try {
            URL defaults = getClass().getClassLoader().getResource("defaults/").toURI().toURL();
            URLClassLoader loader = new URLClassLoader(new URL[] { defaults });
            imgLoader = new ImageLoader(loader);
        } catch (URISyntaxException | MalformedURLException e) {
            //should never happen
            logger.error(e.getMessage(), e);
        }
        this.imgLoader = imgLoader;
    }

    @Override
    public TileImage getTileImage(TileDefinition tile, Rotation rot) {
        return null;
    }

    @Override
    public TileImage getAbbeyImage(Rotation rot) {
        return null;
        //return (new TileImageFactory()).getAbbeyImage();
    }

    @Override
    public Image getImage(String path) {
        return imgLoader.getImage(path);
    }

    @Override
    public Image getLayeredImage(LayeredImageDescriptor lid) {
        return imgLoader.getLayeredImage(lid);
    }

    private ImmutablePoint getBarnPlacement(Location loc) {
        if (loc.intersect(Location.NL.union(Location.WR)) != null) return new ImmutablePoint(0, 0);
        if (loc.intersect(Location.NR.union(Location.EL)) != null) return new ImmutablePoint(100, 0);
        if (loc.intersect(Location.SL.union(Location.ER)) != null) return new ImmutablePoint(100, 100);
        if (loc.intersect(Location.SR.union(Location.WL)) != null) return new ImmutablePoint(0, 100);
        throw new IllegalArgumentException("Corner location expected");
    }

    @Override
    public ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Location loc) {
        if (type.equals(Barn.class)) {
            return getBarnPlacement(loc);
        }
        return null;
    }

    @Override
    public Map<Location, FeatureArea> getBarnTileAreas(Tile tile, int width, int height, Set<Location> corners) {
        //TODO update method interface
        assert corners.size() == 1;

        int rx = width/2;
        int ry = height/2;
        Area a = new Area(new Ellipse2D.Double(width - rx, height - ry, 2 * rx, 2 * ry));

        return HashMap.of(corners.get(), new FeatureArea(a, FeatureArea.DEFAULT_FARM_ZINDEX));
    }


    @Override
    public Map<Location, FeatureArea> getBridgeAreas(Tile tile, int width, int height, Set<Location> locations) {
        return null;
    }

    @Override
    public Map<Location, FeatureArea> getFeatureAreas(Tile tile, int width, int height, Set<Location> locations) {
        if (tile.getId().equals(CountCapability.QUARTER_ACTION_TILE_ID)) {
            Map<Location, FeatureArea> areas = HashMap.empty();
            double rx = width * 0.6;
            double ry = height * 0.6;
            for (Location loc : locations) {
                ImmutablePoint offset = COUNT_OFFSETS.get(loc).get();
                Area a = new Area(new Ellipse2D.Double(-rx+offset.getX(),-ry+offset.getY(),2*rx,2*ry));
                areas = areas.put(loc, new FeatureArea(a, FeatureArea.DEFAULT_STRUCTURE_ZINDEX));
            }
            return areas;
        }
        return null;
    }
}
