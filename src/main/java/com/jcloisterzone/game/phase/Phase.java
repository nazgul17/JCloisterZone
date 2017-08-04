package com.jcloisterzone.game.phase;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameState.Flag;
import com.jcloisterzone.reducers.AddPoints;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.PayRansomMessage;

import io.vavr.collection.Array;
import io.vavr.collection.List;


public abstract class Phase {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected final Game game;

    private Phase defaultNext;

    public Phase(Game game) {
        this.game = game;
    }

    public Phase getDefaultNext() {
        return defaultNext;
    }

    public void setDefaultNext(Phase defaultNext) {
        this.defaultNext = defaultNext;
    }

    public void next(GameState state) {
        next(state, defaultNext);
    }

    public void next(GameState state, Class<? extends Phase> phaseClass) {
        next(state, game.getPhases().get(phaseClass));
    }

    public void next(GameState state, Phase phase) {
        phase.enter(state);
    }

    public abstract void enter(GameState state);

    protected void promote(GameState state) {
        state = state.setPhase(this.getClass());
        game.replaceState(state);
    }

    protected GameState clearActions(GameState state) {
        return state.setPlayerActions(null);
    }

    /**
     * Method is invoked on active phase when user buy back inprisoned follower
     */
    @Deprecated //generic approach to refresh actions
    public void notifyRansomPaid() {
        //do nothing by default
    }

    public boolean isActive(CapabilitiesState capabilities) {
        return true;
    }

    @WsSubscribe
    public void handlePass(PassMessage msg) {
        GameState state = game.getState();

        if (!state.getPlayerActions().isPassAllowed()) {
            throw new IllegalStateException("Pass is not allowed");
        }

        state = clearActions(state);
        if (getDefaultNext() instanceof PhantomPhase) {
            //skip PhantomPhase if user pass turn
            getDefaultNext().next(state);
        } else {
            next(state);
        }
    }

    @WsSubscribe
    public void handlePayRansom(PayRansomMessage msg) {
        GameState state = game.getState();

        if (state.hasFlag(Flag.RANSOM_PAID)) {
            throw new IllegalStateException("Ransom can be paid only once a turn.");
        }


        Player player = state.getActivePlayer();
        Predicate<Follower> pred = f -> f.getId().equals(msg.getMeepleId());

        Array<List<Follower>> model = state.getCapabilities().getModel(TowerCapability.class);
        Follower follower = null;
        Player jailer = null;

        for (int i = 0; i < model.length(); i++) {
            follower = model.get(i).find(pred).getOrNull();
            if (follower != null) {
                jailer = state.getPlayers().getPlayer(i);
                break;
            }
        }

        if (follower == null) {
            throw new IllegalArgumentException(String.format("No such prisoner %s.", msg.getMeepleId()));
        }
        if (!follower.getPlayer().equals(player)) {
            new IllegalArgumentException("Cannot pay ransom for opponent's follower.");
        }

        Player _jailer = jailer;
        Follower _follower = follower;
        state = state.updateCapabilityModel(TowerCapability.class, m ->
            m.update(_jailer.getIndex(), l -> l.remove(_follower))
        );
        state = (new AddPoints(player, -TowerCapability.RANSOM_POINTS, PointCategory.TOWER_RANSOM)).apply(state);
        state = (new AddPoints(jailer, TowerCapability.RANSOM_POINTS, PointCategory.TOWER_RANSOM)).apply(state);
        state = state.addFlag(Flag.RANSOM_PAID);
        // TODO add PlayEvent

        promote(state);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
