package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;


/**
 * @model Set<FeaturePointer> : placed bridges
 */
public class BridgeCapability extends Capability<Set<FeaturePointer>> {

    @Override
    public GameState onStartGame(GameState state) {
        int tokens = state.getPlayers().length() < 5 ? 3 : 2;
        state = state.updatePlayers(ps -> {
            for (Player p : ps.getPlayers()) {
                ps = ps.addPlayerTokenCount(p.getIndex(), Token.BRIDGE, tokens);
            }
            return ps;
        });
        state = setModel(state, HashSet.empty());
        return state;
    }


//    @Override
//    public void turnPartCleanUp() {
//        bridgeUsed = false;
//    }

//    @Override
//    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
//        if (!bridgeUsed && getPlayerBridges(game.getPhase().getActivePlayer()) > 0) {
//            BridgeAction action = prepareBridgeAction();
//            if (action != null) {
//                actions.add(action);
//            }
//        }
//    }

//    public BridgeAction prepareMandatoryBridgeAction() {
//        Tile tile = game.getCurrentTile();
//        for (Entry<Location, Tile> entry : getBoard().getAdjacentTilesMap(tile.getPosition()).entrySet()) {
//            Tile adjacent = entry.getValue();
//            Location rel = entry.getKey();
//
//            EdgeType adjacentSide = adjacent.getEdge(rel.rev());
//            EdgeType tileSide = tile.getEdge(rel);
//            if (tileSide != adjacentSide) {
//                Location bridgeLoc = getBridgeLocationForAdjacent(rel);
//                BridgeAction action = prepareTileBridgeAction(tile, null, bridgeLoc);
//                if (action != null) return action;
//                return prepareTileBridgeAction(adjacent, null, bridgeLoc);
//            }
//        }
//        throw new IllegalStateException();
//    }
//
//    private Location getBridgeLocationForAdjacent(Location rel) {
//        if (rel == Location.N || rel == Location.S) {
//            return Location.NS;
//        } else {
//            return Location.WE;
//        }
//    }
//
//    private BridgeAction prepareBridgeAction() {
//        BridgeAction action = null;
//        Tile tile = game.getCurrentTile();
//        action = prepareTileBridgeAction(tile, action, Location.NS);
//        action = prepareTileBridgeAction(tile, action, Location.WE);
//        for (Entry<Location, Tile> entry : getBoard().getAdjacentTilesMap(tile.getPosition()).entrySet()) {
//            Tile adjacent = entry.getValue();
//            Location rel = entry.getKey();
//            action = prepareTileBridgeAction(adjacent, action, getBridgeLocationForAdjacent(rel));
//        }
//        return action;
//    }
//
//    private BridgeAction prepareTileBridgeAction(Tile tile, BridgeAction action, Location bridgeLoc) {
//        if (isBridgePlacementAllowed(tile, tile.getPosition(), bridgeLoc)) {
//            if (action == null) action = new BridgeAction();
//            action.add(new FeaturePointer(tile.getPosition(), bridgeLoc));
//        }
//        return action;
//    }
//
//    private boolean isBridgePlacementAllowed(Tile tile, Position p, Location bridgeLoc) {
//        if (!tile.isBridgeAllowed(bridgeLoc)) return false;
//        for (Entry<Location, Tile> e : getBoard().getAdjacentTilesMap(p).entrySet()) {
//            Location rel = e.getKey();
//            if (rel.intersect(bridgeLoc) != null) {
//                Tile adjacent = e.getValue();
//                EdgeType adjacentSide = adjacent.getEdge(rel.rev());
//                if (adjacentSide != EdgeType.ROAD) return false;
//            }
//        }
//        return true;
//    }
//
//    public boolean isTilePlacementWithBridgePossible(Tile tile, Position p) {
//        if (getPlayerBridges(game.getActivePlayer()) > 0) {
//            if (isTilePlacementWithBridgeAllowed(tile, p, Location.NS)) return true;
//            if (isTilePlacementWithBridgeAllowed(tile, p, Location.WE)) return true;
//            if (isTilePlacementWithOneAdjacentBridgeAllowed(tile, p)) return true;
//        }
//        return false;
//    }
//
//    private boolean isTilePlacementWithBridgeAllowed(Tile tile, Position p, Location bridgeLoc) {
//        if (!tile.isBridgeAllowed(bridgeLoc)) return false;
//
//        for (Entry<Location, Tile> e : getBoard().getAdjacentTilesMap(p).entrySet()) {
//            Tile adjacent = e.getValue();
//            Location rel = e.getKey();
//
//            EdgeType adjacentSide = adjacent.getEdge(rel.rev());
//            EdgeType tileSide = tile.getEdge(rel);
//            if (rel.intersect(bridgeLoc) != null) {
//                if (adjacentSide != EdgeType.ROAD) return false;
//            } else {
//                if (adjacentSide != tileSide) return false;
//            }
//        }
//        return true;
//    }
//
//    private boolean isTilePlacementWithOneAdjacentBridgeAllowed(Tile tile, Position p) {
//        boolean bridgeUsed = false;
//        for (Entry<Location, Tile> e : getBoard().getAdjacentTilesMap(p).entrySet()) {
//            Tile adjacent = e.getValue();
//            Location rel = e.getKey();
//
//            EdgeType tileSide = tile.getEdge(rel);
//            EdgeType adjacentSide = adjacent.getEdge(rel.rev());
//
//            if (tileSide != adjacentSide) {
//                if (bridgeUsed) return false;
//                if (tileSide != EdgeType.ROAD) return false;
//
//                Location bridgeLoc = getBridgeLocationForAdjacent(rel);
//                if (!isBridgePlacementAllowed(adjacent, adjacent.getPosition(), bridgeLoc)) return false;
//                bridgeUsed = true;
//            }
//        }
//        return bridgeUsed; //ok if exactly one bridge is used
//    }
//
//    public void deployBridge(Position pos, Location loc, boolean forced) {
//        Tile tile = getBoard().getPlayer(pos);
//        if (!tile.isBridgeAllowed(loc)) {
//            throw new IllegalArgumentException("Cannot deploy " + loc + " bridge on " + pos);
//        }
//        bridgeUsed = true;
//        tile.placeBridge(loc);
//        BridgeEvent ev = new BridgeEvent(BridgeEvent.DEPLOY, game.getActivePlayer(), pos, loc);
//        ev.setForced(forced);
//        game.post(ev);
//    }



}
