package com.jcloisterzone.board;

import io.vavr.Tuple2;
import io.vavr.collection.Set;

public interface TilePack {

    static final String INACTIVE_GROUP = "inactive";

    int totalSize();
    boolean isEmpty();
    int size();

    Tuple2<TileDefinition, TilePack> drawTile(int index);
    Tuple2<TileDefinition, TilePack> drawTile(String groupId, String tileId);
    Tuple2<TileDefinition, TilePack> drawTile(String tileId);

    /* special Abbey related methods - refactor je to jen kvuli klientovi */
    Tuple2<TileDefinition, TilePack> getAbbeyTile();

    TilePack setGroupState(String groupId, TileGroupState state);
    TileGroupState getGroupState(String groupId);
    Set<String> getGroups();

    int getSizeForEdgePattern(EdgePattern edgePattern);
}
