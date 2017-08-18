package com.jcloisterzone.reducers;

import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateHelpers;

import io.vavr.Function1;

public interface Reducer extends Function1<GameState, GameState>, GameStateHelpers {

}
