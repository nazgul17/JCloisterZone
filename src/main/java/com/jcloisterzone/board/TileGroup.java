package com.jcloisterzone.board;

import java.io.Serializable;
import java.util.function.Function;

import com.jcloisterzone.Immutable;

import io.vavr.collection.Array;
import io.vavr.collection.Vector;

@Immutable
public class TileGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final Vector<TileDefinition> tiles;
    private final boolean active;

    public TileGroup(String name, Vector<TileDefinition> tiles, boolean active) {
        this.name = name;
        this.tiles = tiles;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public Vector<TileDefinition> getTiles() {
        return tiles;
    }

    public TileGroup setTiles(Vector<TileDefinition> tiles) {
        if (this.tiles == tiles) return this;
        return new TileGroup(name, tiles, active);
    }

    public TileGroup mapTiles(Function<Vector<TileDefinition>, Vector<TileDefinition>> fn) {
        return setTiles(fn.apply(tiles));
    }

    public boolean isActive() {
        return active;
    }

    public TileGroup setActive(boolean active) {
        if (this.active == active) return this;
        return new TileGroup(name, tiles, active);
    }

    public int size() {
        return tiles.size();
    }

    public boolean isEmpty() {
        return tiles.isEmpty();
    }

}
