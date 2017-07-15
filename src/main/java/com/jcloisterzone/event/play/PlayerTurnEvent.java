package com.jcloisterzone.event.play;

import com.jcloisterzone.Player;

public class PlayerTurnEvent extends PlayEvent {

    public PlayerTurnEvent(Player targetPlayer) {
        super(targetPlayer);
    }

    @Override
    public String toString() {
        return super.toString() + " player:" + getTriggeringPlayer();
    }

}
