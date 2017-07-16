package com.jcloisterzone.action;

import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.message.CommitMessage;

import io.vavr.collection.HashSet;

public class ConfirmAction extends PlayerAction<Boolean> {

    public ConfirmAction() {
        super(HashSet.empty());
    }

    @Override
    public void perform(GameController gc, Boolean target) {
        Game game = gc.getGame();
        gc.getConnection().send(new CommitMessage(game.getGameId()));
    }
}
