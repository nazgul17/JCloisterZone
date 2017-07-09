package com.jcloisterzone.ui.grid.layer;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.SelectFollowerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.grid.layer.MeepleLayer.PositionedFigureImage;
import com.jcloisterzone.ui.resources.FeatureArea;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;


public class FollowerAreaLayer extends AbstractAreaLayer {

    private final MeepleLayer meepleLayer;

    public FollowerAreaLayer(GridPanel gridPanel, GameController gc, MeepleLayer meepleLayer) {
        super(gridPanel, gc);
        this.meepleLayer = meepleLayer;
    }

    @Override
    public SelectFollowerAction getAction() {
        return (SelectFollowerAction) super.getAction();
    }

    @Override
    protected Map<BoardPointer, FeatureArea> prepareAreas() {
        SelectFollowerAction action = getAction();
        int r = (int) (getTileWidth() / 3.0);
        int innerR = (int) (getTileWidth() / 4.2);
        int boxSize = (int) (getTileWidth() * MeepleLayer.FIGURE_SIZE_RATIO);

        Map<BoardPointer, FeatureArea> result = HashMap.empty();
        for (Tuple2<Position, Set<MeeplePointer>> t : action.groupByPosition()) {
            Position pos = t._1;
            for (MeeplePointer pointer : t._2) {
                PositionedFigureImage pfi = null;
                //IMMUTABLE TODO use meepleLayer.getMeeplePostions instead
                for (PositionedFigureImage item : meepleLayer.getPositionedFigures()) {
                    if (item.getFigure() instanceof Meeple) {
                        Meeple meeple = (Meeple) item.getFigure();
                        if (pointer.match(meeple)) {
                            pfi = item;
                            break;
                        }
                    }
                }
                if (pfi != null) {
                    ImmutablePoint offset = pfi.getScaledOffset(boxSize);
                    int x = offset.getX();
                    int y = offset.getY();
                    int width = (int) (getTileWidth() * MeepleLayer.FIGURE_SIZE_RATIO * pfi.xScaleFactor);
                    int height = (int) (pfi.heightWidthRatio * width * pfi.yScaleFactor);
                    int cx = x+(width/2);
                    int cy = y+(height/2);

                    Area trackingArea = new Area(new Ellipse2D.Double(cx-r, cy-r, 2*r, 2*r));
                    Area displyArea = new Area(trackingArea);
                    displyArea.subtract(new Area(new Ellipse2D.Double(cx-innerR, cy-innerR, 2*innerR, 2*innerR)));
                    if (pfi.order > 0) {
                        //more then one meeple on feature, remove part of are over prev meeple
                        int subWidth = r*4/5;
                        trackingArea.subtract(new Area(new Rectangle(cx-r, cy-r, subWidth, 2*r)));
                    }

                    FeatureArea fa = new FeatureArea(trackingArea, displyArea, pfi.order);
                    fa.setForceAreaColor(((Meeple) pfi.getFigure()).getPlayer().getColors().getMeepleColor());

                    AffineTransform translation = AffineTransform.getTranslateInstance(pos.x * getTileWidth(), pos.y * getTileHeight());
                    FeatureArea translated = fa.transform(translation);
                    result = result.put(pointer, translated);
                }
            }
        }
        return result;
    }


    @Override
    protected void performAction(BoardPointer ptr) {
        getAction().perform(getRmiProxy(), (MeeplePointer) ptr);
    }


}
