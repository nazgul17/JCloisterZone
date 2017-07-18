package com.jcloisterzone.event.play;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.figure.neutral.NeutralFigure;

public class NeutralFigureMoved extends PlayEvent {

    private final BoardPointer from;
    private final BoardPointer to;
    private final NeutralFigure<?> neutralFigure;

    public NeutralFigureMoved(Player triggeringPlayer, NeutralFigure<?> neutralFigure,
            BoardPointer from, BoardPointer to) {
        super(triggeringPlayer);
        this.neutralFigure = neutralFigure;
        this.from = from;
        this.to = to;
    }

    public BoardPointer getFrom() {
        return from;
    }

    public BoardPointer getTo() {
        return to;
    }

    public NeutralFigure<?> getNeutralFigure() {
        return neutralFigure;
    }
}
