package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.ConfirmAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.config.Config.ConfirmConfig;
import com.jcloisterzone.event.play.MeepleDeployed;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.CommitMessage;

public class CommitActionPhase extends ServerAwarePhase {

    public CommitActionPhase(Game game, GameController gc) {
        super(game, gc);
    }

    @Override
    public void enter(GameState state) {
        Player player = state.getTurnPlayer();
        if (isLocalPlayer(player)) {
            boolean needsConfirm = false;
            // IMMUTABLE TODO
            PlayEvent last = state.getEvents().last();
            if (last instanceof MeepleDeployed) {
                ConfirmConfig cfg =  getConfig().getConfirm();
                MeepleDeployed ev = (MeepleDeployed) last;
                if (cfg.getAny_deployment()) {
                    needsConfirm = true;
                } else if (cfg.getFarm_deployment() && ev.getLocation().isFarmLocation()) {
                    needsConfirm = true;
                } else if (cfg.getOn_tower_deployment() && ev.getLocation() == Location.TOWER) {
                    needsConfirm = true;
                }
            }
            if (!needsConfirm) {
                promote(state);
                getConnection().send(new CommitMessage(game.getGameId()));
                return;
            }
        }

        //if player is not active, always trigger event and wait for remote CommitMessage
        state = state.setPlayerActions(
            new ActionsState(player, new ConfirmAction(), false)
        );
        promote(state);
    }

    @WsSubscribe
    public void handleCommit(CommitMessage msg) {
        game.clearUndo();
        game.updateRandomSeed(msg.getCurrentTime());

        GameState state = game.getState();
        state = clearActions(state);
        next(state);
    }
}
