package com.jcloisterzone.ui.grid.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Figure;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.figure.neutral.Fairy;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;

import io.vavr.Tuple2;
import io.vavr.collection.Stream;

public class MeepleLayer extends AbstractGridLayer {

    public static class MeppleLayerModel {
        ArrayList<FigureImage> outsideBridge = new ArrayList<>();
        ArrayList<FigureImage> onBridge = new ArrayList<>();
    }

    public static final double FIGURE_SIZE_RATIO = 0.35f;

    /**
     * Corn circles allows multiple meeples on single feature.
     * In such case double meeple should be displayed after common ones.
     */
    private MeppleLayerModel model = new MeppleLayerModel();
    //private PositionedFigureImage fairyOnFeature = null;


    public MeepleLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        gc.register(this);
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        if (ev.hasMeeplesChanged() || ev.hasNeutralFiguresChanged()) {
            updateModel(ev.getCurrentState());
            gridPanel.repaint();
        }
    }

    private void updateModel(GameState state) {
        MeppleLayerModel model = new MeppleLayerModel();

        HashMap<FeaturePointer, LinkedList<Figure<?>>> onFeature = new HashMap<>();
        LinkedList<Tuple2<Position, NeutralFigure<?>>> onTile = new LinkedList<>();

        for (Tuple2<Meeple, FeaturePointer> t : gc.getGame().getState().getDeployedMeeples()) {
            LinkedList<Figure<?>> list = onFeature.get(t._2);
            if (list == null) {
                list = new LinkedList<>();
                onFeature.put(t._2, list);
            }
            list.add(t._1);
        }

        for (Tuple2<NeutralFigure<?>, BoardPointer> t : gc.getGame().getState().getNeutralFigures().getDeployedNeutralFigures()) {
            if (t._2 instanceof Position) {
                onTile.add(new Tuple2<>((Position) t._2, t._1));
            } else {
                FeaturePointer fp = t._2.asFeaturePointer();
                LinkedList<Figure<?>> list = onFeature.get(fp);
                if (list == null) {
                    list = new LinkedList<>();
                    onFeature.put(fp, list);
                }
                list.add(t._1);
            }
        }

        onFeature.forEach((fp, list) -> {
            //TODO rearrange

            int order = 0;

            for (Figure<?> fig : list) {
                Tile tile = state.getBoard().get(fp.getPosition());

                FigureImage fi = new FigureImage(fig);
                fi.offset = getFigureOffset(tile, fig, fp);
                if (order > 0) {
                    fi.offset = fi.offset.add(10 * order, 0);
                }
                fillFigureImage(fi, tile, fig, fp);

                model.outsideBridge.add(fi);
                order++;
            }
        });

        onTile.forEach(t -> {
            Position pos = t._1;
            NeutralFigure<?> fig = t._2;
            Tile tile = state.getBoard().get(pos);

            FigureImage fi = new FigureImage(fig);
            fi.offset = getFigureOffset(tile, fig, pos);
            fillFigureImage(fi, tile, fig, pos);

            model.outsideBridge.add(fi);
        });

        this.model = model;
    }

    public Stream<FigureImage> getAllFigureImages() {
        return Stream.concat(model.onBridge, model.outsideBridge);
    }

    private ImmutablePoint getFigureOffset(Tile tile, Figure<?> fig, BoardPointer ptr) {
        ImmutablePoint point = getFigureTileOffset(tile, fig, ptr);
        Position pos = ptr.getPosition();
        return point.add(100 * pos.x, 100 * pos.y);
    }

    private ImmutablePoint getFigureTileOffset(Tile tile, Figure<?> fig, BoardPointer ptr) {
        if (ptr instanceof Position) {
            if (fig instanceof Fairy) {
                //fairy on tile
                return new ImmutablePoint(62, 52);
            } else {
                return new ImmutablePoint(50, 50);
            }
        }
        if (fig instanceof Fairy) {
            //fairy next to follower
            FeaturePointer fp = (FeaturePointer) ptr; // MeeplePointer has been converted to FeaturePointer during group by!
            return rm.getMeeplePlacement(tile, SmallFollower.class, fp.getLocation());
        } else {
            Meeple m = (Meeple) fig;
            FeaturePointer fp = (FeaturePointer) ptr;
            return rm.getMeeplePlacement(tile, m.getClass(), fp.getLocation());
        }
    }

    private void fillFigureImage(FigureImage fi, Tile tile, Figure<?> fig, BoardPointer ptr) {
        double baseScale = FIGURE_SIZE_RATIO * gridPanel.getMeepleScaleFactor();
        if (fig instanceof NeutralFigure<?>) {
            final boolean mageOrWitch = fig instanceof Mage || fig instanceof Witch;
            final boolean count = fig instanceof Count;

            Image image = rm.getImage("neutral/"+fig.getClass().getSimpleName().toLowerCase());
            fi.img = image;

            if (mageOrWitch || count) {
                fi.scaleX = 1.2 * baseScale;
                fi.scaleY = 1.2 * baseScale;
            } else if (fig instanceof Fairy) {
                fi.scaleX = baseScale;
                fi.scaleY = baseScale;
            }
            // no scale for dragon
        } else {
            Meeple m = (Meeple) fig;
            FeaturePointer fp = ptr.asFeaturePointer();
            //Feature feature = tile.getFeature(fp.getLocation());
            Color color = m.getPlayer().getColors().getMeepleColor();
            LayeredImageDescriptor lid = new LayeredImageDescriptor(m.getClass(), color);
            lid.setAdditionalLayer(getExtraDecoration(m.getClass(), fp));
            Image image = rm.getLayeredImage(lid);
            if (fp.getLocation() == Location.ABBOT) {
                image = rotate(image, 90);
            }
            fi.img = image;
            fi.scaleX = baseScale;
            fi.scaleY = baseScale;
        }
        return;
    }

    @Override
    public void paint(Graphics2D g) {
        int baseSize = getTileWidth();
        AffineTransform originalTransform = g.getTransform();
        for (FigureImage fi : model.outsideBridge) {
            Image scaled = fi.getScaledInstance(baseSize);
            int width = scaled.getWidth(null);
            int height = scaled.getHeight(null);

            ImmutablePoint scaledOffset = fi.offset.scale(baseSize, getTileHeight());
            int x = scaledOffset.getX();
            int y = scaledOffset.getY();

            g.rotate(-gridPanel.getBoardRotation().getTheta(), x, y);
            g.drawImage(scaled, x - width / 2, y - height / 2, gridPanel);
            g.setTransform(originalTransform);
        }

    }

    // Legacy

//    private void paintPositionedImage(Graphics2D g, PositionedImage mi, int squareSize) {
//        ImageData i = mi.getScaledImageData(squareSize);
//
//        int x = getOffsetX(mi.position) + i.offset.getX();
//        int y = getOffsetY(mi.position) + i.offset.getY();
//
//        g.rotate(-gridPanel.getBoardRotation().getTheta(), x+i.boxSize/2, y+i.boxSize/2);
//        g.drawImage(i.image, x, y, gridPanel);
//        g.rotate(gridPanel.getBoardRotation().getTheta(), x+i.boxSize/2, y+i.boxSize/2);
//    }

//    @Override
//    public void zoomChanged(int squareSize) {
//        for (PositionedFigureImage mi : images) {
//            mi.resetScaledImageData();
//        }
//        super.zoomChanged(squareSize);
//    }

//    private PositionedFigureImage createMeepleImage(Meeple meeple, Color c, FeaturePointer fp) {
//        Tile tile = getGame().getBoard().get(fp.getPosition());
//        Feature feature = getGame().getBoard().get(fp);
//        ImmutablePoint offset = rm.getMeeplePlacement(tile, meeple.getClass(), fp.getLocation());
//        LayeredImageDescriptor lid = new LayeredImageDescriptor(meeple.getClass(), c);
//        lid.setAdditionalLayer(getExtraDecoration(meeple.getClass(), fp));
//        Image image = rm.getLayeredImage(lid);
//        if (fp.getLocation() == Location.ABBOT) {
//            image = rotate(image, 90);
//        }
//        return new PositionedFigureImage(meeple, fp, null, offset, image, feature instanceof Bridge);
//    }
//
//    private PositionedFigureImage createNeutralFigureImage(NeutralFigure<?> fig, BoardPointer ptr) {
//        final boolean mageOrWitch = fig instanceof Mage || fig instanceof Witch;
//        final boolean count = fig instanceof Count;
//        boolean bridgePlacement = false;
//        ImmutablePoint offset;
//        FeaturePointer fp = null;
//        String nextToMeeple = null;
//        if (ptr instanceof FeaturePointer) {
//            fp = (FeaturePointer) ptr;
//        } else if (ptr instanceof MeeplePointer) {
//            MeeplePointer mptr = (MeeplePointer) ptr;
//            nextToMeeple = mptr.getMeepleId();
//            fp = mptr.asFeaturePointer();
//        }
//        if (count) {
//            offset = DefaultResourceManager.COUNT_OFFSETS.get(fp.getLocation()).get();
//        } else if (fp != null) {
//            Tile tile = getGame().getBoard().get(fp.getPosition());
//            Feature feature = getGame().getBoard().get(fp);
//            bridgePlacement = feature instanceof Bridge;
//            offset = rm.getMeeplePlacement(tile, SmallFollower.class, fp.getLocation());
//            if (nextToMeeple != null) {
//                //for better fairy visibilty
//                offset = offset.translate(-5, 0);
//            }
//        } else {
//            if (fig instanceof Fairy) {
//                //fairy on tile
//                offset = new ImmutablePoint(62, 52);
//            } else {
//                offset = new ImmutablePoint(50, 50);
//            }
//        }
//        Image image = rm.getImage("neutral/"+fig.getClass().getSimpleName().toLowerCase());
//
//        if (mageOrWitch) {
//            offset = offset.translate(0, -10);
//        }
//        PositionedFigureImage pfi = new PositionedFigureImage(fig, ptr.asFeaturePointer(), nextToMeeple, offset, image, bridgePlacement);
//
//        if (fig instanceof Dragon) {
//            pfi.sizeRatio = 1.0;
//        }
//
//        if (mageOrWitch || count) {
//            pfi.xScaleFactor = pfi.yScaleFactor = 1.2;
//        }
////        if (nextToMeeple != null) {
////            fairyOnFeature = pfi;
////        }
//        return pfi;
//    }

//    private void rearrangeMeeples(FeaturePointer fp) {
//        int order = 0;
//        boolean hasOther = false;
//
//        LinkedList<PositionedFigureImage> featureImages = new LinkedList<>();
//        PositionedFigureImage withFairy = null;
//        boolean isFairyOnCurrentFeature = false;
//
//        //iterate revese ti have small follower is placement order
//        ListIterator<PositionedFigureImage> iter = images.listIterator(images.size());
//        while (iter.hasPrevious()) {
//            PositionedFigureImage mi = iter.previous();
//            if (mi.location == fp.getLocation() && mi.position.equals(fp.getPosition())) {
//                if (fairyOnFeature == mi) {
//                    //dont add to array, it will be assigned from
//                    hasOther = true;
//                    isFairyOnCurrentFeature = true;
//                } else {
//                    if (mi.getFigure() instanceof SmallFollower) {
//                        //small followers first
//                        featureImages.addFirst(mi);
//                    } else {
//                        //others on top
//                        hasOther = true;
//                        featureImages.addLast(mi);
//                    }
//                    if (fairyOnFeature != null && mi.getFigure() instanceof Meeple) {
//                        if (((Meeple) mi.getFigure()).getId().equals(fairyOnFeature.nextToMeeple)) {
//                            withFairy = mi;
//                        }
//                    }
//                }
//            }
//        }
//
//        if (withFairy == null && isFairyOnCurrentFeature) {
//            fairyOnFeature.order = 0; //show lonely fairy on first position
//            order++;
//        }
//
//        for (PositionedFigureImage mi : featureImages) {
//            mi.order = order++;
//            //System.err.println("Order: "+mi.getFigure().toString() + " = " + mi.order);
//            if (mi == withFairy) {
//                fairyOnFeature.order = order++;
//                //System.err.println("Order: "+fairyOnFeature.getFigure().toString() + " = " + fairyOnFeature.order);
//            }
//        }
//
//        if (order > 1 && hasOther) {
//            Collections.sort(images, new Comparator<PositionedFigureImage>() {
//                @Override
//                public int compare(PositionedFigureImage o1, PositionedFigureImage o2) {
//                    return o1.order - o2.order;
//                }
//            });
//        }
//    }
//
//    private void meepleDeployed(MeepleEvent ev) {
//        Color c = ev.getMeeple().getPlayer().getColors().getMeepleColor();
//        images.add(createMeepleImage(ev.getMeeple(), c, ev.getTo()));
//        rearrangeMeeples(ev.getTo());
//    }
//
//    private void neutralFigureDeployed(NeutralFigureMoveEvent ev) {
//        images.add(createNeutralFigureImage(ev.getFigure(), ev.getTo()));
//        if (ev.getTo() instanceof FeaturePointer || ev.getTo() instanceof MeeplePointer) {
//            rearrangeMeeples(ev.getTo().asFeaturePointer());
//        }
//    }

//    private void figureUndeployed(Figure figure, BoardPointer from) {
//        Iterator<PositionedFigureImage> iter = images.iterator();
//        while (iter.hasNext()) {
//            PositionedFigureImage mi = iter.next();
//            if (mi.getFigure().equals(figure)) {
//                if (mi == fairyOnFeature) {
//                    fairyOnFeature = null;
//                }
//                iter.remove();
//                break;
//            }
//        }
//        if (from instanceof FeaturePointer || from instanceof MeeplePointer) {
//            rearrangeMeeples(from.asFeaturePointer());
//        }
//    }
//
//    private void meepleUndeployed(MeepleEvent ev) {
//        figureUndeployed(ev.getMeeple(), ev.getFrom());
//    }
//
//    private void neutralFigureUndeployed(NeutralFigureMoveEvent ev) {
//        figureUndeployed(ev.getFigure(), ev.getFrom());
//    }


    //TODO path from Theme
    private String getExtraDecoration(Class<? extends Meeple> type, FeaturePointer fp) {
        if (Follower.class.isAssignableFrom(type) && fp.getLocation().isFarmLocation()) {
            return "player-meeples/decorations/farm";
        }
        if (fp.getLocation() == Location.TOWER) {
            if (BigFollower.class.isAssignableFrom(type)) {
                return "player-meeples/decorations/big_tower";
            } else {
                return "player-meeples/decorations/tower";
            }
        }
        return null;
    }

//    public class PositionedImage {
//        public final Position position;
//        public final ImmutablePoint offset;
//        public final Image sourceImage;
//        public double heightWidthRatio = 1.0;
//        public double xScaleFactor = 1.0;
//        public double yScaleFactor = 1.0;
//        public double sizeRatio = FIGURE_SIZE_RATIO;
//
//        private ImageData scaledImageData;
//
//        public PositionedImage(Position position, ImmutablePoint offset, Image sourceImage) {
//            this.position = position;
//            this.offset = offset;
//            this.sourceImage = sourceImage;
//        }
//
//        public ImmutablePoint getScaledOffset(int boxSize) {
//            return offset.scale(getTileWidth(), getTileHeight(), boxSize);
//        }
//
//        public ImageData getScaledImageData(int squareSize) {
//            if (scaledImageData == null) {
//
//                int boxSize = (int) (getTileWidth() * sizeRatio * gridPanel.getMeepleScaleFactor()); //TODO no resize - direct image resize???
//
//                ImmutablePoint scaledOffset = getScaledOffset(boxSize);
//
//                int width = (int) (boxSize * xScaleFactor);
//                int height = (int) (heightWidthRatio * boxSize * yScaleFactor);
//
//                Image scaledImage = sourceImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
//
//                scaledImageData = new ImageData(scaledImage, scaledOffset, boxSize);
//            }
//
//            return scaledImageData;
//        }
//
////        public void resetScaledImageData() {
////            scaledImageData = null;
////        }
//    }
//
//    public class PositionedFigureImage extends PositionedImage {
//        public final Figure figure;
//        public final Location location;
//        public final boolean bridgePlacement;
//        public final String nextToMeeple;
//        public int order;
//
//        public PositionedFigureImage(Figure figure, FeaturePointer fp, String nextToMeeple, ImmutablePoint offset, Image sourceImage, boolean bridgePlacement) {
//            super(fp.getPosition(), offset, sourceImage);
//            this.figure = figure;
//            location = fp.getLocation();
//            this.bridgePlacement = bridgePlacement;
//            this.nextToMeeple = nextToMeeple;
//        }
//
//        @Override
//        public ImmutablePoint getScaledOffset(int boxSize) {
//            ImmutablePoint point = offset;
//            if (order > 0) {
//                point = point.translate(10*order, 0);
//            }
//            return point.scale(getTileWidth(), getTileHeight(), boxSize);
//        }
//
//        public Figure getFigure() {
//            return figure;
//        }
//    }
//
//    private class ImageData {
//        public final ImmutablePoint offset;
//        public final Image image;
//        public final int boxSize;
//
//        public ImageData(Image image, ImmutablePoint offset, int boxSize) {
//            super();
//            this.image = image;
//            this.offset = offset;
//            this.boxSize = boxSize;
//        }
//    }

    //TODO better use affine transform while drawing
    @Deprecated
    public static Image rotate(Image img, double angle) {
        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
               cos = Math.abs(Math.cos(Math.toRadians(angle)));

        int w = img.getWidth(null), h = img.getHeight(null);

        int neww = (int) Math.floor(w*cos + h*sin),
            newh = (int) Math.floor(h*cos + w*sin);

        BufferedImage bimg = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bimg.createGraphics();

        g.translate((neww-w)/2, (newh-h)/2);
        g.rotate(Math.toRadians(angle), w/2, h/2);
        g.drawRenderedImage(toBufferedImage(img), null);
        g.dispose();
        return bimg;
    }

    private static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

}