package com.jcloisterzone.reducers;

import com.jcloisterzone.Player;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.game.state.GameState;

public class ScoreCompletable extends ScoreFeature {

    private int featurePoints;

    public ScoreCompletable(Completable feature) {
        super(feature);
    }

    @Override
    int getFeaturePoints(GameState state, Player player) {
        return featurePoints;
    }

    @Override
    public Completable getFeature() {
        return (Completable) super.getFeature();
    }

    @Override
    public GameState apply(GameState state) {
        featurePoints = getFeature().getPoints(state);
        return super.apply(state);
    }

}
