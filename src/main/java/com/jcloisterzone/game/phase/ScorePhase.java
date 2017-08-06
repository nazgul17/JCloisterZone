package com.jcloisterzone.game.phase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.ConfirmAction;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.config.Config.ConfirmConfig;
import com.jcloisterzone.event.play.MeepleDeployed;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.capability.GoldminesCapability;
import com.jcloisterzone.game.capability.MageAndWitchCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.capability.WagonCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.ScoreFeature;
import com.jcloisterzone.reducers.UndeployMeeples;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.CommitMessage;

import io.vavr.Tuple2;
import io.vavr.control.Option;


//TODO split into CommitActionPhase a ScorePhase
public class ScorePhase extends ServerAwarePhase {

    private Set<Completable> alreadyScored = new HashSet<>();

//    private final BarnCapability barnCap;
//    private final CastleCapability castleCap;
//    private final TunnelCapability tunnelCap;
//    private final WagonCapability wagonCap;
//    private final MageAndWitchCapability mageWitchCap;
//    private final GoldminesCapability gldCap;

    public ScorePhase(Game game, GameController gc) {
        super(game, gc);
//        barnCap = game.getCapability(BarnCapability.class);
//        tunnelCap = game.getCapability(TunnelCapability.class);
//        castleCap = game.getCapability(CastleCapability.class);
//        wagonCap = game.getCapability(WagonCapability.class);
//        mageWitchCap = game.getCapability(MageAndWitchCapability.class);
//        gldCap = game.getCapability(GoldminesCapability.class);
    }

    private GameState scoreCompletedOnTile(GameState state, Tile tile) {
        for (Tuple2<Location, Completable> t : tile.getCompletableFeatures()) {
            state = scoreCompleted(state, t._2, tile);
        }
        return state;
    }

    private GameState scoreCompletedNearAbbey(GameState state, Position pos) {
        for (Tuple2<Location, Tile> t : state.getBoard().getAdjacentTilesMap(pos)) {
            Tile tile = t._2;
            Feature feature = tile.getFeaturePartOf(t._1.rev());
            if (feature instanceof Completable) {
                state = scoreCompleted(state, (Completable) feature, null);
            }
        }
        return state;
    }

    private GameState scoreFollowersOnBarnFarm(GameState state, Farm farm) {
        // IMMUTABLE TODO
//        FarmScoreContext ctx = farm.getScoreContext();
//        ctx.setCityCache(cityCache);
//        farm.walk(ctx);
//
//        boolean hasBarn = false;
//        for (Meeple m : ctx.getSpecialMeeples()) {
//            if (m instanceof Barn) {
//                hasBarn = true;
//                break;
//            }
//        }
//        if (hasBarn) {
//            for (Player p : ctx.getMajorOwners()) {
//                int points = ctx.getPointsWhenBarnIsConnected(p);
//                game.scoreFeature(points, ctx, p);
//            }
//            for (Meeple m : ctx.getMeeples()) {
//                if (!(m instanceof Barn)) {
//                    undeloyMeeple(m);
//                }
//            }
//        }
        return state;
    }

    @Override
    public void enter(GameState state) {
        Player player = state.getTurnPlayer();
        if (isLocalPlayer(player)) {
            boolean needsConfirm = false;
            // IMMUTABLE TODO
            PlayEvent last = state.getEvents().last();
            if (last instanceof MeepleDeployed) {
                ConfirmConfig cfg =  getConfig().getConfirm();
                MeepleDeployed ev = (MeepleDeployed) last;
                if (cfg.getAny_deployment()) {
                    needsConfirm = true;
                } else if (cfg.getFarm_deployment() && ev.getLocation().isFarmLocation()) {
                    needsConfirm = true;
                } else if (cfg.getOn_tower_deployment() && ev.getLocation() == Location.TOWER) {
                    needsConfirm = true;
                }
            }
            if (!needsConfirm) {
                getConnection().send(new CommitMessage(game.getGameId()));
                promote(state);
                return;
            }
        }

        //if player is not active, always trigger event and wait for remote CommitMessage
        state = state.setPlayerActions(
            new ActionsState(player, new ConfirmAction(), false)
        );
        promote(state);
    }

    @WsSubscribe
    public void handleCommit(CommitMessage msg) {
        game.clearUndo();
        game.updateRandomSeed(msg.getCurrentTime());

        GameState state = game.getState();
        Board board = state.getBoard(); //can keep ref because only points are changed
        Tile tile = board.getLastPlaced();
        Position pos = tile.getPosition();
        //TODO separate event here ??? and move this code to abbey and mayor game
        //TODO immutable
//        if (state.getCapabilities().contains(BarnCapability.class)) {
//            Map<City, CityScoreContext> cityCache = new HashMap<>();
//            for (Tuple2<Location, Feature> t : tile.getFeatures()) {
//                if (t._2 instanceof Farm) {
//                    state = scoreFollowersOnBarnFarm(state, (Farm) t._2, cityCache);
//                }
//            }
//        }

        state = scoreCompletedOnTile(state, tile);
        if (tile.isAbbeyTile()) {
            state = scoreCompletedNearAbbey(state, pos);
        }

        if (state.getCapabilities().contains(TunnelCapability.class)) {
//            Road r = tunnelCap.getPlacedTunnel();
//            if (r != null) {
//                state = scoreCompleted(state, r, tile);
//            }
        }

        for (Tile neighbour : board.getAdjacentAndDiagonalTiles(pos)) {
            Cloister cloister = neighbour.getCloister();
            if (cloister != null) {
                state = scoreCompleted(state, cloister, null);
            }
        }

        if (state.getCapabilities().contains(CastleCapability.class)) {
            // IMMUTABLE TODO
//            for (Entry<Castle, Integer> entry : castleCap.getCastleScore().entrySet()) {
//                scoreCastle(entry.getKey(), entry.getValue());
//            }
        }

        if (state.getCapabilities().contains(GoldminesCapability.class)) {
            //gldCap.awardGoldPieces();
        }

        alreadyScored.clear();
        next(state);
    }

//    private void scoreCastle(Castle castle, int points) {
//        List<Meeple> meeples = castle.getMeeples();
//        if (meeples.isEmpty()) meeples = castle.getSecondFeature().getMeeples();
//        Meeple m = meeples.get(0); //all meeples must share same owner
//        m.getPlayer().addPoints(points, PointCategory.CASTLE);
//        if (gldCap != null) {
//            gldCap.castleCompleted(castle, m.getPlayer());
//        }
//        game.post(new ScoreEvent(m.getFeature(), points, PointCategory.CASTLE, m));
//        undeloyMeeple(m);
//    }

    private GameState scoreCompleted(GameState state, Completable completable, Tile triggerBuilderForPlaced) {
        if (triggerBuilderForPlaced != null && state.getCapabilities().contains(BuilderCapability.class)) {
            Player player = state.getTurnPlayer();
            GameState _state = state;
            Option<Meeple> builder = completable
                .getMeeples(state)
                .find(m -> {
                    return m instanceof Builder
                        && m.getPlayer().equals(player)
                        && !m.getPosition(_state).equals(triggerBuilderForPlaced.getPosition());
                });
            if (!builder.isEmpty()) {
                state = state.getCapabilities().get(BuilderCapability.class).useBuilder(state);
            }
        }

        if (completable.isCompleted(state) && !alreadyScored.contains(completable)) {
            alreadyScored.add(completable);

            for (Capability<?> cap : state.getCapabilities().toSeq()) {
                state = cap.onCompleted(state, completable);
            }

            state = (new ScoreFeature(completable)).apply(state);
            state = (new UndeployMeeples(completable)).apply(state);

            //IMMUTABLE TODO
//   notify scored wagon
//          if (m instanceof Wagon && wagonCap != null) {
//          wagonCap.wagonScored((Wagon) m, feature);
//      	}
            //game.post(new FeatureCompletedEvent(getActivePlayer(), completable));
        }

        return state;
    }

}
