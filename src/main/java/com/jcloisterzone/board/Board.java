package com.jcloisterzone.board;

import java.awt.Rectangle;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.MultiTileFeature;
import com.jcloisterzone.game.CachedValue;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.control.Option;


public class Board {
    //TODO move tiles inside and make it immutable ?

    private final Game game;

    private transient final java.util.Map<Position, Tile> tiles = new java.util.HashMap<>();

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

    public Stream<TilePlacement> getTilePlacements(TileDefinition tile) {
        return getAvailablePlacements(tile).flatMap(t -> {
            return Stream.of(Rotation.values())
                .filter(r -> t._2.isMatchingExact(tile.getEdgePattern().rotate(r)))
                .map(r -> new TilePlacement(t._1, r));
        });
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

    public Set<Feature> getAllFeatures() {
        return HashSet.ofAll(game.getState().getFeatures().values());
    }

    public Option<Feature> getFeaturePartOf(FeaturePointer fp) {
        return game.getState().getFeatures()
            .find(t -> fp.isPartOf(t._1))
            .map(t -> t._2);
    }


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
        LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles = game.getState().getPlacedTiles();
        assert !placedTiles.containsKey(pos);

        Option<Tuple2<Position, EdgePattern>> patterns = getAvailablePlacements().find(t -> t._1.equals(pos));
        if (patterns.isDefined()) {
            if (!patterns.get()._2.isMatchingExact(tile.getEdgePattern().rotate(rot))) {
                throw new IllegalArgumentException("Invalid rotation " + pos + "," + rot);
            }
        } else {
            if (!placedTiles.isEmpty()) {
                throw new IllegalArgumentException("Invalid position " + pos + "," + rot);
            }
        }

        GameState state = game.getState();

        state = state.setPlacedTiles(
            placedTiles.put(
                pos,
                new Tuple2<>(tile, rot)
            )
        );

        java.util.Map<FeaturePointer, Feature> fpUpdate = new java.util.HashMap<>();
        Stream.ofAll(tile.getInitialFeatures().values())
            .map(f -> f.placeOnBoard(pos, rot))
            .forEach(feature -> {
                if (feature instanceof MultiTileFeature) {
                    Stream<FeaturePointer> adjacent = feature.getPlaces().get().getAdjacent(feature.getClass());
                    feature = adjacent.foldLeft((MultiTileFeature) feature, (f, adjFp) -> {
                        Option<Feature> adj = getFeaturePartOf(adjFp);
                        if (adj.isEmpty()) return f;
                        return f.merge((MultiTileFeature) adj.get());
                    });
                }
                for (FeaturePointer fp : feature.getPlaces()) {
                    fpUpdate.put(fp, feature);
                }
            });
        state = state.setFeatures(HashMap.ofAll(fpUpdate).merge(state.getFeatures()));
        game.replaceState(state); //make one atomic change when everything is validated
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
        Tile tile = tiles.get(pos);
        if (tile == null) {
            Tuple2<TileDefinition, Rotation> t = getPlacedTile(pos);
            if (t != null) {
                tile = new Tile(game, pos);
                tiles.put(pos, tile);
            }
        }
        return tile;
    }

    public Feature get(FeaturePointer fp) {
        return game.getState().getFeatures().get(fp).getOrNull();
    }

    public Stream<Tile> getPlacedTiles() {
        return Stream.ofAll(game.getState().getPlacedTiles()).map(t -> get(t._1));
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
