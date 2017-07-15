package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.event.play.PlayEvent;

@Idempotent
public class RequestConfirmEvent extends PlayEvent {

    public RequestConfirmEvent(Player targetPlayer) {
        super(null, targetPlayer);
    }


}
