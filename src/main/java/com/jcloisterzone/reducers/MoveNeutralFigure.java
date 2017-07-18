package com.jcloisterzone.reducers;

import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.GameState;

public class MoveNeutralFigure implements Reducer {

    private final NeutralFigure<?> figure;
    private final BoardPointer pointer;
    
    
    public MoveNeutralFigure(NeutralFigure<?> figure, BoardPointer pointer) {
		super();
		this.figure = figure;
		this.pointer = pointer;
	}

	@Override
    public GameState apply(GameState state) {
        // TODO Auto-generated method stub
        return null;
    }

}
