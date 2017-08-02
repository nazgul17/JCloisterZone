package com.jcloisterzone.game.capability;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Mayor;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;

public class MayorCapability extends Capability<Void> {

    @Override
    public List<Follower> createPlayerFollowers(Player player, MeepleIdProvider idProvider) {
        return List.of((Follower) new Mayor(idProvider.generateId(Mayor.class), player));
    }

    private Set<FeaturePointer> filterMayorLocations(Set<FeaturePointer> followerOptions) {
        return Sets.filter(followerOptions, new Predicate<FeaturePointer>() {
            @Override
            public boolean apply(FeaturePointer bp) {
                Feature fe = getBoard().getPlayer(bp);
                return fe instanceof City;
            }
        });

    }

    @Override
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        if (game.getActivePlayer().hasFollower(Mayor.class) && !followerOptions.isEmpty()) {
            Set<FeaturePointer> mayorLocations = filterMayorLocations(followerOptions);
            if (!mayorLocations.isEmpty()) {
                actions.add(new MeepleAction(Mayor.class).addAll(mayorLocations));
            }
        }
    }

}
