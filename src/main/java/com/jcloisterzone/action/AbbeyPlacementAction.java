package com.jcloisterzone.action;

import java.awt.Image;

import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.ui.annotations.LinkedGridLayer;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.grid.layer.AbbeyPlacementLayer;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Set;

@Deprecated
@LinkedImage("actions/abbeyplacement")
@LinkedGridLayer(AbbeyPlacementLayer.class)
public class AbbeyPlacementAction extends SelectTileAction {

    public AbbeyPlacementAction(Set<Position> options) {
        super(options);
    }

//    @Override
//    public Image getImage(ResourceManager rm, Player player, boolean active) {
//        return rm.getAbbeyImage(Rotation.R0).getImage();
//    }

    @Override
    public void perform(RmiProxy server, Position p) {
        server.placeTile(Rotation.R0, p);
    }

    @Override
    public String toString() {
        return "place abbey";
    }

}
