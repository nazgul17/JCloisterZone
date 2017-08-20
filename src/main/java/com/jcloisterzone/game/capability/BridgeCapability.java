package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.BridgeAction;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.EdgePattern;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.BridgePlaced;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;
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

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        Player player = state.getPlayerActions().getPlayer();

        boolean playerHasBridge = state.getPlayers().getPlayerTokenCount(
            player.getIndex(), Token.BRIDGE) > 0;

        if (!playerHasBridge ||
            state.getCurrentTurnEvents().find(Predicates.instanceOf(BridgePlaced.class)).isDefined()) {
            return state;
        }

        Position pos = state.getLastPlaced().getPosition();
        Set<FeaturePointer> options = HashSet.empty();

        for (Location bridgeLoc : Location.BRIDGES) {
            FeaturePointer ptr = new FeaturePointer(pos, bridgeLoc);
            if (isBridgePlacementAllowed(state, ptr)) {
                options = options.add(ptr);
            }
        }

        if (options.isEmpty()) {
            return state;
        }

        return state.appendAction(new BridgeAction(options));
    }

    public boolean isBridgePlacementAllowed(GameState state, FeaturePointer bridgePtr) {
        Board board = state.getBoard();
        Position pos = bridgePtr.getPosition();
        Location loc = bridgePtr.getLocation();

        // for valid placement there must be adjacent place with empty
        // space on the other side
        boolean adjExists = loc.splitToSides()
                .map(l -> board.get(pos.add(l)))
                .find(Predicates.isNotNull())
                .isDefined();

        if (adjExists) {
            return false;
        }

        // also no bridge must be already placed on adjacent tile
        Set<FeaturePointer> placedBridges = getModel(state);
        if (placedBridges.find(fp -> fp.getPosition().equals(pos)).isDefined()) {
            return false;
        }

        //and bridge must be legal on tile
        Tile tile = board.get(pos);
        return tile.getEdgePattern().isBridgeAllowed(loc);
    }
}
