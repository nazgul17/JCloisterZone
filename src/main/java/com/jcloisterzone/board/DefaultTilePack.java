package com.jcloisterzone.board;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;

public class DefaultTilePack implements TilePack {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Array<TileDefinition>> groups;
    private final Map<String, TileGroupState> groupStates;


    public DefaultTilePack(Map<String, Array<TileDefinition>> groups) {
        this.groups = groups;
        this.groupStates = groups.map((groupId, tiles ) -> {
            return new Tuple2<>(
                groupId,
                INACTIVE_GROUP.equals(groupId) ? TileGroupState.RETIRED : TileGroupState.WAITING
            );
        });
    }

    @Override
    public int totalSize() {
        return Stream.ofAll(groups.values()).map(tiles -> tiles.length()).sum().intValue();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return Stream.ofAll(groups)
            .filter(t -> groupStates.get(t._1).get() == TileGroupState.ACTIVE)
            .map(t -> t._2.length())
            .sum().intValue();
    }

    @Override
    public Tile drawTile(int index) {
        for (Entry<String,TileGroup> entry: groups.entrySet()) {
            TileGroup group = entry.getValue();
            if (group.state != TileGroupState.ACTIVE) continue;
            ArrayList<Tile> tiles = group.tiles;
            if (index < tiles.size()) {
                Tile currentTile = tiles.remove(index);
                decreaseSideMaskCounter(currentTile, entry.getKey());
                return currentTile;
            } else {
                index -= tiles.size();
            }
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    private void increaseSideMaskCounter(Tile tile, String groupId) {
        if (!INACTIVE_GROUP.equals(groupId) && tile.getPosition() == null) {
            Integer countForSideMask = edgePatterns.get(tile.getEdgePattern());
            if (countForSideMask == null) {
                edgePatterns.put(tile.getEdgePattern(), 1);
            } else {
                edgePatterns.put(tile.getEdgePattern(), countForSideMask + 1);
            }
        }
    }

    private void decreaseSideMaskCounter(Tile tile, String groupId) {
        if (tile == null || groupId.equals(INACTIVE_GROUP)) return;
        Integer count = edgePatterns.get(tile.getEdgePattern());
        if (count == null) {
            logger.error("Inconsistent edge mask statistics. Cannot decrease: " + tile.getEdgePattern().toString());
            return;
        }
        if (count == 1) {
            edgePatterns.remove(tile.getEdgePattern());
        } else {
            edgePatterns.put(tile.getEdgePattern(), count - 1);
        }
    }

    @Override
    public Tile drawTile(String groupId, String tileId) {
        ArrayList<Tile> tiles = groups.get(groupId).tiles;
        Iterator<Tile> i = tiles.iterator();
        while(i.hasNext()) {
            Tile tile = i.next();
            if (tile.getId().equals(tileId)) {
                i.remove();
                decreaseSideMaskCounter(tile, groupId);
                return tile;
            }
        }
        return null;
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
    public void setGroupState(String groupId, TileGroupState state) {
        //can be called with non-existing group (from expansion etc.)
        TileGroup group = groups.get(groupId);
        if (group != null) {
            group.state = state;
        }
    }

    @Override
    public TileGroupState getGroupState(String groupId) {
        TileGroup group = groups.get(groupId);
        if (group == null) return null;
        return group.state;
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

    @Override
    public int getSizeForEdgePattern(EdgePattern pattern) {
        int size = 0;
        for (EdgePattern filled : pattern.expand()) {
            Integer count = edgePatterns.get(filled);
            size += count == null ? 0 : count;
        }
        return size;
    }

}
