package com.jcloisterzone.reducers;

import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Scoreable;
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
            state = (new ScoreFeature(feature)).apply(state);

            //IMMUTABLE TODO solve Barn scoring
        }

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.finalScoring(state);
        }

        return state;
    }

}
