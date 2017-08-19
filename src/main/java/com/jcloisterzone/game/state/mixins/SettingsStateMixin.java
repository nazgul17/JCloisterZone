package com.jcloisterzone.game.state.mixins;

import java.util.function.Function;

import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;

import io.vavr.collection.Map;

public interface SettingsStateMixin {

    CapabilitiesState getCapabilities();
    GameState setCapabilities(CapabilitiesState capabilities);

    Map<CustomRule, Object> getRules();

    default boolean hasCapability(Class<? extends Capability<?>> cls) {
        return getCapabilities().contains(cls);
    }

    default GameState updateCapabilities(Function<CapabilitiesState, CapabilitiesState> fn) {
        return setCapabilities(fn.apply(getCapabilities()));
    }

    default <M> M getCapabilityModel(Class<? extends Capability<M>> cls) {
        return getCapabilities().getModel(cls);
    }

    default <M> GameState setCapabilityModel(Class<? extends Capability<M>> cls, M model) {
        return setCapabilities(getCapabilities().setModel(cls, model));
    }

    default <M> GameState updateCapabilityModel(Class<? extends Capability<M>> cls, Function<M, M> fn) {
        return setCapabilities(getCapabilities().updateModel(cls, fn));
    }

    default boolean getBooleanValue(CustomRule rule) {
        assert rule.getType().equals(Boolean.class);
        return (Boolean) getRules().get(rule).getOrElse(Boolean.FALSE);
    }

}
