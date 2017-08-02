package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.TradeGoods;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.TradeGoodsCapability;

public class TradeResourceEvent extends PlayEvent {

    private final TradeGoods resource;
    private final int count;

    public TradeResourceEvent(Player player, TradeGoods resource, int count) {
        super(player, player);
        this.resource = resource;
        this.count = count;
    }

    public TradeGoods getResource() {
        return resource;
    }

    public int getCount() {
        return count;
    }
}
