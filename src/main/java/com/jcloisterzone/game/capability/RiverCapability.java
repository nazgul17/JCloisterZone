package com.jcloisterzone.game.capability;

import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.feature.River;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.List;


public class RiverCapability extends Capability<Void> {

//    private static final String R1_LAKE_ID = "R1.I.e";
//    private static final String R2_LAKE_ID = "R2.I.v";
//    private static final String R2_FORK_ID = "R2.III";

//    private static List<String> STREAM_IDS =  Arrays.asList("R1.I.s", "R2.I.s", "GQ.RFI");

    @Override
    public GameState onStartGame(GameState state) {
        state = state.mapTilePack(pack -> {
            pack = pack.deactivateGroup("default");
            //pack = pack.activateGroup("river-fork");
            pack = pack.activateGroup("river");
//            //pack = pack.deactivateGroup("default");
//            if (pack.getGroupSize("river-fork") > 0) {
//                pack = pack.setGroupState("river-fork", TileGroupState.ACTIVE);
//            } else {
//                pack = pack.setGroupState("river", TileGroupState.ACTIVE);
//            }
            return pack;
        });
        return state;
    }

    @Override
    public GameState onTurnPartCleanUp(GameState state) {
//        return state.mapTilePack(pack -> {
//            if (pack.getGroupState("river-fork") == TileGroupState.ACTIVE &&
//                    pack.getGroupSize("river-fork") == 0) {
//                pack = pack.setGroupState("river", TileGroupState.ACTIVE);
//            } else {
//                //...
//            }
//            return pack;
//        });
        return state;
    }

    private boolean isConnectedToPlacedRiver(GameState state, Position pos, Location side) {
        Position adjPos = pos.add(side);
        return state.getPlacedTiles().containsKey(adjPos);
    }

    private boolean isContinuationFree(GameState state, Position pos, Location side) {
        Position adjPos = pos.add(side);
        Position adjPos2 = adjPos.add(side);
        List<Position> reservedTiles = List.of(
            adjPos.add(side.prev()),
            adjPos.add(side.next()),
            adjPos2,
            adjPos2.add(side.prev()),
            adjPos2.add(side.next())
        );
        return reservedTiles.find(p -> state.getPlacedTiles().containsKey(p)).isEmpty();
    }

    @Override
    public boolean isTilePlacementAllowed(GameState state, TileDefinition tile, TilePlacement placement) {
        Position pos = placement.getPosition();
        Rotation rot = placement.getRotation();
        List<Location> sides = tile.getInitialFeatures()
            .filterValues(Predicates.instanceOf(River.class))
            .map(Tuple2::_1)
            .map(l -> l.rotateCW(rot))
            .get()
            .splitToSides();

        List<Location> openSides = sides.filter(side -> !isConnectedToPlacedRiver(state, pos, side));
        if (sides.size() == openSides.size()) {
            return false;
        }

        if (openSides.find(side -> !isContinuationFree(state, pos, side)).isDefined()) {
            return false;
        }
        return true;
    }

//
//    @Override
//    public void begin() {
//        getTilePack().setGroupState("default", TileGroupState.WAITING);
//        getTilePack().setGroupState("river-start", TileGroupState.ACTIVE);
//        if (!game.hasExpansion(Expansion.RIVER_II)) {
//            getTilePack().setGroupState("river", TileGroupState.ACTIVE);
//        }
//    }
//
//    public void activateNonRiverTiles() {
//        getTilePack().setGroupState("default", TileGroupState.ACTIVE);
//        getTilePack().setGroupState("river", TileGroupState.RETIRED);
//        Tile lake = getTilePack().drawTile(TilePack.INACTIVE_GROUP, getLakeId());
//        getBoard().refreshAvailablePlacements(lake);
//        if (!getBoard().getAvailablePlacements().isEmpty()) {
//            Entry<Position, Set<Rotation>> entry = getBoard().getAvailablePlacements().entrySet().iterator().next();
//            lake.setRotation(entry.getValue().iterator().next());
//            getBoard().add(lake, entry.getKey());
//            getBoard().mergeFeatures(lake);
//            game.post(new TileEvent(TileEvent.PLACEMENT, null, lake, lake.getPosition()));
//        }
//    }
//


//    @Override
//    public void turnPartCleanUp() {
//        if (getCurrentTile().getRiver() == null) return;
//        if (getTilePack().isEmpty()) {
//            if (getTilePack().getGroupState("river") == TileGroupState.ACTIVE) {
//                activateNonRiverTiles();
//            } else {
//                getTilePack().setGroupState("river-start", TileGroupState.RETIRED);
//                getTilePack().setGroupState("river", TileGroupState.ACTIVE);
//            }
//        }
//    }
//

}