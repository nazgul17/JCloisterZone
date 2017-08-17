package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.play.BridgePlaced;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.grid.GridPanel;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

public class BridgeLayer extends AbstractGridLayer {

    private static final AlphaComposite BRIDGE_FILL_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f);

    /** bridges */
    private Set<FeaturePointer> model = HashSet.empty();

    private MeepleLayer meepleLayer;

    public BridgeLayer(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        gc.register(this);
    }

    @Subscribe
    public void handleGameChanged(GameChangedEvent ev) {
        boolean bridgesChanged = false;
        for (PlayEvent pe : ev.getPlayEventsSymmetricDifference()) {
            if (pe instanceof BridgePlaced) {
                bridgesChanged = true;
                break;
            }
        }

        if (bridgesChanged) {
            model = createModel(ev.getCurrentState());
        }
    }

    private Set<FeaturePointer> createModel(GameState state) {
        return state.getCapabilities().getModel(BridgeCapability.class);
    }

    @Override
    public void paint(Graphics2D g2) {
        Composite oldComposite = g2.getComposite();
        Board board = getGame().getBoard();
        for (FeaturePointer bridge : model) {
            Position pos = bridge.getPosition();
            Location loc = bridge.getLocation();
            Tile tile = board.get(pos);

            Area a = rm.getBridgeArea(tile, getTileWidth(), getTileHeight(), loc).getTrackingArea();
            a.transform(AffineTransform.getTranslateInstance(getOffsetX(pos), getOffsetY(pos)));

            g2.setColor(Color.BLACK);
            g2.setComposite(BRIDGE_FILL_COMPOSITE);
            g2.fill(a);

        }
        g2.setComposite(oldComposite);

        meepleLayer.paintMeeplesOnBridges(g2);
    }


    public MeepleLayer getMeepleLayer() {
        return meepleLayer;
    }

    public void setMeepleLayer(MeepleLayer meepleLayer) {
        this.meepleLayer = meepleLayer;
    }
}
