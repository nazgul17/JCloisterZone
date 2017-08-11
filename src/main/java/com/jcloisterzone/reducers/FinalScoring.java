package com.jcloisterzone.reducers;

import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;
import io.vavr.collection.Stream;

public class FinalScoring implements Reducer {

    @Override
    public GameState apply(GameState state) {
        Stream<Scoreable> scoreables = state.getBoard().getOccupiedScoreables();

        //score first all except farms
        for (Scoreable feature : scoreables.filter(Predicates.<Scoreable>instanceOf(Farm.class).negate())) {
            state = (new ScoreFeature(feature)).apply(state);
        }

        //then score farms
        for (Scoreable feature : scoreables.filter(Predicates.instanceOf(Farm.class))) {
            Farm farm = (Farm) feature;
            boolean hasBarn = farm.getSpecialMeeples(state)
                .find(Predicates.instanceOf(Barn.class)).isDefined();
            if (hasBarn) {
                   state = (new ScoreFarmBarn(farm)).apply(state);
            } else {
                state = (new ScoreFeature(farm)).apply(state);
            }
        }

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.finalScoring(state);
        }

        return state;
    }

}
