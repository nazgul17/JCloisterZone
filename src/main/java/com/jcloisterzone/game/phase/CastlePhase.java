package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.CastleAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Token;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.PlaceTokenMessage;

import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Set;

public class CastlePhase extends Phase {

    public CastlePhase(Game game) {
        super(game);
    }

    @Override
    public boolean isActive(CapabilitiesState capabilities) {
        return capabilities.contains(CastleCapability.class);
    }

//    @Override
//    public Player getActivePlayer() {
//        Player p = castleCap.getCastlePlayer();
//        return p == null ? game.getTurnPlayer() : p;
//    }

    @Override
    public void enter(GameState state) {
        Player player = state.getTurnPlayer();
        if (state.getPlayers().getPlayerTokenCount(player.getIndex(), Token.CASTLE) == 0) {
            next(state);
            return;
        }

        Tile currentTile = state.getBoard().getLastPlaced();
        Position pos = currentTile.getPosition();
        Set<FeaturePointer> options = currentTile.getFeatures()
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

        if (options.isEmpty()) {
            next(state);
            return;
        }

        CastleAction action = new CastleAction(options);
        ActionsState as = new ActionsState(player, action, true);
        promote(state.setPlayerActions(as));
    }

    @WsSubscribe
    public void handlePlaceTokenMessage(PlaceTokenMessage msg) {
        GameState state = game.getState();
        //TODO
        // - replace city with castle on board
//    	Player owner = castleCap.getCastlePlayer();
//        castleCap.decreaseCastles(owner);
//        castleCap.convertCityToCastle(pos, loc);
//        prepareCastleAction(); //it is possible to deploy castle by another player
    }
}
