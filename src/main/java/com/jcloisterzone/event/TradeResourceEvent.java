package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.ClothWineGrainCapability;

public class TradeResourceEvent extends PlayEvent {

    private final TradeResource resource;
    private final int count;

    public TradeResourceEvent(Player player, TradeResource resource, int count) {
        super(player, player);
        this.resource = resource;
        this.count = count;
    }

    public TradeResource getResource() {
        return resource;
    }

    public int getCount() {
        return count;
    }
}
