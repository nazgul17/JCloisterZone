package com.jcloisterzone.action;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.grid.layer.TilePlacementLayer;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;


@LinkedGridLayer(TilePlacementLayer.class)
public class TilePlacementAction extends PlayerAction<TilePlacement> {

    private static final long serialVersionUID = 1L;

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
    public void perform(GameController gc, TilePlacement tp) {
        RmiProxy server = gc.getRmiProxy();
        server.placeTile(tp.getRotation(), tp.getPosition());
    }

    @Override
    public String toString() {
        return "place tile " + tile.getId();
    }
}
