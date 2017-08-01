package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.figure.Builder;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameState;

import io.vavr.collection.List;

public class BuilderCapability extends Capability {

    private static final long serialVersionUID = 1L;

    public enum BuilderState { UNUSED, USED, SECOND_TURN; }

    private final BuilderState builderState;

    public BuilderCapability() {
        this(BuilderState.UNUSED);
    }

    public BuilderCapability(BuilderState builderState) {
        this.builderState = builderState;
    }

    public BuilderCapability setBuilderState(BuilderState builderState) {
        return new BuilderCapability(builderState);
    }

    public BuilderCapability useBuilder() {
        if (builderState == BuilderState.UNUSED) {
            return setBuilderState(BuilderState.USED);
        }
        return this;
    }

    public BuilderState getBuilderState() {
        return builderState;
    }

    @Override
    public List<Special> createPlayerSpecialMeeples(Player p, MeepleIdProvider idProvider) {
        return List.of((Special) new Builder(idProvider.generateId(Builder.class), p));
    }


    @Override
    public GameState turnPartCleanUp(GameState state) {
        BuilderState bs = state.getCapability(BuilderCapability.class).getBuilderState();

        if (bs == BuilderState.USED) {
            return state.updateCapability(BuilderCapability.class, cap -> cap.setBuilderState(BuilderState.SECOND_TURN));
        }
        if (bs == BuilderState.SECOND_TURN) {
            return state.updateCapability(BuilderCapability.class, cap -> cap.setBuilderState(BuilderState.UNUSED));
        }
        return state;
    }
}
