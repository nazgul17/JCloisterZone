package com.jcloisterzone.game;

import java.util.function.Function;

import io.vavr.control.Option;

public class CachedValue<T> {

    private final Game game;
    private final Function<GameState, T> fn;

    private GameState validFor;
    private Option<T> value;

    public CachedValue(Game game, Function<GameState, T> fn) {
        this.game = game;
        this.fn = fn;
    }

    public T get() {
        if (validFor != game.getState() && value == null) {
            validFor = game.getState();
            value = Option.of(fn.apply(validFor));
        }
        return value.getOrNull();
    }
}
