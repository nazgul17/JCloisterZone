package com.jcloisterzone.reducers;

import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.game.GameState;

public class ScoreFeature implements Reducer {

    private final Scoreable feature;

    public ScoreFeature(Scoreable feature) {
        this.feature = feature;
    }

    @Override
    public GameState apply(GameState state) {
    }

}
