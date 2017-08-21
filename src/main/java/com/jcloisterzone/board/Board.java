package com.jcloisterzone.board;

import java.awt.Rectangle;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import io.vavr.control.Option;


@Deprecated
public class Board {

    private final GameState state;
    private final java.util.Map<Position, Option<Tile>> tiles = new java.util.HashMap<>();

    private Rectangle bounds;

//	protected Set<TunnelEnd> tunnels = new HashSet<>();
//	protected Map<Integer, TunnelEnd> openTunnels = new HashMap<>(); //tunnel with open one side


    public Board(GameState state) {
       this.state = state;
    }

    private PlacedTile getPlacedTile(Position pos) {
        return state.getPlacedTiles().get(pos).getOrNull();
    }





    public Tile get(Position pos) {
        Option<Tile> o = tiles.get(pos);
        if (o == null) {
            PlacedTile t = getPlacedTile(pos);
            if (t == null) {
                tiles.put(pos, Option.none());
                return null;
            } else {
                Tile tile = new Tile(state, pos, t);
                tiles.put(pos, Option.some(tile));
                return tile;
            }
        }
        return o.getOrNull();
    }

    public Feature get(FeaturePointer fp) {
        return state.getFeatureMap().get(fp).getOrNull();
    }

    public Stream<Tile> getPlacedTiles() {
        return Stream.ofAll(state.getPlacedTiles()).map(t -> get(t._1));
    }



//
//    public Rectangle getBounds() {
//        if (bounds == null) {
//            bounds = computeBounds();
//        }
//        return bounds;
//    }
//
//    public int getMaxX() {
//        return getBounds().width + getBounds().x;
//    }
//
//    public int getMinX() {
//        return getBounds().x;
//    }
//
//    public int getMaxY() {
//        return getBounds().height + getBounds().y;
//    }
//
//    public int getMinY() {
//        return getBounds().y;
//    }
}
