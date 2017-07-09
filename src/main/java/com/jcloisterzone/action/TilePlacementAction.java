package com.jcloisterzone.action;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;
import com.jcloisterzone.ui.grid.layer.TilePlacementLayer;
import com.jcloisterzone.ui.resources.TileImage;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class TilePlacementAction extends PlayerAction<TilePlacement> implements ForwardBackwardListener {

    private final TileDefinition tile;
    private ForwardBackwardListener forwardBackwardDelegate;

    private Rotation tileRotation = Rotation.R0;

    public TilePlacementAction(TileDefinition tile, Set<TilePlacement> options) {
        super(options);
        this.tile = tile;
    }

    public TileDefinition getTile() {
        return tile;
    }

    public Rotation getTileRotation() {
        return tileRotation;
    }

    public void setTileRotation(Rotation tileRotation) {
        this.tileRotation = tileRotation;
    }

    @Override
    public void forward() {
        forwardBackwardDelegate.forward();
    }

    @Override
    public void backward() {
        forwardBackwardDelegate.backward();
    }

    public Map<Position, Set<Rotation>> groupByPosition() {
        return getOptions()
            .groupBy(tp -> tp.getPosition())
            .mapValues(setOfPlacements -> setOfPlacements.map(tp -> tp.getRotation()));
    }

    public Stream<Rotation> getRotations(Position pos) {
        return Stream.ofAll(getOptions())
            .filter(tp -> tp.getPosition().equals(pos))
            .map(tp -> tp.getRotation());
    }

    @Override
    public Image getImage(Player player, boolean active) {
        TileImage tileImg = client.getResourceManager().getTileImage(tile, Rotation.R0);
        Insets ins = tileImg.getOffset();
        Image img =  tileImg.getImage();
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        BufferedImage bi = UiUtils.newTransparentImage(w+2, h+2);
        AffineTransform at = tileRotation.getAffineTransform(w, h);
        at.concatenate(AffineTransform.getTranslateInstance(1, 1));
        Graphics2D ig = bi.createGraphics();
        ig.setColor(Color.BLACK);
        ig.drawRect(ins.left, ins.top, w+1-ins.left-ins.right, h+1-ins.top-ins.bottom);
        ig.drawImage(img, at, null);
        return bi;
    }

    @Override
    public void perform(RmiProxy server, TilePlacement tp) {
        server.placeTile(tp.getRotation(), tp.getPosition());
    }

    @Override
    protected Class<? extends ActionLayer<?>> getActionLayerType() {
        return TilePlacementLayer.class;
    }

    @Override
    public String toString() {
        return "place tile " + tile.getId();
    }

    public ForwardBackwardListener getForwardBackwardDelegate() {
        return forwardBackwardDelegate;
    }

    public void setForwardBackwardDelegate(
            ForwardBackwardListener forwardBackwardDelegate) {
        this.forwardBackwardDelegate = forwardBackwardDelegate;
    }
}
