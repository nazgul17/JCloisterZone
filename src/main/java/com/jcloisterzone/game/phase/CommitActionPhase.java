package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.ConfirmAction;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.CommitMessage;

public class CommitActionPhase extends Phase {

    public CommitActionPhase(Game game) {
        super(game);
    }

    @Override
    public void enter(GameState state) {
        Player player = state.getTurnPlayer();
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
