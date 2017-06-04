package com.jcloisterzone.figure;

import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.game.Game;

public class SmallFollower extends Follower {

    private static final long serialVersionUID = 9167040308990588349L;

    public static final int QUANTITY = 7;

    public SmallFollower(Game game, Integer idSuffix, PlayerAttributes player) {
        super(game, idSuffix, player);
    }

}
