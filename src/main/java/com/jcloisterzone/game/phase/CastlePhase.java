package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.CastleAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.CastleCreated;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.PlacedTile;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;

import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

public class CastlePhase extends Phase {

    public CastlePhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive(CapabilitiesState capabilities) {
        return capabilities.contains(CastleCapability.class);
    }

    private Set<FeaturePointer> getPlayerOptions(GameState state, Player player) {
        if (state.getPlayers().getPlayerTokenCount(player.getIndex(), Token.CASTLE) == 0) {
            return HashSet.empty();
        }

        PlacedTile placedTile = state.getLastPlaced();
        Tile currentTile = state.getBoard().get(placedTile.getPosition());
        Position pos = currentTile.getPosition();
        return currentTile.getFeatures()
            .filter(t -> t._2 instanceof City)
            .filter(t -> ((City) t._2).isCastleBase())
            .filter(t -> t._2.getPlaces().size() == 2)
            .filter(t -> {
                List<Follower> followers = t._2.getFollowers(state).toList();
                if (followers.size() != 1) return false;
                return followers.get().getPlayer().equals(player);
            })
            .map(t -> new FeaturePointer(pos, t._1))
            .toSet();
    }

    private void prepareActions(GameState state, Player continueWith) {
        Player turnPlayer = state.getTurnPlayer();
        Player player = continueWith;

        do {
            Set<FeaturePointer> options = getPlayerOptions(state, player);
            if (!options.isEmpty()) {
                CastleAction action = new CastleAction(options);
                ActionsState as = new ActionsState(player, action, true);
                promote(state.setPlayerActions(as));
                return;
            }

            player = player.getNextPlayer(state);
        } while (!player.equals(turnPlayer));

        //no castle action
        next(state);
    }

    @Override
    public void enter(GameState state) {
        prepareActions(state, state.getTurnPlayer());
    }

    @WsSubscribe
    public void handlePlaceTokenMessage(PlaceTokenMessage msg) {
        if (msg.getToken() != Token.CASTLE) {
            throw new IllegalArgumentException();
        }
        GameState state = game.getState();
        Player player = state.getActivePlayer();
        City city = (City) state.getBoard().get(msg.getPointer());
        Castle castle = new Castle(city.getPlaces());

        Map<FeaturePointer, Feature> update = city.getPlaces().toMap(ptr -> new Tuple2<>(ptr, castle));

        state = state.updatePlayers(ps ->
           ps.addPlayerTokenCount(player.getIndex(), Token.CASTLE, -1)
        );
        state = state.setFeatureMap(update.merge(state.getFeatureMap()));
        state = state.appendEvent(new CastleCreated(
           PlayEventMeta.createWithPlayer(player),
           castle
        ));

        Player nextPlayer = player.getNextPlayer(state);
        state = clearActions(state);
        if (nextPlayer.equals(state.getTurnPlayer())) {
            next(state);
        } else {
            //it is possible to deploy castle by another player
            prepareActions(state, nextPlayer);
        }
    }
}
