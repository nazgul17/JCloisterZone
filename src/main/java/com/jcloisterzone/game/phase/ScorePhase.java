package com.jcloisterzone.game.phase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.ConfirmAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.config.Config.ConfirmConfig;
import com.jcloisterzone.event.FeatureCompletedEvent;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.event.RequestConfirmEvent;
import com.jcloisterzone.event.play.MeepleDeployed;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Cloister;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.feature.visitor.score.CityScoreContext;
import com.jcloisterzone.feature.visitor.score.CompletableScoreContext;
import com.jcloisterzone.feature.visitor.score.FarmScoreContext;
import com.jcloisterzone.feature.visitor.score.PositionCollectingScoreContext;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Wagon;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.capability.BarnCapability;
import com.jcloisterzone.game.capability.BuilderCapability;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.capability.GoldminesCapability;
import com.jcloisterzone.game.capability.MageAndWitchCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.capability.WagonCapability;
import com.jcloisterzone.reducers.ScoreFeature;
import com.jcloisterzone.reducers.UndeployMeeples;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.CommitMessage;

import io.vavr.Predicates;
import io.vavr.Tuple2;

public class ScorePhase extends ServerAwarePhase {

    private Set<Completable> alreadyScored = new HashSet<>();

    private final BarnCapability barnCap;
    private final BuilderCapability builderCap;
    private final CastleCapability castleCap;
    private final TunnelCapability tunnelCap;
    private final WagonCapability wagonCap;
    private final MageAndWitchCapability mageWitchCap;
    private final GoldminesCapability gldCap;

    public ScorePhase(Game game, GameController gc) {
        super(game, gc);
        barnCap = game.getCapability(BarnCapability.class);
        builderCap = game.getCapability(BuilderCapability.class);
        tunnelCap = game.getCapability(TunnelCapability.class);
        castleCap = game.getCapability(CastleCapability.class);
        wagonCap = game.getCapability(WagonCapability.class);
        mageWitchCap = game.getCapability(MageAndWitchCapability.class);
        gldCap = game.getCapability(GoldminesCapability.class);
    }

    private void scoreCompletedOnTile(Tile tile) {
        tile.getCompletableFeatures().forEach(t -> {
            scoreCompleted(t._2, true);
        });
    }

    private void scoreCompletedNearAbbey(Position pos) {
        for (Tuple2<Location, Tile> t : getBoard().getAdjacentTilesMap(pos)) {
            Tile tile = t._2;
            Feature feature = tile.getFeaturePartOf(t._1.rev());
            if (feature instanceof Completable) {
                scoreCompleted((Completable) feature, false);
            }
        }
    }

    private void scoreFollowersOnBarnFarm(Farm farm, Map<City, CityScoreContext> cityCache) {
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
    }

    @Override
    public void enter() {
        GameState state = game.getState();
        Player player = state.getActivePlayer();
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
            if (needsConfirm) {
                game.replaceState(state.setPlayerAcrions(
                    new ActionsState(player, new ConfirmAction(), false)
                ));
            } else {
                getConnection().send(new CommitMessage(game.getGameId()));
            }
        } else {
            //if player is not active, always trigger event and wait for remote CommitMessage
            game.replaceState(state.setPlayerAcrions(
                new ActionsState(player, new ConfirmAction(), false)
            ));
        }
    }

    @WsSubscribe
    public void handleCommit(CommitMessage msg) {
        game.updateRandomSeed(msg.getCurrentTime());

        Tile tile = game.getCurrentTile();
        Position pos = tile.getPosition();
        //TODO separate event here ??? and move this code to abbey and mayor game
        if (barnCap != null) {
            Map<City, CityScoreContext> cityCache = new HashMap<>();
            for (Tuple2<Location, Feature> t : tile.getFeatures()) {
                if (t._2 instanceof Farm) {
                    scoreFollowersOnBarnFarm((Farm) t._2, cityCache);
                }
            }
        }

        scoreCompletedOnTile(tile);
        if (tile.isAbbeyTile()) {
            scoreCompletedNearAbbey(pos);
        }

        if (tunnelCap != null) {
            Road r = tunnelCap.getPlacedTunnel();
            if (r != null) {
                scoreCompleted(r, true);
            }
        }

        for (Tile neighbour : getBoard().getAdjacentAndDiagonalTiles(pos)) {
            Cloister cloister = neighbour.getCloister();
            if (cloister != null) {
                scoreCompleted(cloister, false);
            }
        }

        if (castleCap != null) {
            // IMMUTABLE TODO
//            for (Entry<Castle, Integer> entry : castleCap.getCastleScore().entrySet()) {
//                scoreCastle(entry.getKey(), entry.getValue());
//            }
        }

        if (gldCap != null) {
            gldCap.awardGoldPieces();
        }

        alreadyScored.clear();
        next();
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

    private void scoreCompleted(Completable completable, boolean triggerBuilder) {
        GameState state = game.getState();
        if (triggerBuilder && builderCap != null) {
            if (!completable.getMeeples(state).find(Predicates.instanceOf(Builder.class)).isEmpty()) {
                builderCap.useBuilder();
            }
        }
        if (completable.isCompleted(state) && !alreadyScored.contains(completable)) {
            alreadyScored.add(completable);
            game.scoreCompleted(completable);

            game.replaceState(
                new ScoreFeature(completable),
                new UndeployMeeples(completable)
            );
            //IMMUTABLE TODO
//   notify scored wagon
//          if (m instanceof Wagon && wagonCap != null) {
//          wagonCap.wagonScored((Wagon) m, feature);
//      	}
            //game.post(new FeatureCompletedEvent(getActivePlayer(), completable));
        }
    }

}
