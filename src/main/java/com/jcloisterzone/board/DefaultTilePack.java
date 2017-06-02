package com.jcloisterzone.board;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class DefaultTilePack implements TilePack {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Array<TileDefinition>> groups;
    private final Map<String, TileGroupState> groupStates;

    public DefaultTilePack(Map<String, Array<TileDefinition>> groups) {
        this(groups, groups.map((groupId, tiles ) -> {
            return new Tuple2<>(
                groupId,
                INACTIVE_GROUP.equals(groupId) ? TileGroupState.RETIRED : TileGroupState.WAITING
            );
        }));
    }

    public DefaultTilePack(Map<String, Array<TileDefinition>> groups, Map<String, TileGroupState> groupStates) {
        this.groups = groups;
        this.groupStates = groupStates;
    }

    private Stream<Array<TileDefinition>> getActiveGroups() {
        return Stream.ofAll(groups)
            .filter(t -> getGroupState(t._1) == TileGroupState.ACTIVE)
            .map(t -> t._2);
    }

    @Override
    public int totalSize() {
        return Stream.ofAll(groups.values())
            .map(tiles -> tiles.length())
            .sum().intValue();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return getActiveGroups()
            .map(tiles -> tiles.length())
            .sum().intValue();
    }

    private DefaultTilePack replaceGroup(String groupId, Array<TileDefinition> tiles) {
        if (tiles.isEmpty()) {
            return new DefaultTilePack(
                groups.remove(groupId),
                groupStates.remove(groupId)
            );
        } else {
            return new DefaultTilePack(
                groups.put(groupId, tiles),
                groupStates
            );
        }
    }

    @Override
    public Tuple2<TileDefinition, TilePack> drawTile(int index) {
        for (Tuple2<String, Array<TileDefinition>> t : groups) {
            String groupId = t._1;
            if (getGroupState(groupId) != TileGroupState.ACTIVE) continue;
            Array<TileDefinition> tiles = t._2;
            if (index < tiles.size()) {
                TileDefinition tile = tiles.get(index);
                tiles = tiles.removeAt(index);
                return new Tuple2<>(tile, replaceGroup(groupId, tiles));
            } else {
                index -= tiles.size();
            }
        }
        throw new IllegalArgumentException();
    }


    @Override
    public Tuple2<TileDefinition, TilePack> drawTile(String groupId, String tileId) {
        Predicate<TileDefinition> matchesId = t -> t.getId().equals(tileId);
        Array<TileDefinition> tiles = groups.get(groupId)
            .getOrElseThrow(() -> new IllegalArgumentException());
        TileDefinition tile = tiles.find(matchesId)
            .getOrElseThrow(() -> new IllegalArgumentException());
        DefaultTilePack pack = replaceGroup(groupId, tiles.removeFirst(matchesId));
        return new Tuple2<>(tile, pack);
    }

    @Override
    public Tile drawTile(String tileId) {
        for (String groupId: groups.keySet()) {
            Tile tile = drawTile(groupId, tileId);
            if (tile != null) return tile;
        }
        logger.warn("Tile pack does not contain {}", tileId);
        return null;
    }

    public List<Tile> drawPrePlacedActiveTiles() {
        List<Tile> result = new ArrayList<>();
        for (Entry<String, TileGroup> entry: groups.entrySet()) {
            TileGroup group = entry.getValue();
            Iterator<Tile> i = group.tiles.iterator();
            while(i.hasNext()) {
                Tile tile = i.next();
                if (tile.getPosition() != null) {
                    if (group.state == TileGroupState.ACTIVE) {
                        result.add(tile);
                        i.remove();
                    } else {
                        tile.setPosition(null);
                        increaseSideMaskCounter(tile, entry.getKey());
                    }
                }
            }
        }
        return result;
    }

    public void addTile(Tile tile, String groupId) {
        TileGroup group = groups.get(groupId);
        if (group == null) {
            group = new TileGroup();
            groups.put(groupId, group);
        }
        group.tiles.add(tile);
        increaseSideMaskCounter(tile, groupId);
    }

    @Override
    public DefaultTilePack setGroupState(String groupId, TileGroupState state) {
        //can be called with non-existing group (from expansion etc.)
        return new DefaultTilePack(
            groups,
            groupStates.put(groupId, state)
        );
    }

    @Override
    public TileGroupState getGroupState(String groupId) {
        return groupStates.get(groupId).getOrNull();
    }

    @Override
    public Set<String> getGroups() {
        return groups.keySet();
    }

    /* special Abbey related methods - TODO refactor it is here only for client */
    @Override
    public Tile getAbbeyTile() {
        for (Tile tile : groups.get(INACTIVE_GROUP).tiles) {
            if (tile.getId().equals(Tile.ABBEY_TILE_ID)) {
                return tile;
            }
        }
        return null;
    }
}
