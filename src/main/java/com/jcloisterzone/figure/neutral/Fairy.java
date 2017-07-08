package com.jcloisterzone.figure.neutral;

import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.NeutralFigureMoveEvent;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.game.Game;

public class Fairy extends NeutralFigure<BoardPointer> {

    private static final long serialVersionUID = 4710402383462428260L;

    private Follower nextTo;

    public Fairy(Game game) {
        super(game);
    }

    public Follower getNextTo() {
        return nextTo;
    }

    public void setNextTo(Follower nextTo) {
        this.nextTo = nextTo;
    }

    @Override
    public void deploy(BoardPointer at) {
        if (at instanceof MeeplePointer) {
            //new rules
            setNextTo((Follower) game.getMeeple((MeeplePointer) at));
        } else if (at == null) {
            setNextTo(null);;
        }
        super.deploy(at);
    }
}
