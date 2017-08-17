package com.jcloisterzone.reducers;

import com.jcloisterzone.board.Board;
import com.jcloisterzone.feature.Castle;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.feature.Scoreable;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.state.GameState;

import io.vavr.Predicates;

public class FinalScoring implements Reducer {

    @Override
    public GameState apply(GameState state) {
        Board board = state.getBoard();

        for (Scoreable feature : board.getOccupiedScoreables(Completable.class)) {
            Completable completable = (Completable) feature;
            state = (new ScoreCompletable(completable)).apply(state);
        }

        for (Scoreable feature : board.getOccupiedScoreables(Castle.class)) {
            // no points for castles at the end
            Castle castle = (Castle) feature;
            state = (new ScoreCastle(castle, 0)).apply(state);
        }

        for (Scoreable feature : board.getOccupiedScoreables(Farm.class)) {
            Farm farm = (Farm) feature;
            boolean hasBarn = farm.getSpecialMeeples(state)
                .find(Predicates.instanceOf(Barn.class)).isDefined();
            if (hasBarn) {
                   state = (new ScoreFarmBarn(farm)).apply(state);
            } else {
                state = (new ScoreFarm(farm)).apply(state);
            }
        }

        for (Capability<?> cap : state.getCapabilities().toSeq()) {
            state = cap.finalScoring(state);
        }

        return state;
    }

}
