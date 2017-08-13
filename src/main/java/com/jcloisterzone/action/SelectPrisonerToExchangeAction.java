package com.jcloisterzone.action;

import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.annotations.LinkedPanel;
import com.jcloisterzone.ui.grid.actionpanel.PrisonersExchangePanel;
import com.jcloisterzone.wsio.message.ExchangeFollowerChoiceMessage;

import io.vavr.collection.Set;

/**
 * Action is triggered in rare case when there is different follower classes
 * to exchange and owner can choose which one want to return.
 * Eg. opponent has captured both my big and small. When I capture
 * it's follower exchange happens immediately and I can choose which one should
 * be returned.
 *
 */
@LinkedPanel(PrisonersExchangePanel.class)
public class SelectPrisonerToExchangeAction extends PlayerAction<Follower> {

    private final Follower justCapturedFollower;

    public SelectPrisonerToExchangeAction(Follower justCapturedFollower, Set<Follower> options) {
        super(options);
        this.justCapturedFollower = justCapturedFollower;
    }

    @Override
    public void perform(GameController gc, Follower f) {
        gc.getConnection().send(
            new ExchangeFollowerChoiceMessage(gc.getGameId(), f.getId())
        );
    }

    public Follower getJustCapturedFollower() {
        return justCapturedFollower;
    }
}
