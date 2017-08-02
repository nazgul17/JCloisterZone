package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.play.ScoreEvent;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.capability.FairyCapability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Set;

public class ScoreFeature implements Reducer {

    private final Scoreable feature;

    public ScoreFeature(Scoreable feature) {
        this.feature = feature;
    }

    private GameState scorePlayer(GameState state, Player p, Follower nextToFairy, boolean finalScoring) {
        int points = feature.getPoints(state, p);
        PointCategory pointCategory = feature.getPointCategory();

        state = (new AddPoints(p, points, pointCategory)).apply(state);

        Follower follower = nextToFairy == null ? feature.getSampleFollower(state, p) : nextToFairy;
        ScoreEvent scoreEvent;


        if (nextToFairy != null && !finalScoring) {
            state = (new AddPoints(
                p, FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, PointCategory.FAIRY
            )).apply(state);

            scoreEvent = new ScoreEvent(
                points+FairyCapability.FAIRY_POINTS_FINISHED_OBJECT,
                points+" + "+FairyCapability.FAIRY_POINTS_FINISHED_OBJECT,
                pointCategory,
                finalScoring,
                follower.getDeployment(state),
                follower
            );
        } else {
            scoreEvent = new ScoreEvent(
                points,
                pointCategory,
                finalScoring,
                follower.getDeployment(state),
                follower
            );
        }

        state = state.appendEvent(scoreEvent);
        return state;
    }

    @Override
    public GameState apply(GameState state) {
        boolean finalScoring = state.isGameOver();

        Set<Player> players = feature.getOwners(state);
        if (players.isEmpty()) return state;

        HashMap<Player, Follower> playersWithFairyBonus = HashMap.empty();

        BoardPointer ptr = state.getNeutralFigures().getFairyDeployment();
        if (ptr != null && !finalScoring) {
            boolean onTileRule = ptr instanceof Position;
            FeaturePointer fairyFp = ptr.asFeaturePointer();

            for (Tuple2<Follower, FeaturePointer> t : feature.getFollowers2(state)) {
                Follower m = t._1;
                if (!t._2.equals(fairyFp)) continue;

                if (!onTileRule) {
                    if (!((MeeplePointer) ptr).getMeepleId().equals(m.getId())) continue;
                }

                playersWithFairyBonus = playersWithFairyBonus.put(m.getPlayer(), m);
            }
        }

        for (Player pl : players) {
            Follower nextToFairy = playersWithFairyBonus.get(pl).getOrNull();
            state = scorePlayer(state, pl, nextToFairy, finalScoring);
        }

        for (Player pl : playersWithFairyBonus.keySet().removeAll(players)) {
            // player is not owner but next to fairy -> add just fairy points
            Follower nextToFairy = playersWithFairyBonus.get(pl).getOrNull();

            state = (new AddPoints(
                pl, FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, PointCategory.FAIRY
            )).apply(state);

            ScoreEvent scoreEvent = new ScoreEvent(
                FairyCapability.FAIRY_POINTS_FINISHED_OBJECT,
                PointCategory.FAIRY,
                false,
                nextToFairy.getDeployment(state),
                nextToFairy
            );
            state = state.appendEvent(scoreEvent);
        }

        return state;
    }

}
