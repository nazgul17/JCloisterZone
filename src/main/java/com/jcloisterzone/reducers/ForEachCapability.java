package com.jcloisterzone.reducers;

import java.util.function.BiFunction;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameState;

public class ForEachCapability implements Reducer {

    private final BiFunction<Capability, GameState, GameState> fn;

    public ForEachCapability(BiFunction<Capability, GameState, GameState> fn) {
        this.fn = fn;
    }

    @Override
    public GameState apply(GameState state) {
        for(Capability cap : state.getCapabilities().values()) {
            state = fn.apply(cap, state);
        }
        return state;
    }

}
