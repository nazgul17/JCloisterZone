package com.jcloisterzone.board;

import com.jcloisterzone.game.Game;

import io.vavr.Tuple2;

public class TilePack {

    private final Game game;

    public TilePack(Game game) {
        this.game = game;
    }

    private TilePackState getState() {
        return game.getState().getTilePack();
    }

    public int totalSize() {
        return getState().totalSize();
    }

    public boolean isEmpty() {
        return getState().isEmpty();
    }

    public int size() {
        return getState().size();
    }

    public TileDefinition drawTile(int index) {
        Tuple2<TileDefinition, TilePackState> t = getState().drawTile(index);
        game.replaceState(state -> state.setTilePack(t._2).setDrawnTile(t._1));
        return t._1;
    }

    public TileDefinition drawTile(String tileId) {
        Tuple2<TileDefinition, TilePackState> t = getState().drawTile(tileId);
        game.replaceState(state -> state.setTilePack(t._2).setDrawnTile(t._1));
        return t._1;
    }
}
