package com.jcloisterzone.game.phase;

import java.util.Collections;
import java.util.List;

import com.jcloisterzone.Player;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.SelectFeatureAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.SelectActionEvent;
import com.jcloisterzone.figure.neutral.Count;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.capability.CountCapability;
import com.jcloisterzone.wsio.RmiProxy;

public class CocCountPhase extends Phase {

    private final CountCapability countCap;

    public CocCountPhase(Game game) {
        super(game);
        countCap = game.getCapability(CountCapability.class);
    }

    @Override
    public boolean isActive() {
        return game.hasCapability(CountCapability.class);
    }

    @Override
    public void enter() {
        Player activePlayer = game.getActivePlayer();
        Position pos = countCap.getQuarterPosition();
        Location countLoc = countCap.getCount().getLocation();

        //TODO neutral meeple action. see MagaAndWitch
        SelectFeatureAction action = new SelectFeatureAction("count") {
            @Override
            public void perform(RmiProxy server, FeaturePointer target) {
                server.moveNeutralFigure(target, Count.class);
            }
        };

        for (Location quarter : Location.quarters()) {
            if (countLoc != quarter) {
                action.add(new FeaturePointer(pos, quarter));
            }
        }

        List<PlayerAction<?>> actions = Collections.singletonList(action);
        game.post(new SelectActionEvent(activePlayer, actions, true));
    }

    @Override
    public void moveNeutralFigure(BoardPointer ptr, Class<? extends NeutralFigure> figureType) {
        FeaturePointer fp = (FeaturePointer) ptr;
        Count count = countCap.getCount();
        count.deploy(fp);
        next();
    }

}
