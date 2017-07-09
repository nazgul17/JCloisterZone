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
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;
import com.jcloisterzone.ui.grid.layer.TilePlacementLayer;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.ui.resources.TileImage;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;


@LinkedGridLayer(TilePlacementLayer.class)
public class TilePlacementAction extends PlayerAction<TilePlacement> {

    private final TileDefinition tile;

    public TilePlacementAction(TileDefinition tile, Set<TilePlacement> options) {
        super(options);
        this.tile = tile;
    }

    public TileDefinition getTile() {
        return tile;
    }

    public Map<Position, Set<Rotation>> groupByPosition() {
        return getOptions()
            .groupBy(tp -> tp.getPosition())
            .mapValues(setOfPlacements -> setOfPlacements.map(tp -> tp.getRotation()));
    }

    public Set<Rotation> getRotations(Position pos) {
        return Stream.ofAll(getOptions())
            .filter(tp -> tp.getPosition().equals(pos))
            .map(tp -> tp.getRotation())
            .toSet();
    }

    @Override
    public void perform(RmiProxy server, TilePlacement tp) {
        server.placeTile(tp.getRotation(), tp.getPosition());
    }

    @Override
    public String toString() {
        return "place tile " + tile.getId();
    }


}
