package com.jcloisterzone.board;

import java.awt.Rectangle;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.MultiTileFeature;
import com.jcloisterzone.game.CachedValue;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.capability.BridgeCapability;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.control.Option;


/**
 * Board represent game board with any size, so <code>Tile</code> instances ale
 * stored in <code>Map</code>. Board supplies proper merging of score objects
 * (<code>Road</code>, <code>City</code> or <code>Farm</code>)
 * and diagonal merge for <code>Cloister<code> instances.

 */
public class Board {
    //TODO move tiles inside and make it immutable ?

    private final Game game;

    private transient final java.util.Map<Position, Option<Tile>> tiles = new java.util.HashMap<>();

    private transient final CachedValue<Rectangle> bounds;

//	protected Set<TunnelEnd> tunnels = new HashSet<>();
//	protected Map<Integer, TunnelEnd> openTunnels = new HashMap<>(); //tunnel with open one side


    public Board(Game game) {
        this.game = game;
        bounds = new CachedValue<>(game, this::computeBounds);
    }

    private Tuple2<TileDefinition, Rotation> getPlacedTile(Position pos) {
        return game.getState().getPlacedTiles().get(pos).getOrNull();
    }

    private EdgePattern getEdgetPattern(Position pos) {
        Tuple2<TileDefinition, Rotation> placed = getPlacedTile(pos);
        if (placed != null) {
            return placed._1.getEdgePattern().rotate(placed._2);
        }

        return new EdgePattern(
            Position.ADJACENT.map((loc, offset) -> {
                Position adj = pos.add(offset);
                Tuple2<TileDefinition, Rotation> adjTile = getPlacedTile(adj);
                if (adjTile == null) {
                    return new Tuple2<>(loc, EdgeType.UNKNOWN);
                } else {
                    EdgeType edge = adjTile._1.getEdgePattern().rotate(adjTile._2).at(loc.rev());
                    return new Tuple2<>(loc, edge);
                }
            })
         );
    }

    public Stream<Tuple2<Position, EdgePattern>> getAvailablePlacements() {
        java.util.Set<Position> used = new java.util.HashSet<>();
        Map<Position, Tuple2<TileDefinition, Rotation>> placedTiles = game.getState().getPlacedTiles();

        return Stream.ofAll(placedTiles).flatMap(item -> {
            Position pos = item._1;
            java.util.List<Tuple2<Position, EdgePattern>> avail = new java.util.ArrayList<>(4);
            for (Position offset: Position.ADJACENT.values()) {
                Position adj = pos.add(offset);
                if (!used.contains(adj) && !placedTiles.containsKey(adj)) {
                    avail.add(new Tuple2<Position, EdgePattern>(adj, getEdgetPattern(adj)));
                    used.add(adj);
                }
            }
            return avail;
        });
    }

    public Stream<Tuple2<Position, EdgePattern>> getAvailablePlacements(TileDefinition tile) {
        return getAvailablePlacements().filter(t -> {
            //TODO check bridge
            return game.isTilePlacementAllowed(tile, t._1);
        });
    }

    public Stream<Tuple2<Position, EdgePattern>> getHoles() {
        return getAvailablePlacements().filter(t -> t._2.wildcardSize() == 0);
    }

//    /**
//     * Updates current avail moves for next turn
//     * @param tile next tile
//     */
//    public void refreshAvailablePlacements(Tile tile) {
//        Rotation tileRotation = tile.getRotation();
//        currentAvailMoves.clear();
//        for (Position p : availMoves.keySet()) {
//            EnumSet<Rotation> allowed = EnumSet.noneOf(Rotation.class);
//            for (Rotation rotation: Rotation.values()) {
//                tile.setRotation(rotation);
//                if (!isPlacementAllowed(tile, p)) {
//                    //not allowed according standard rules, must check if deployed bridge can allow it
//                    if (!game.hasCapability(BridgeCapability.class)) continue;
//                    if (!game.getCapability(BridgeCapability.class).isTilePlacementWithBridgePossible(tile, p)) continue;
//                }
//                if (!game.isTilePlacementAllowed(tile, p)) continue;
//                allowed.add(rotation);
//            }
//            if (!allowed.isEmpty()) {
//                currentAvailMoves.put(p, allowed);
//            }
//        }
//        tile.setRotation(tileRotation); //reset rotation
//    }


//    protected void availMovesAdd(Position pos) {
//        availMoves.put(pos, EdgePattern.forEmptyTile(this, pos));
//    }
//
//    protected void availMovesRemove(Position pos) {
//        availMoves.remove(pos);
//    }

//    public EdgePattern getAvailMoveEdgePattern(Position pos) {
//        return availMoves.get(pos);
//    }


    /**
     * Place tile on given position. Check for correct placement (check if neigbours
     * edges match with tile edges according to Carcassonne rules
     * @param tile tile to place
     * @param p position to place
     * @throws IllegalMoveException if placement is violate game rules
     */
    //TODO rename to placeTile
    //IMMUTABLE TODO
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void add(TileDefinition tile, Position pos, Rotation rot) {
        Map<Position, Tuple2<TileDefinition, Rotation>> placedTiles = game.getState().getPlacedTiles();
        assert !placedTiles.containsKey(pos);
        game.replaceState(state -> state.setPlacedTiles(
            placedTiles.put(
                pos,
                new Tuple2<>(tile, rot)
            )
        ));
        java.util.Map<FeaturePointer, Feature> fpUpdate = new java.util.HashMap<>();
        Stream.ofAll(tile.getInitialFeatures().values())
            .map(f -> f.placeOnBoard(pos, rot))
            .forEach(feature -> {
                if (feature instanceof MultiTileFeature) {
                    Stream<FeaturePointer> adjacent = feature.getPlaces().get().getAdjacent(feature.getClass());
                    feature = adjacent.foldLeft((MultiTileFeature) feature, (f, adjFp) -> {
                        Option<Feature> adj = game.getState().getFeatures().get(adjFp);
                        if (adj.isEmpty()) return f;
                        return f.merge((MultiTileFeature) adj.get());
                    });
                }
                for (FeaturePointer fp : feature.getPlaces()) {
                    fpUpdate.put(fp, feature);
                }
            });
        game.replaceState(state ->
            state.setFeatures(HashMap.ofAll(fpUpdate).merge(state.getFeatures()))
        );
    }

//    public void add(Tile tile, Position p, boolean unchecked) {
//        if (!unchecked) {
//            if (tile.isAbbeyTile()) {
//                if (!holes.contains(p)) {
//                    throw new IllegalArgumentException("Abbey must be placed inside hole");
//                }
//            } else {
//                if (!currentAvailMoves.containsKey(p)) {
//                    throw new IllegalArgumentException("Invalid position " + p);
//                }
//                if (!currentAvailMoves.get(p).contains(tile.getRotation())) {
//                    throw new IllegalArgumentException("Incorrect rotation " + tile.getRotation() + " "+ p);
//                }
//            }
//        }
//
//        tiles.put(p, tile);
//        availMovesRemove(p);
//
//        for (Position offset: Position.ADJACENT.values()) {
//            Position next = p.add(offset);
//            if (get(next) == null) {
//                availMovesAdd(next);
//                if (isHole(next)) {
//                    holes.add(next);
//                }
//            }
//        }
//        holes.remove(p);
//        tile.setPosition(p);
//        if (p.x > maxX) maxX = p.x;
//        if (p.x < minX) minX = p.x;
//        if (p.y > maxY) maxY = p.y;
//        if (p.y < minY) minY = p.y;
//    }

//    public void mergeFeatures(Tile tile) {
//        for (Entry<Location, Tile> e : getAdjacentTilesMap(tile.getPosition()).entrySet()) {
//            tile.merge(e.getValue(), e.getKey());
//        }
//    }

//    public void remove(Tile tile) {
//        Position pos = tile.getPosition();
//        assert pos != null;
//        tiles.remove(pos);
//        tile.setPosition(null);
//        availMovesAdd(pos);
//        if (isHole(pos)) holes.add(pos);
//        for (Position offset: Position.ADJACENT.values()) {
//            Position next = pos.add(offset);
//            holes.remove(next);
//            if (getAdjacentCount(next) == 0) {
//                availMoves.remove(next);
//            }
//        }
//    }

//    public void unmergeFeatures(Tile tile) {
//        assert tile.getPosition() != null;
//        for (Entry<Location, Tile> e : getAdjacentTilesMap(tile.getPosition()).entrySet()) {
//            tile.unmerge(e.getValue(), e.getKey());
//        }
//    }

//    public void discardTile(Tile tile) {
//        discardedTiles.add(tile);
//        game.post(new TileEvent(TileEvent.DISCARD, null, tile, null));
//    }


//    public List<Tile> getDiscardedTiles() {
//        return discardedTiles;
//    }

//    private boolean isHole(Position p) {
//        for (Position offset: Position.ADJACENT.values()) {
//            Position next = p.add(offset);
//            if (get(next) == null) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private int getAdjacentCount(Position p) {
//        int count = 0;
//        for (Position offset: Position.ADJACENT.values()) {
//            Position next = p.add(offset);
//            if (get(next) != null) {
//                count++;
//            }
//        }
//        return count;
//    }


    public Tile get(int x, int y) {
        return get(new Position(x, y));
    }

    public Tile get(Position pos) {
        Option<Tile> o = tiles.get(pos);
        if (o == null) {
            Tuple2<TileDefinition, Rotation> t = getPlacedTile(pos);
            o = Option.of(t == null ? null : new Tile(game, pos));
            tiles.put(pos, o);
        }
        return o.get();
    }

    public Feature get(FeaturePointer fp) {
        return game.getState().getFeatures().get(fp).getOrNull();
    }

    //TODO maybe get all feature will be enough in new arch
//    public Collection<Tile> getAllTiles() {
//
//    }

    /*
     * Check if placement is legal against orthonogal neigbours. */
//    public boolean isPlacementAllowed(Tile tile, Position p) {
//        for (Entry<Location, Tile> e : getAdjacentTilesMap(p).entrySet()) {
//            if (!tile.check(e.getValue(), e.getKey(), this)) {
//                return false;
//            }
//        }
//        return true;
//    }

    private Rectangle computeBounds(GameState state) {
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;
        for (Position pos : state.getPlacedTiles().keySet()) {
            if (minX > pos.x) minX = pos.x;
            if (maxX < pos.x) maxX = pos.x;
            if (minY > pos.y) minY = pos.y;
            if (maxY < pos.y) maxY = pos.y;
        };
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public int getMaxX() {
        return bounds.get().width + bounds.get().x;
    }

    public int getMinX() {
        return bounds.get().x;
    }

    public int getMaxY() {
        return bounds.get().height + bounds.get().y;
    }

    public int getMinY() {
        return bounds.get().y;
    }

    public Stream<Tuple2<Location, Tile>> getAdjacentTilesMap(Position pos) {
        return Stream.ofAll(Position.ADJACENT)
            .map(t -> t.map2(offset -> get(pos.add(offset))))
            .filter(t -> t._2 != null);
    }

    public Stream<Tile> getAdjacentAndDiagonalTiles(Position pos) {
        return Stream.ofAll(Position.ADJACENT_AND_DIAGONAL.values())
            .map(pos::add)
            .map(this::get)
            .filter(Predicates.isNotNull());
    }

    public int getContinuousRowSize(Position start, Location direction) {
        start = start.add(direction);
        int size = 0;
        while (getPlacedTile(start) != null) {
            size++;
            start = start.add(direction);
        }
        return size;
    }

}
