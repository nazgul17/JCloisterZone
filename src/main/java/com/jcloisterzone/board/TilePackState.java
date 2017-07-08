package com.jcloisterzone.board;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;

public class TilePackState {

    static final String INACTIVE_GROUP = "inactive";

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Array<TileDefinition>> groups;
    private final Map<String, TileGroupState> groupStates;

    public TilePackState(Map<String, Array<TileDefinition>> groups) {
        this(groups, groups.map((groupId, tiles ) -> {
            return new Tuple2<>(
                groupId,
                INACTIVE_GROUP.equals(groupId) ? TileGroupState.RETIRED : TileGroupState.WAITING
            );
        }));
    }

    public TilePackState(Map<String, Array<TileDefinition>> groups, Map<String, TileGroupState> groupStates) {
        this.groups = groups;
        this.groupStates = groupStates;
    }

    private Stream<Array<TileDefinition>> getActiveGroups() {
        return Stream.ofAll(groups)
            .filter(t -> getGroupState(t._1) == TileGroupState.ACTIVE)
            .map(t -> t._2);
    }

    private Stream<TileDefinition> getActiveTiles() {
        return getActiveGroups().flatMap(t -> t);
    }


    public int totalSize() {
        return Stream.ofAll(groups.values())
            .map(tiles -> tiles.length())
            .sum().intValue();
    }


    public boolean isEmpty() {
        return size() == 0;
    }


    public int size() {
        return getActiveGroups()
            .map(tiles -> tiles.length())
            .sum().intValue();
    }

    private TilePackState replaceGroup(String groupId, Array<TileDefinition> tiles) {
        if (tiles.isEmpty()) {
            return new TilePackState(
                groups.remove(groupId),
                groupStates.remove(groupId)
            );
        } else {
            return new TilePackState(
                groups.put(groupId, tiles),
                groupStates
            );
        }
    }


    public Tuple2<TileDefinition, TilePackState> drawTile(int index) {
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



    public Tuple2<TileDefinition, TilePackState> drawTile(String groupId, String tileId) {
        Predicate<TileDefinition> matchesId = t -> t.getId().equals(tileId);
        Array<TileDefinition> tiles = groups.get(groupId)
            .getOrElseThrow(() -> new IllegalArgumentException());
        TileDefinition tile = tiles.find(matchesId)
            .getOrElseThrow(() -> new IllegalArgumentException());
        TilePackState pack = replaceGroup(groupId, tiles.removeFirst(matchesId));
        return new Tuple2<>(tile, pack);
    }


    public Tuple2<TileDefinition, TilePackState> drawTile(String tileId) {
        for (String groupId: getGroups()) {
            try {
                return drawTile(groupId, tileId);
            } catch (IllegalArgumentException e) {
                //pass
            }
        }
        throw new IllegalArgumentException("Tile pack does not contain " + tileId);
    }

    public TilePackState setGroupState(String groupId, TileGroupState state) {
        //can be called with non-existing group (from expansion etc.)
        return new TilePackState(
            groups,
            groupStates.put(groupId, state)
        );
    }

    public TileGroupState getGroupState(String groupId) {
        return groupStates.get(groupId).getOrNull();
    }

    public Set<String> getGroups() {
        return groups.keySet();
    }

    public int getSizeForEdgePattern(EdgePattern edgePattern) {
        return getActiveTiles()
            .filter(tile -> edgePattern.isMatchingAnyRotation(tile.getEdgePattern()))
            .size();
    }

    /* special Abbey related methods - TODO refactor it is here only for client */
    /*@Override
    public Tile getAbbeyTile() {
        for (Tile tile : groups.get(INACTIVE_GROUP).tiles) {
            if (tile.getId().equals(Tile.ABBEY_TILE_ID)) {
                return tile;
            }
        }
        return null;
    }*/
}
