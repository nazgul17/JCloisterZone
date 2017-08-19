package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.message.ToggleClockMessage;

public abstract class ServerAwarePhase extends Phase {

    private final GameController gc;

    public ServerAwarePhase(Game game, GameController gc) {
        super(game);
        this.gc = gc;
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
}
