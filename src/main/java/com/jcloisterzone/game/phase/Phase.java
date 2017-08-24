package com.jcloisterzone.game.phase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Player;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.PayRansom;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PassMessage;
import com.jcloisterzone.wsio.message.PayRansomMessage;
import com.jcloisterzone.wsio.message.ToggleClockMessage;


public abstract class Phase {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected final Game game;
    protected final GameController gc;

    private Phase defaultNext;

    public Phase(GameController gc) {
        this.gc = gc;
        this.game = gc.getGame();
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

    public Connection getConnection() {
        return gc.getConnection();
    }

    public GameController getGameController() {
        return gc;
    }

    public DebugConfig getDebugConfig() {
        Config config = gc.getConfig();
        return config == null ? null : config.getDebug();
    }

    public boolean isLocalPlayer(Player player) {
        return player.getSlot().isOwn();
    }

    //IMMUTABLE TODO - toggle can be derived from players action !!!
    public void toggleClock(Player player) {
        if (isLocalPlayer(player)) {
            getConnection().send(new ToggleClockMessage(game.getGameId(), player.getIndex()));
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
