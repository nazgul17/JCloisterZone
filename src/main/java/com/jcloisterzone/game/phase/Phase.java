package com.jcloisterzone.game.phase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateHelpers;
import com.jcloisterzone.reducers.PayRansom;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.PayRansomMessage;


public abstract class Phase implements GameStateHelpers {

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
        game.markUndo();
        GameState state = game.getState();
        state = (new PayRansom(msg.getMeepleId())).apply(state);
        promote(state);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
