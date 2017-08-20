package com.jcloisterzone.game.state;

import java.io.Serializable;
import java.util.function.Function;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.board.EdgePattern;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;

@Immutable
public class PlacedTile implements Serializable {

    private static final long serialVersionUID = 1L;

    private final TileDefinition tile;
    private final Rotation rotation;

    public PlacedTile(TileDefinition tile, Rotation rotation) {
        this.tile = tile;
        this.rotation = rotation;
    }

    public TileDefinition getTile() {
        return tile;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public PlacedTile setTile(TileDefinition tile) {
        return new PlacedTile(tile, rotation);
    }

    public PlacedTile mapTile(Function<TileDefinition, TileDefinition> fn) {
        return new PlacedTile(fn.apply(tile), rotation);
    }

    public PlacedTile setRotation(Rotation rotation) {
        return new PlacedTile(tile, rotation);
    }

    public PlacedTile mapRotation(Function<Rotation, Rotation> fn) {
        return new PlacedTile(tile, fn.apply(rotation));
    }

    public EdgePattern getEdgePattern() {
        return tile.getEdgePattern().rotate(rotation);
    }
}
