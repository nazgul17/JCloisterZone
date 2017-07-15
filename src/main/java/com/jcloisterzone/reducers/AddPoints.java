package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.game.GameState;

import io.vavr.collection.Array;

public class AddPoints implements Reducer {

    final Player player;
    final int points;
    final PointCategory category;

    public AddPoints(Player player, int points, PointCategory category) {
        this.player = player;
        this.points = points;
        this.category = category;
    }

    @Override
    public GameState apply(GameState state) {
        if (points == 0) {
            return state;
        }

        int idx = player.getIndex();
        Array<PlayerScore> score = state.getScore();
        PlayerScore playerScore = score.get(idx);
        score = score.insert(idx, playerScore.addPoints(points, category));
        return state.setScore(score);
    }

}
