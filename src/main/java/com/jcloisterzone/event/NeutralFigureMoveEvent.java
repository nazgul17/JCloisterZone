package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;

public class NeutralFigureMoveEvent extends MoveEvent<BoardPointer> {

    private final NeutralFigure<?> figure;

    public NeutralFigureMoveEvent(Player player, NeutralFigure<?> figure, BoardPointer from, BoardPointer to) {
        super(player, from, to);
        this.figure = figure;
    }

    public NeutralFigure<?> getFigure() {
        return figure;
    }
}
