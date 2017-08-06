package com.jcloisterzone.game.capability;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.BarnAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.feature.Farm;
import com.jcloisterzone.figure.Barn;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.Pig;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;

import io.vavr.collection.List;
import io.vavr.collection.Set;


public final class BarnCapability extends Capability<Void> {

    @Override
    public List<Special> createPlayerSpecialMeeples(Player player, MeepleIdProvider idProvider) {
        return List.of((Special) new Barn(idProvider.generateId(Pig.class), player));
    }

    @Override
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        Position pos = getCurrentTile().getPosition();

        if (game.getActivePlayer().hasSpecialMeeple(Barn.class)) {
            BarnAction barnAction = null;
            Location corner = Location.WR.union(Location.NL);
            Location positionChange = Location.W;
            for (int i = 0; i < 4; i++) {
                if (isBarnCorner(corner, positionChange)) {
                    if (barnAction == null) {
                        barnAction = new BarnAction();
                        actions.add(barnAction);
                    }
                    barnAction.add(new FeaturePointer(pos, corner));
                }
                corner = corner.next();
                positionChange = positionChange.next();
            }
        }
    }

    private boolean isBarnCorner(Location corner, Location positionChange) {
        Farm farm = null;
        Position pos = getCurrentTile().getPosition();
        for (int i = 0; i < 4; i++) {
            Tile tile = getBoard().getPlayer(pos);
            if (tile == null) return false;
            farm = (Farm) tile.getFeaturePartOf(corner);
            if (farm == null) return false;
            corner = corner.next();
            pos = pos.add(positionChange);
            positionChange = positionChange.next();
        }

        if (!game.getBooleanValue(CustomRule.MULTI_BARN_ALLOWED)) {
            return !farm.walk(new IsOccupied().with(Barn.class));
        }

        return true;
    }
}