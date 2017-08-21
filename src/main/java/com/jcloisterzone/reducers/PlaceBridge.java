package com.jcloisterzone.reducers;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.BridgePlaced;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Bridge;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;

public class PlaceBridge implements Reducer {

    private final FeaturePointer ptr;
    private boolean silent;

    public PlaceBridge(FeaturePointer ptr, boolean silent) {
        this.ptr = ptr;
        this.silent = silent;
    }

    public PlaceBridge(FeaturePointer ptr) {
        this(ptr, false);
    }

    @Override
    public GameState apply(GameState state) {
        Position bridgePos = ptr.getPosition();
        Location bridgeLoc = ptr.getLocation();

        LinkedHashMap<Position, PlacedTile> placedTiles = state.getPlacedTiles();
        PlacedTile ptile = placedTiles.get(bridgePos).get();
        Rotation tileRotation = ptile.getRotation();
        ptile = ptile.mapTile(t -> t.addBridge(bridgeLoc.rotateCCW(tileRotation)));
        state = state.setPlacedTiles(placedTiles.put(bridgePos, ptile));

        Bridge bridge = new Bridge(bridgeLoc);
        Road bridgeRoad = bridge.placeOnBoard(bridgePos, tileRotation);
        state = state.setFeatureMap(
            state.getFeatureMap().put(ptr, bridgeRoad)
        );
        state = state.mapCapabilityModel(BridgeCapability.class, model -> model.add(ptr));
        if (!silent) {
            state = state.appendEvent(
                new BridgePlaced(PlayEventMeta.createWithActivePlayer(state), ptr)
            );
        }

        return state;
    }

}
