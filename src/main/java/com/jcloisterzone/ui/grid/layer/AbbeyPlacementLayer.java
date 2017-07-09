package com.jcloisterzone.ui.grid.layer;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;

import com.jcloisterzone.action.AbbeyPlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.action.ActionWrapper;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.GridPanel;

public class AbbeyPlacementLayer extends AbstractTilePlacementLayer implements ActionLayer {

    private ActionWrapper actionWrapper;

    public AbbeyPlacementLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public void setActionWrapper(boolean active, ActionWrapper actionWrapper) {
        this.actionWrapper = actionWrapper;
        setActive(active);
        setAvailablePositions(getAction() == null ? null : getAction().getOptions());
    }

    @Override
    public ActionWrapper getActionWrapper() {
        return actionWrapper;
    }

    @Override
    public AbbeyPlacementAction getAction() {
        return actionWrapper == null ? null : (AbbeyPlacementAction) actionWrapper.getAction();
    }

    @Override
    protected void drawPreviewIcon(Graphics2D g2, Position previewPosition) {
        //TODO offset
        Image previewIcon = rm.getAbbeyImage(Rotation.R0).getImage();
        Composite compositeBackup = g2.getComposite();
        g2.setComposite(ALLOWED_PREVIEW);
        g2.drawImage(previewIcon,
            getAffineTransform(previewIcon.getWidth(null), previewIcon.getHeight(null), previewPosition), null);
        g2.setComposite(compositeBackup);
    }

    @Override
    public void mouseClicked(MouseEvent e, Position p) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (getPreviewPosition() != null && isActive()) {
                e.consume();
                getAction().perform(getRmiProxy(), p);
            }
        }
    }

}
