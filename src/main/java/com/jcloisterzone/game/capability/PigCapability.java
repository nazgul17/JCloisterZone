package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.MeepleAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.figure.BigFollower;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

public class PigCapability extends Capability {

    @Override
    public List<Special> createPlayerSpecialMeeples(Player player) {
        return List.of((Special) new Pig(player));
    }


    @Override
    public Vector<PlayerAction<?>> prepareActions(Vector<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        Player player = game.getActivePlayer();
        if (!player.hasSpecialMeeple(Pig.class)) return;

        Tile tile = getCurrentTile();
        if (!game.isDeployAllowed(tile, Pig.class)) return;

        Position pos = tile.getPosition();
        MeepleAction pigAction = null;
        for (Location loc : tile.getPlayerFeatures(player, Farm.class)) {
            if (pigAction == null) {
                pigAction = new MeepleAction(Pig.class);
                actions.add(pigAction);
            }
            pigAction.add(new FeaturePointer(pos, loc));
        }
    }
}
