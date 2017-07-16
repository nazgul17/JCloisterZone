package com.jcloisterzone.ui.grid.layer;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.jcloisterzone.action.TilePlacementAction;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TilePlacement;
import com.jcloisterzone.board.TileSymmetry;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.ActionPanel;
import com.jcloisterzone.ui.controls.action.ActionWrapper;
import com.jcloisterzone.ui.controls.action.TilePlacementActionWrapper;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.ForwardBackwardListener;
import com.jcloisterzone.ui.grid.GridPanel;
import com.jcloisterzone.ui.resources.TileImage;

import io.vavr.collection.Set;

public class TilePlacementLayer extends AbstractTilePlacementLayer implements ActionLayer, ForwardBackwardListener {

    private TilePlacementActionWrapper actionWrapper;

    private Rotation realRotation;
    private Rotation previewRotation;
    private boolean allowedRotation;

    public TilePlacementLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
    }

    @Override
    public void setActionWrapper(boolean active, ActionWrapper actionWrapper) {
        this.actionWrapper = (TilePlacementActionWrapper) actionWrapper;
        setActive(active);
        if (actionWrapper == null) {
            setAvailablePositions(null);
            realRotation = null;
        } else {
            this.actionWrapper.setForwardBackwardDelegate(this);
            setAvailablePositions(getAction().groupByPosition().keySet());
        };
    }

    @Override
    public TilePlacementActionWrapper getActionWrapper() {
        return actionWrapper;
    }

    @Override
    public TilePlacementAction getAction() {
        return (TilePlacementAction) getActionWrapper().getAction();
    }

    @Override
    protected void drawPreviewIcon(Graphics2D g2, Position previewPosition) {
        if (realRotation != actionWrapper.getTileRotation()) {
            preparePreviewRotation(previewPosition);
        }
        TileImage previewIcon = rm.getTileImage(getAction().getTile(), previewRotation);
        Composite compositeBackup = g2.getComposite();
        g2.setComposite(allowedRotation ? ALLOWED_PREVIEW : DISALLOWED_PREVIEW);
        g2.drawImage(previewIcon.getImage(), getAffineTransform(previewIcon, previewPosition), null);
        g2.setComposite(compositeBackup);
    }

    private void preparePreviewRotation(Position p) {
        realRotation = getActionWrapper().getTileRotation();
        previewRotation = realRotation;

        Set<Rotation> allowedRotations = getAction().getRotations(p);
        if (allowedRotations.contains(previewRotation)) {
            allowedRotation = true;
        } else {
            if (allowedRotations.size() == 1) {
                previewRotation = allowedRotations.iterator().next();
                allowedRotation = true;
            } else if (getAction().getTile().getSymmetry() == TileSymmetry.S2) {
                previewRotation = realRotation.next();
                allowedRotation = true;
            } else {
                allowedRotation = false;
            }
        }
    }

    @Override
    public void forward() {
        rotate(Rotation.R90);
    }

    @Override
    public void backward() {
        rotate(Rotation.R270);
    }

    private void rotate(Rotation spin) {
        Rotation current = getActionWrapper().getTileRotation();
        Rotation next = current.add(spin);
        if (getPreviewPosition() != null) {
            Set<Rotation> rotations = getAction().getRotations(getPreviewPosition());
            if (!rotations.isEmpty()) {
                if (rotations.size() == 1) {
                    next = rotations.iterator().next();
                } else {
                    if (rotations.contains(current)) {
                        while (!rotations.contains(next)) next = next.add(spin);
                    } else {
                        if (getAction().getTile().getSymmetry() == TileSymmetry.S2 && rotations.size() == 2) {
                            //if S2 and size == 2 rotate to flip preview to second choice
                            next = next.add(spin);
                        } else {
                            next = current;
                        }
                        while (!rotations.contains(next)) next = next.add(spin);
                    }
                }
            }
        }
        getActionWrapper().setTileRotation(next);
        ActionPanel panel = gc.getGameView().getControlPanel().getActionPanel();
        panel.refreshImageCache();
        gc.getGameView().getGridPanel().repaint();
    }

    @Override
    public void squareEntered(MouseEvent e, Position p) {
        realRotation = null;
        super.squareEntered(e, p);
    }

    @Override
    public void squareExited(MouseEvent e, Position p) {
        realRotation = null;
        super.squareExited(e, p);
    }

    @Override
    public void mouseClicked(MouseEvent e, Position p) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (getPreviewPosition() != null && isActive() && allowedRotation) {
                e.consume();
                getAction().perform(gc, new TilePlacement(p, previewRotation));
            }
        }
    }

}
