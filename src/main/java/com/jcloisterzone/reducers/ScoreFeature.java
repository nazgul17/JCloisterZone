package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.event.ScoreEvent;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.GameState;
import com.jcloisterzone.game.capability.FairyCapability;

import io.vavr.collection.Set;

public class ScoreFeature implements Reducer {

    private final Scoreable feature;

    public ScoreFeature(Scoreable feature) {
        this.feature = feature;
    }

     private GameState scorePlayer(GameState state, Player p) {
        boolean finalScoring = state.isGameOver();

        int points = feature.getPoints(state, p);
        PointCategory pointCategory = feature.getPointCategory();

        state = (new AddPoints(p, points, pointCategory)).apply(state);

        Follower follower = feature.getSampleFollower(state, p);
        ScoreEvent scoreEvent;
        boolean isFairyScore = false;
// IMMUTABLE TODO
//        if (fairyCapability != null) {
//            for (Follower f : feature.getFollowers()) {
//                if (f.getPlayer().equals(p) && fairyCapability.isNextTo(f)) {
//                    isFairyScore = true;
//                    break;
//                }
//            }
//        }
        if (isFairyScore && !finalScoring) {
            state = (new AddPoints(
                p, FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, PointCategory.FAIRY
            )).apply(state);

            scoreEvent = new ScoreEvent(follower.getDeployment(state), points+FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, pointCategory, follower);
            scoreEvent.setLabel(points+" + "+FairyCapability.FAIRY_POINTS_FINISHED_OBJECT);
        } else {
            scoreEvent = new ScoreEvent(follower.getDeployment(state), points, pointCategory, follower);
        }
        scoreEvent.setFinal(finalScoring);

        state = state.appendEvent(scoreEvent);
        return state;
    }

    @Override
    public GameState apply(GameState state) {
        Set<Player> players = feature.getOwners(state);
        if (players.isEmpty()) return state;

        for (Player pl : players) {
            state = scorePlayer(state, pl);
        }
//        if (fairyCapability != null && !isOver()) {
//            java.util.Set<Player> fairyPlayersWithoutMayority = new java.util.HashSet<>();
//            for (Follower f : feature.getFollowers()) {
//                Player owner = getPlayer(f.getPlayer());
//                if (fairyCapability.isNextTo(f) && !players.contains(owner)
//                    && !fairyPlayersWithoutMayority.contains(owner)) {
//                    fairyPlayersWithoutMayority.add(owner);
//
//                    owner.addPoints(FairyCapability.FAIRY_POINTS_FINISHED_OBJECT, PointCategory.FAIRY);
//                }
//            }
//        }
        return state;
    }

}
