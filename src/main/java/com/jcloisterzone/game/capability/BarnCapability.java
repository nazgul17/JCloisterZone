package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Corner;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.Phantom;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;


public final class BarnCapability extends Capability<Void> {

    @Override
    public List<Special> createPlayerSpecialMeeples(Player player, MeepleIdProvider idProvider) {
        return List.of((Special) new Barn(idProvider.generateId(Pig.class), player));
    }

    @Override
    public GameState onActionPhaseEntered(GameState state) {
        Player player = state.getPlayerActions().getPlayer();

        Barn barn = player.getMeepleFromSupply(state, Barn.class);
        if (barn == null) {
            return state;
        }

        Tile currentTile = state.getBoard().getLastPlaced();
        Position pos = currentTile.getPosition();

        Set<FeaturePointer> options = Stream.of(
            new Position(pos.x - 1, pos.y - 1),
            new Position(pos.x, pos.y - 1),
            pos,
            new Position(pos.x -1, pos.y)
        )
            .map(p -> getCornerFeature(state, p))
            .filter(Predicates.isNotNull())
            .filter(t -> {
                if (state.getBooleanValue(CustomRule.MULTI_BARN_ALLOWED)) {
                    return true;
                }
                return t._2.getSpecialMeeples(state)
                    .find(Predicates.instanceOf(Barn.class))
                    .isEmpty();
            })
            .map(Tuple2::_1)
            .toSet();

        if (options.isEmpty()) {
            return state;
        }

        ActionsState as = state.getPlayerActions();
        as = as.appendAction(new MeepleAction(Barn.class, options));
        return state.setPlayerActions(as);

    }

    private Tuple2<FeaturePointer, Feature> getCornerFeature(GameState state, Position pos) {
        Board board = state.getBoard();
        Tuple2<FeaturePointer, Feature> t =
            board.getFeaturePartOf2(new FeaturePointer(new Position(pos.x + 1, pos.y), Location.SR)).getOrNull();
        if (t == null || !t._1.getLocation().getCorners().contains(Corner.SW)) return null;
        t = board.getFeaturePartOf2(new FeaturePointer(new Position(pos.x + 1, pos.y + 1), Location.WR)).getOrNull();
        if (t == null || !t._1.getLocation().getCorners().contains(Corner.NW)) return null;
        t = board.getFeaturePartOf2(new FeaturePointer(new Position(pos.x, pos.y + 1), Location.NR)).getOrNull();
        if (t == null || !t._1.getLocation().getCorners().contains(Corner.NW)) return null;
        t = board.getFeaturePartOf2(new FeaturePointer(pos, Location.ER)).getOrNull();
        if (t == null || !t._1.getLocation().getCorners().contains(Corner.SE)) return null;
        return t;
    }
}