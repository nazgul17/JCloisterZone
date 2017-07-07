package com.jcloisterzone.event;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.Meeple;

public class ScoreEvent extends PlayEvent  {

    //TODO fields revision

    private final BoardPointer pointer;

    private final int points;
    private final PointCategory category;
    private final Class<? extends Meeple> meepleType;

    private String label;
    private boolean isFinal;

    public ScoreEvent(FeaturePointer fp, int points, PointCategory category, Meeple meeple) {
        super(null, meeple == null ? null : meeple.getPlayer());
        this.pointer = fp;
        this.points = points;
        this.category = category;
        this.meepleType = meeple.getClass();
    }

    public ScoreEvent(Position position, Player targetPlayer, int points, PointCategory category) {
        super(null, targetPlayer);
        this.pointer = position;
        this.meepleType = null;
        this.points = points;
        this.category = category;
    }

    public FeaturePointer getFeaturePointer() {
        return (pointer instanceof FeaturePointer) ? (FeaturePointer) pointer : null;
    }

    public Position getPosition() {
        return pointer.getPosition();
    }

    public int getPoints() {
        return points;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label == null ? points + "" : label;
    }

    public Class<? extends Meeple> getMeepleType() {
        return meepleType;
    }

    public PointCategory getCategory() {
        return category;
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public String toString() {
        return "ScoreEvent(  " + pointer +  ", " + points + ")";
    }
}
