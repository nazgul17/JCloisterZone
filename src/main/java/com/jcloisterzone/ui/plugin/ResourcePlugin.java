package com.jcloisterzone.ui.plugin;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Functions;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.Tower;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.resources.AreaRotationScaling;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.FeatureDescriptor;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.ui.resources.TileImage;
import com.jcloisterzone.ui.resources.svg.ThemeGeometry;

import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;


public class ResourcePlugin extends Plugin implements ResourceManager {

    public static final int NORMALIZED_SIZE = 1000;

    private static ThemeGeometry defaultGeometry;
    private ThemeGeometry pluginGeometry;
    private Insets imageOffset =  new Insets(0, 0, 0, 0);
    private int imageRatioX = 1;
    private int imageRatioY = 1;

    private Set<String> supportedExpansions = HashSet.empty(); //expansion codes

    static {
        try {
            defaultGeometry = new ThemeGeometry(ResourcePlugin.class.getClassLoader(), "defaults/tiles", 1.0);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            LoggerFactory.getLogger(ThemeGeometry.class).error(e.getMessage(), e);
        }
    }

    public ResourcePlugin(URL url, String relativePath) throws Exception {
        super(url, relativePath);
    }

    @Override
    protected void doLoad() throws IOException, SAXException, ParserConfigurationException {
        pluginGeometry = new ThemeGeometry(getLoader(), "tiles", getImageSizeRatio());
    }

    @Override
    protected void parseMetadata(Element rootElement) throws Exception {
        super.parseMetadata(rootElement);
        NodeList nl = rootElement.getElementsByTagName("expansions");
        if (nl.getLength() == 0) throw new Exception("Supported expansions missing in plugin.xml for " + getId());
        Element expansion = (Element) nl.item(0);
        nl = expansion.getElementsByTagName("expansion");
        if (nl.getLength() == 0) throw new Exception("No expansion is supported by " + getId());
        for (int i = 0; i < nl.getLength(); i++) {
            String expName = nl.item(i).getFirstChild().getNodeValue().trim();
            Expansion exp = Expansion.valueOf(expName);
            supportedExpansions = supportedExpansions.add(exp.getCode());
        }

        Element tiles = XMLUtils.getElementByTagName(rootElement, "tiles");
        if (tiles != null) {
            String value = XMLUtils.childValue(tiles, "image-offset");
            if (value != null) {
                String[] tokens = value.split(",");
                if (tokens.length != 4) {
                    throw new Exception("Invalid value for image-offset " + value);
                }
                imageOffset = new Insets(
                   Integer.parseInt(tokens[0]),
                   Integer.parseInt(tokens[1]),
                   Integer.parseInt(tokens[2]),
                   Integer.parseInt(tokens[3])
                );
            }
            value = XMLUtils.childValue(tiles, "image-ratio-x");
            if (value != null) {
                imageRatioX = Integer.parseInt(value);
                if (imageRatioX == 0)
                    imageRatioX = 1;
            }
            value = XMLUtils.childValue(tiles, "image-ratio-y");
            if (value != null) {
                imageRatioY = Integer.parseInt(value);
                if (imageRatioY == 0) imageRatioY = 1;
            }
        }
    }


    protected boolean containsTile(String tileId) {
        if (!isEnabled()) return false;
        String expCode = tileId.substring(0, 2);
        return supportedExpansions.contains(expCode);
    }

    public boolean isExpansionSupported(Expansion exp) {
        return supportedExpansions.contains(exp.getCode());
    }

    public double getImageSizeRatio() {
        return imageRatioY/(double)imageRatioX;
    }

    @Override
    public TileImage getTileImage(TileDefinition tile, Rotation rot) {
        return getTileImage(tile.getId(), rot);
    }

    @Override
    public TileImage getAbbeyImage(Rotation rot) {
        return getTileImage(TileDefinition.ABBEY_TILE_ID, rot);
    }

    private TileImage getTileImage(String tileId, Rotation rot) {
        if (!containsTile(tileId)) return null;
        String baseName = "tiles/"+tileId.substring(0, 2) + "/" + tileId.substring(3);
        String fileName;
        Image img;
        // first try to find rotation specific image
        fileName = baseName + "@" + rot.ordinal();
        img =  getImageLoader().getImage(fileName);
        if (img != null) {
            return new TileImage(img, imageOffset);
        }
        // if not found, load generic one and rotate manually
        fileName = baseName;
        img =  getImageLoader().getImage(fileName);
        if (img == null) return null;
        if (rot == Rotation.R0) {
            return new TileImage(img, imageOffset);
        }
        BufferedImage buf;
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (rot == Rotation.R180) {
            buf = UiUtils.newTransparentImage(w, h);
        } else {
            buf = UiUtils.newTransparentImage(h, w);
        }
        Graphics2D g = (Graphics2D) buf.getGraphics();
        g.drawImage(img, rot.getAffineTransform(w, h), null);
        return new TileImage(buf, imageOffset);
    }

    @Override
    public Image getImage(String path) {
        return getImageLoader().getImage(path);
    }

    @Override
    public Image getLayeredImage(LayeredImageDescriptor lid) {
        return getImageLoader().getLayeredImage(lid);
    }

    @Override
    public ImmutablePoint getMeeplePlacement(Tile tile, Class<? extends Meeple> type, Location loc) {
        if (!containsTile(tile.getId())) return null;
        if (type.equals(Barn.class)) return null;
        Feature feature = tile.getFeature(loc);

        Location normLoc = loc.rotateCCW(tile.getRotation());
        ImmutablePoint point = pluginGeometry.getMeeplePlacement(tile.getTileDefinition(), feature.getClass(), normLoc);
        if (point == null) {
            point = defaultGeometry.getMeeplePlacement(tile.getTileDefinition(), feature.getClass(), normLoc);
        }
        if (point == null) {
            logger.warn("No point defined for <" + (new FeatureDescriptor(tile.getId(), feature.getClass(), loc)) + ">");
            point = new ImmutablePoint(0, 0);
        }
        return point.rotate100(tile.getRotation());
    }

    private FeatureArea applyRotationScaling(Tile tile, ThemeGeometry geom, FeatureArea area) {
        if (area == null) return null;
        /* rectangular tiles can have noScale direction to keep one dimension unchanged by rotation */
        AreaRotationScaling ars = area.getRotationScaling();
        if (ars != AreaRotationScaling.NORMAL)  {
            Rotation rot = tile.getRotation();
            if (rot == Rotation.R90 || rot == Rotation.R270) {
                AffineTransform t = new AffineTransform();
                if (ars == AreaRotationScaling.NO_SCALE_HEIGHT) {
                    ars.concatAffineTransform(t, geom.getImageSizeRatio());
                } else {
                    ars.concatAffineTransform(t, 1.0 / geom.getImageSizeRatio());
                }
                area = area.transform(t);
            }
        }
        return area;
    }

    private FeatureArea getFeatureArea(TileDefinition tile, Class<? extends Feature> featureClass, Location loc) {
        if (loc == Location.ABBOT) loc = Location.CLOISTER;
        if (Castle.class.equals(featureClass)) {
            featureClass = City.class;
        }
        ThemeGeometry source = null;
        FeatureArea area = pluginGeometry.getArea(tile, featureClass, loc);
        if (area == null) {
            area = adaptDefaultGeometry(defaultGeometry.getArea(tile, featureClass, loc));
            if (area == null) {
                logger.error("No shape defined for <" + (new FeatureDescriptor(tile.getId(), featureClass, loc)) + ">");
                return new FeatureArea(new Area(), 0);
            } else {
                source = defaultGeometry;
            }
        } else {
            source = pluginGeometry;
        }

//        area = applyRotationScaling(tile, source, area);
//        AffineTransform t = new AffineTransform();
//        t.concatenate(tile.getRotation().getAffineTransform(NORMALIZED_SIZE, (int) (NORMALIZED_SIZE * getImageSizeRatio())));
//        area = area.transform(t);
        return area;
    }

    private Area getSubtractionArea(TileDefinition tile, boolean farm) {
        Area d = defaultGeometry.getSubtractionArea(tile, farm),
             p = pluginGeometry.getSubtractionArea(tile, farm),
             area = new Area();

        if (d != null) {
            area.add(adaptDefaultGeometry(d));
        }
        if (p != null) {
            //HACK always area rotation scale as not scale in both width and height
            //it's what is required for ROAD subtraction but it's possible in future it will be needed scale area too.
//            Rotation rot = tile.getRotation();
//            if (rot == Rotation.R90 || rot == Rotation.R270) {
//                AffineTransform t = new AffineTransform();
//                AreaRotationScaling.NO_SCALE_HEIGHT.concatAffineTransform(t, getImageSizeRatio());
//                AreaRotationScaling.NO_SCALE_WIDTH.concatAffineTransform(t, 1.0 / getImageSizeRatio());
//                p = p.createTransformedArea(t);
//            }

            area.add(p);
        }

//        AffineTransform t = new AffineTransform();
//        t.concatenate(tile.getRotation().getAffineTransform(NORMALIZED_SIZE, (int) (NORMALIZED_SIZE * getImageSizeRatio())));
//        area.transform(t);
        return area;
    }

    private boolean isFarmComplement(TileDefinition tile, Location loc) {
        if (pluginGeometry.isFarmComplement(tile, loc)) return true;
        if (defaultGeometry.isFarmComplement(tile, loc)) return true;
        return false;
    }

    private FeatureArea adaptDefaultGeometry(FeatureArea fa) {
        if (fa == null) return null;
        return fa.transform(AffineTransform.getScaleInstance(1.0, getImageSizeRatio()));
    }

    private Area adaptDefaultGeometry(Area a) {
        if (a == null) return null;
        if (imageRatioX != imageRatioY) {
            return a.createTransformedArea(
                AffineTransform.getScaleInstance(1.0, getImageSizeRatio())
            );
        }
        return a;
    }

    @Override
    public Map<Location, FeatureArea> getFeatureAreas(Tile tile, int width, int height, Set<Location> locations) {
        if (!containsTile(tile.getId())) return null;
        // dirty hack to not handle quarter locations
        if (tile.getId().equals(CountCapability.QUARTER_ACTION_TILE_ID)) return null;

        TileDefinition tileDef = tile.getTileDefinition();
        Map<Location, Feature> features = tileDef.getInitialFeatures();
        Rotation rot = tile.getRotation();
        Set<Location> initialLocations = locations.map(loc -> loc.rotateCCW(rot));

        Location complementFarm = features
            .find(t -> t._2 instanceof Farm && isFarmComplement(tileDef, t._1))
            .map(Tuple2::_1).getOrNull();
        Location bridgeLoc = features
            .find(t -> t._2 instanceof Bridge)
            .map(Tuple2::_1).getOrNull();

        // get base areas for all features
        Map<Location, FeatureArea> baseAreas = features
            .filter(t -> t._1 != complementFarm)
            .map((loc, feature) -> new Tuple2<>(loc, getFeatureArea(tileDef, feature.getClass(), loc)));

        // complement farm area is remaining uncovered area
        if (complementFarm != null) {
            Area union = baseAreas.foldLeft(new Area(), (area, t) -> {
                area.add(t._2.getTrackingArea()); // Area is mutable, returning same ref
                return area;
            });
            Area farmArea = new Area(getFullRectangle());
            farmArea.subtract(union);
            baseAreas = baseAreas.put(complementFarm, new FeatureArea(farmArea, FeatureArea.DEFAULT_FARM_ZINDEX));
        }

        // farms are defined in shapes.xml as bounding regions, other non-farm
        // features must be subtract for get clear shape
        Area nonFarmUnion = baseAreas.foldLeft(new Area(), (area, t) -> {
            if (!t._1.isFarmLocation()) {
                area.add(t._2.getTrackingArea()); // Area is mutable, returning same ref
            }
            return area;
        });
        baseAreas = baseAreas.toMap(t -> {
            if (!t._1.isFarmLocation()) {
                return t;
            }
            return t.map2(fa -> fa.subtract(nonFarmUnion));
        });

        // subtract "restricted" areas
        Area onlyFarmSubtraction = getSubtractionArea(tileDef, true);
        Area allSubtraction = getSubtractionArea(tileDef, false);
        baseAreas = baseAreas.toMap(t -> {
            Location loc = t._1;
            if (loc == bridgeLoc) return t;
            if (loc.isFarmLocation()) t = t.map2(fa -> fa.subtract(onlyFarmSubtraction));
            return t.map2(fa -> fa.subtract(allSubtraction));
        });

        //subtract tower
        FeatureArea towerArea = baseAreas.get(Location.TOWER).getOrNull();
        if (towerArea != null) {
            baseAreas = baseAreas.toMap(t -> {
                Location loc = t._1;
                if (loc == bridgeLoc || loc == Location.TOWER) return t;
                return t.map2(fa -> fa.subtract(towerArea));
            });
        }

        // if flier area is requested, add it to result and subtract it from others
        //TODO don't subtract if contained in locations but subtract when present on tile
//        if (locations.contains(Location.FLIER)) {
//            FeatureArea flierArea = getFeatureArea(tileDef, null, Location.FLIER);
//            baseAreas = baseAreas.mapValues(fa -> fa.subtract(flierArea));
//            baseAreas = baseAreas.put(Location.FLIER, flierArea);
//        }

        // bridge should be above all, make bridge active on intersection with common areas
        // -> subtract bridge from other areas
        if (bridgeLoc != null) {
            FeatureArea bridgeArea = baseAreas.get(bridgeLoc).get();
            baseAreas = baseAreas.toMap(t -> {
                if (t._1 == bridgeLoc || t._1.isFarmLocation()) return t;
                return t.map2(fa -> fa.subtract(bridgeArea));
            });
        }

        // filter result to requested locations
        baseAreas = baseAreas.filterKeys(loc -> initialLocations.contains(loc));


        double ratioX;
        double ratioY;
        if (rot == Rotation.R90 || rot  == Rotation.R270) {
            ratioX = (double) height / NORMALIZED_SIZE / getImageSizeRatio();
            ratioY = (double) width / NORMALIZED_SIZE;
        } else {
            ratioX = (double) width / NORMALIZED_SIZE;
            ratioY = (double) height / NORMALIZED_SIZE / getImageSizeRatio();
        }

        // Apply rotation + resize
        // (concatenate in reverse order)
        AffineTransform tx = AffineTransform.getScaleInstance(ratioX, ratioY);
        tx.concatenate(rot.getAffineTransform(NORMALIZED_SIZE));
        return baseAreas.bimap(
            loc -> loc.rotateCW(rot),
            fa -> fa.transform(tx)
        );
    }

    @Override
    public Map<Location, FeatureArea> getBarnTileAreas(Tile tile, int width, int height, Set<Location> corners) {
        return null;
    }

    //TODO Move to default provider ???
    @Override
    public Map<Location, FeatureArea> getBridgeAreas(Tile tile, int width, int height, Set<Location> locations) {
        if (!isEnabled()) return null;
        return locations.toMap(Functions.identity(), loc -> getBridgeArea(width, height, loc));
    }

    //TODO move to Area Provider ???
    private FeatureArea getBridgeArea(int width, int height, Location loc) {
        AffineTransform transform1;
        if (width == NORMALIZED_SIZE && height == NORMALIZED_SIZE) {
            transform1 = new AffineTransform();
        } else {
            double ratioX = width / (double)NORMALIZED_SIZE;
            double ratioY = height / (double)NORMALIZED_SIZE / getImageSizeRatio();
            transform1 = AffineTransform.getScaleInstance(ratioX,ratioY);
        }
        Area a = pluginGeometry.getBridgeArea(loc).createTransformedArea(transform1);
        return new FeatureArea(a, FeatureArea.DEFAULT_BRIDGE_ZINDEX);
    }


    private Area getBaseRoadAndCitySubstractions(TileDefinition tile) {
        Area sub = new Area();
        if (tile.hasTower()) {
            sub.add(getFeatureArea(tile, Tower.class, Location.TOWER).getTrackingArea());
        }
        if (tile.getFlier() != null) {
            sub.add(getFeatureArea(tile, null, Location.FLIER).getTrackingArea());
        }
        sub.add(getSubtractionArea(tile, false));
        return sub;
    }

    private Rectangle getFullRectangle() {
        return new Rectangle(0,0, NORMALIZED_SIZE-1, (int) (NORMALIZED_SIZE * getImageSizeRatio()));
    }
}
