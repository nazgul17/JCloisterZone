package com.jcloisterzone.feature;

import com.jcloisterzone.PointCategory;
import com.jcloisterzone.feature.visitor.score.ScoreContext;
import com.jcloisterzone.game.Game;

public interface Scoreable extends Feature {

    PointCategory getPointCategory();

    @Deprecated
    ScoreContext getScoreContext();

}
