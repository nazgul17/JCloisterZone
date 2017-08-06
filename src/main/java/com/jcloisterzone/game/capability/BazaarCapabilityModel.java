package com.jcloisterzone.game.capability;

import com.jcloisterzone.Immutable;

import io.vavr.collection.Queue;

@Immutable
public class BazaarCapabilityModel {

    //update bazaar item to immutablw
    private final Queue<BazaarItem> supply;

    public BazaarCapabilityModel(Queue<BazaarItem> supply) {
        this.supply = supply;
    }

    public Queue<BazaarItem> getSupply() {
        return supply;
    }
}
