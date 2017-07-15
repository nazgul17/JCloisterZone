package com.jcloisterzone.board;

import java.awt.Rectangle;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.GameState;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.control.Option;


public class Board {

    private final GameState state;
    private final java.util.Map<Position, Option<Tile>> tiles = new java.util.HashMap<>();

    private Rectangle bounds;

//	protected Set<TunnelEnd> tunnels = new HashSet<>();
//	protected Map<Integer, TunnelEnd> openTunnels = new HashMap<>(); //tunnel with open one side


    public Board(GameState state) {
       this.state = state;
    }

    private Tuple2<TileDefinition, Rotation> getPlacedTile(Position pos) {
        return state.getPlacedTiles().get(pos).getOrNull();
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
        Map<Position, Tuple2<TileDefinition, Rotation>> placedTiles = state.getPlacedTiles();

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
            return true;
            //return game.isTilePlacementAllowed(tile, t._1);
            // TODO IMMUTABLE - call it when capabilities are moved into state
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


    public Set<Feature> getAllFeatures() {
        return HashSet.ofAll(state.getFeatures().values());
    }

    public Stream<Scoreable> getOccupiedScoreables() {
        return Stream.ofAll(getAllFeatures())
            .filter(Predicates.instanceOf(Scoreable.class))
            .filter(f -> f.isOccupied(state))
            .map(f -> (Scoreable) f);
    }

    public Option<Feature> getFeaturePartOf(FeaturePointer fp) {
        return state.getFeatures()
            .find(t -> fp.isPartOf(t._1))
            .map(t -> t._2);
    }

    public Tile get(int x, int y) {
        return get(new Position(x, y));
    }

    public Tile get(Position pos) {
        Option<Tile> o = tiles.get(pos);
        if (o == null) {
            Tuple2<TileDefinition, Rotation> t = getPlacedTile(pos);
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
        return state.getFeatures().get(fp).getOrNull();
    }

    public Stream<Tile> getPlacedTiles() {
        return Stream.ofAll(state.getPlacedTiles()).map(t -> get(t._1));
    }


    private Rectangle computeBounds() {
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

    public Rectangle getBounds() {
        if (bounds == null) {
            bounds = computeBounds();
        }
        return bounds;
    }

    public int getMaxX() {
        return getBounds().width + getBounds().x;
    }

    public int getMinX() {
        return getBounds().x;
    }

    public int getMaxY() {
        return getBounds().height + getBounds().y;
    }

    public int getMinY() {
        return getBounds().y;
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
