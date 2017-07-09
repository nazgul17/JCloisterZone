package com.jcloisterzone.ui.grid.layer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;

import com.jcloisterzone.action.FairyOnTileAction;
import com.jcloisterzone.action.GoldPieceAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.SelectTileAction;
import com.jcloisterzone.action.TowerPieceAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.action.ActionWrapper;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridMouseAdapter;
import com.jcloisterzone.ui.grid.GridMouseListener;
import com.jcloisterzone.ui.grid.GridPanel;


public class TileActionLayer extends AbstractGridLayer implements GridMouseListener, ActionLayer {

    private SelectTileAction action;
    private ActionWrapper actionWrapper;
    private boolean active;
    private Image gridDecoration;

    public TileActionLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public void onShow() {
        super.onShow();
        attachMouseInputListener(new GridMouseAdapter(gridPanel, this));
    }

    @Override
    public void setActionWrapper(boolean active, ActionWrapper actionWrapper) {
        this.actionWrapper = actionWrapper;
        this.active = active;
        PlayerAction<?> action = getAction();
        if (action == null) {
            gridDecoration = null;
        } else if (action instanceof FairyOnTileAction) {
            gridDecoration = rm.getImage("decorations/fairy");
        } else if (action instanceof TowerPieceAction) {
            gridDecoration = rm.getImage("decorations/tower");
        } else if (action instanceof GoldPieceAction) {
            gridDecoration = rm.getImage("decorations/gold");
        }
    }

    @Override
    public ActionWrapper getActionWrapper() {
        return actionWrapper;
    }

    @Override
    public SelectTileAction getAction() {
        return actionWrapper == null ? null : (SelectTileAction) actionWrapper.getAction();
    }

    @Override
    public void paint(Graphics2D g2) {
        int imgSize = gridDecoration.getWidth(null);
        for (Position pos : action.getOptions()) {
            g2.drawImage(gridDecoration, getAffineTransform(imgSize, imgSize, pos), null);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e, Position p) {
        if (!active) return;
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (action.getOptions().contains(p)) {
                e.consume();
                action.perform(getRmiProxy(), p);
            }
        }
    }


    @Override
    public void squareEntered(MouseEvent e, Position p) { }
    @Override
    public void squareExited(MouseEvent e, Position p) {  }
}
