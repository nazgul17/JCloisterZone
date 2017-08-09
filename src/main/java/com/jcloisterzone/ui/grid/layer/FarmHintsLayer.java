package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.FeatureArea;
import com.jcloisterzone.ui.resources.ResourceManager;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

public class FarmHintsLayer extends AbstractGridLayer {

    static class FarmHint {
        public Farm farm;
        public Area area;
        public Area scaledArea;
        public List<Color> colors;


        public FarmHint(Farm farm, Area area, List<Color> colors) {
            this.farm = farm;
            this.area = area;
            this.colors = colors;
        }
    }

    static class FarmHintsLayerModel {
        List<FarmHint> hints = List.empty();
    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private static final int FULL_SIZE = 300;
    private static final AlphaComposite HINT_ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);

    private FarmHintsLayerModel model = new FarmHintsLayerModel();

//    private boolean doRefreshHints;
//    final Map<Tile, Map<Location, FeatureArea>> areas = new HashMap<>();
//    private final List<FarmHint> hints = new ArrayList<>();

    public FarmHintsLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        gc.register(this);
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        if (ev.hasMeeplesChanged() || ev.hasPlacedTilesChanged()) {
            model = createModel(ev.getCurrentState());
            gridPanel.repaint();
        }
    }

    @Override
    public void paint(Graphics2D g2) {
        if (!visible) return;

        Composite old = g2.getComposite();
        g2.setComposite(HINT_ALPHA_COMPOSITE);
        int sqSize = getTileWidth();
        Double scale = sqSize == FULL_SIZE ? null : (double) sqSize / FULL_SIZE;
        TextureFactory textures = new TextureFactory(sqSize);

        for (FarmHint fh : model.hints) {
            if (fh.scaledArea == null) {
                if (scale == null) {
                    fh.scaledArea = fh.area;
                } else {
                    fh.scaledArea = fh.area.createTransformedArea(AffineTransform.getScaleInstance(scale, scale));
                }
            }
            if (fh.colors.length() > 1) {
                g2.setPaint(textures.createMultiColor(fh.colors.toJavaArray(Color.class)));
            } else {
                g2.setPaint(textures.create(fh.colors.get()));
            }
            g2.fill(fh.scaledArea);
        }
        g2.setPaint(null);
        g2.setComposite(old);
    }

    //@Subscribe
    public void onTileEvent(/*TileEvent ev*/) {
        //IMMUTABLE TODO
        logger.warn("IMMUTABLE TODO");
//
//
//        TileDefinition tile = ev.getTileDefinition();
//        if (ev.getType() == TileEvent.PLACEMENT) {
//            Set<Location> farmLocations = new HashSet<>();
//            for (Feature f : tile.getFeatures()) {
//                if (f instanceof Farm) {
//                    farmLocations.translate(f.getLocation());
//                }
//            }
//            if (farmLocations.isEmpty()) return;
//            int w = gridPanel.getTileWidth();
//            int h = gridPanel.getTileHeight();
//            Map<Location, FeatureArea> tAreas = rm.getFeatureAreas(tile, FULL_SIZE, FULL_SIZE * h / w, farmLocations);
//            areas.put(tile, tAreas);
//            refreshHints();
//        }
//        if (ev.getType() == TileEvent.REMOVE) {
//            areas.remove(tile);
//            refreshHints();
//        }

    }

    private FarmHintsLayerModel createModel(GameState state) {
        ResourceManager rm = gc.getClient().getResourceManager();
        Board board = state.getBoard();

        FarmHintsLayerModel model = new FarmHintsLayerModel();
        model.hints = Stream.ofAll(state.getBoard().getAllFeatures())
            .filter(Predicates.instanceOf(Farm.class))
            .map(feature -> (Farm) feature)
            .map(farm -> new Tuple2<>(farm, farm.getOwners(state)))
            .filter(t -> {
                Farm farm = t._1;
                //don't display unimportant farms
                if (t._2.isEmpty()) {
                    boolean hasCity = !farm.getAdjoiningCities().isEmpty() || farm.isAdjoiningCityOfCarcassonne();
                    return farm.getPlaces().size() > 1 && hasCity;
                }
                return true;
            })
            .map(t -> {
                Farm farm = t._1;
                Area area = new Area();

                for (FeaturePointer fp : farm.getPlaces()) {
                    Tile tile = board.get(fp.getPosition());
                    Location loc = fp.getLocation();
                    FeatureArea fa = rm.getFeatureAreas(tile, FULL_SIZE, FULL_SIZE, HashSet.of(loc)).get(loc).get();
                    assert fa != null;
                    Area add = fa.getDisplayArea() == null ? fa.getTrackingArea() : fa.getDisplayArea();
                    area.add(transformArea(add, fp.getPosition()));
                }

                List<Color> colors;
                if (t._2.isEmpty()) {
                    colors = List.of(Color.DARK_GRAY);
                } else {
                    colors = Stream.ofAll(t._2).map(p -> p.getColors().getMeepleColor()).toList();
                }
                return new FarmHint(farm, area, colors);
            })
            .toList();

        return model;
    }

//
//    private void fillHints() {
//        hints.clear();
//        final Set<Feature> processed = new HashSet<>();
//        for (Entry<Tile, Map<Location, FeatureArea>> entry : areas.entrySet()) {
//            for (Feature f : entry.getKey().getFeatures()) {
//                if (!(f instanceof Farm)) continue;
//                if (processed.contains(f)) continue;
//
//                FarmHint fh = f.walk(new FeatureVisitor<FarmHint>() {
//                    FarmHint result = new FarmHint(new Area(), null);
//                    int x = Integer.MAX_VALUE;
//                    int y = Integer.MAX_VALUE;
//                    int size = 0;
//                    boolean hasCity = false;
//                    int[] power = new int[getGame().getState().getPlayers().length()];
//
//                    @Override
//                    public VisitResult visit(Feature feature) {
//                        Farm f = (Farm) feature;
//                        processed.add(f);
//                        size++;
//                        hasCity = hasCity || f.getAdjoiningCities() != null || f.isAdjoiningCityOfCarcassonne();
//                        for (Meeple m : f.getMeeples()) {
//                            if (m instanceof Follower) {
//                                power[m.getPlayer().getIndex()] += ((Follower)m).getPower();
//                            }
//                            if (m instanceof Barn) {
//                                power[m.getPlayer().getIndex()] += 1;
//                            }
//                        }
//                        Position pos = f.getTile().getPosition();
//                        if (pos.x < x) {
//                            if (x != Integer.MAX_VALUE) result.area.transform(AffineTransform.getTranslateInstance(FULL_SIZE * (x-pos.x), 0));
//                            x = pos.x;
//                        }
//                        if (pos.y < y) {
//                            if (y != Integer.MAX_VALUE) result.area.transform(AffineTransform.getTranslateInstance(0, FULL_SIZE * (y-pos.y)));
//                            y = pos.y;
//                        }
//                        Map<Location, FeatureArea> tileAreas = areas.getPlayer(f.getTile());
//                        if (tileAreas != null) { //sync issue, feature can be extended in other thread, so it is not registered in areas yet
//                            Area featureArea = new Area(tileAreas.getPlayer(f.getLocation()).getTrackingArea());
//                            featureArea.transform(AffineTransform.getTranslateInstance(FULL_SIZE * (pos.x-x), FULL_SIZE*(pos.y-y)));
//                            result.area.add(featureArea);
//                        }
//                        return VisitResult.CONTINUE;
//                    }
//
//                    @Override
//                    public FarmHint getResult() {
//                        result.position = new Position(x, y);
//
//                        int bestPower = 0;
//                        List<Integer> bestPlayerIndexes = new ArrayList<>();
//                        for (int i = 0; i < power.length; i++) {
//                            if (power[i] == bestPower) {
//                                bestPlayerIndexes.add(i);
//                            }
//                            if (power[i] > bestPower) {
//                                bestPower = power[i];
//                                bestPlayerIndexes.clear();
//                                bestPlayerIndexes.add(i);
//                            }
//                        }
//                        if (bestPower == 0) {
//                            if (size < 2 || !hasCity) return null; //don't display unimportant farms
//                            result.colors = new Color[] { Color.DARK_GRAY };
//                        } else {
//                            result.colors = new Color[bestPlayerIndexes.size()];
//                            int i = 0;
//                            for (Integer index : bestPlayerIndexes) {
//                                result.colors[i++] = getGame().getPlayer(index).getColors().getMeepleColor();
//                            }
//                        }
//                        return result;
//                    }
//                });
//                if (fh == null) continue; //to small farm
//                hints.add(fh);
//            }
//        }
//    }

    @Override
    public void zoomChanged(int squareSize) {
        for (FarmHint fh : model.hints) {
            fh.scaledArea = null;
        }
    }
}
