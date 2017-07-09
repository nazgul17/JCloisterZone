package com.jcloisterzone.action;

import java.awt.Color;
import java.awt.Image;

import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.figure.neutral.Mage;
import com.jcloisterzone.figure.neutral.Witch;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.wsio.RmiProxy;

//TODO generic NeutralMeepleAction
public class MageAndWitchAction extends SelectFeatureAction {

    private final boolean mage;

    public MageAndWitchAction(boolean mage) {
        this.mage = mage;
    }

    protected Image getImage(Color color) {
        String name = mage ? "mage": "witch";
        return client.getResourceManager().getLayeredImage(new LayeredImageDescriptor("actions/" + name, color));
    }

    @Override
    public void perform(RmiProxy server, FeaturePointer target) {
        if (mage) {
            server.moveNeutralFigure(target, Mage.class);
        } else {
            server.moveNeutralFigure(target, Witch.class);
        }
    }
}
