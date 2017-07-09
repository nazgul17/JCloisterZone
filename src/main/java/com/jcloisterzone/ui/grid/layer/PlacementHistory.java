package com.jcloisterzone.ui.grid.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.jcloisterzone.IPlayer;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.event.PlayEvent;
import com.jcloisterzone.event.PlayerTurnEvent;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.ImmutablePoint;
import com.jcloisterzone.ui.UiUtils;
import com.jcloisterzone.ui.grid.GridPanel;

public class PlacementHistory extends AbstractGridLayer {

    private static final Color DEFAULT_COLOR = Color.DARK_GRAY;
    private static final Position ZERO = new Position(0, 0);
    private static final ImmutablePoint POINT = new ImmutablePoint(50,50);
    private static final ImmutablePoint SHADOW_POINT = new ImmutablePoint(51,51);
    private static final AlphaComposite ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f);


    public PlacementHistory(GridPanel gridPanel, GameController gc) {
        super(gridPanel, gc);
        gc.register(this);
    }

    @Override
    public void paint(Graphics2D g) {
        Composite oldComposite = g.getComposite();
        g.setComposite(ALPHA_COMPOSITE);

        IPlayer turnPlayer = getGame().getTurnPlayer();
        int counter = 0;

        boolean breakOnTurnEvent = false;
        boolean turnEventSeen = false;
        Boolean placedCurrentTurn = null;

        for (PlayEvent ev : gc.getGame().getState().getEvents().reverseIterator()) {
            if (ev instanceof PlayerTurnEvent) {
                if (breakOnTurnEvent) break;

                turnEventSeen = true;
                if (placedCurrentTurn == null) placedCurrentTurn = false;

                IPlayer p = ev.getTargetPlayer();
                if (p != null && getGame().getPrevPlayer(p).equals(turnPlayer)) {
                    if (placedCurrentTurn) {
                        break;
                    } else {
                        breakOnTurnEvent = true;
                    }
                }
            }

            if (!(ev instanceof TileEvent)) continue;
            TileEvent te = (TileEvent) ev;
            if (te.getType() != TileEvent.PLACEMENT) continue;

            if (placedCurrentTurn == null && !turnEventSeen) {
                placedCurrentTurn = true;
            }

            Position pos = te.getPosition();
            IPlayer player = te.getTriggeringPlayer();
            String text = String.valueOf(++counter);
            Color color = player != null ?  player.getColors().getFontColor() : DEFAULT_COLOR;

            BufferedImage buf = UiUtils.newTransparentImage(getTileWidth(), getTileHeight());
            Graphics2D gb = (Graphics2D) buf.getGraphics();
            drawAntialiasedTextCentered(gb, text, 80, ZERO, POINT, color, null);
            gb.setComposite(AlphaComposite.DstOver);
            drawAntialiasedTextCentered(gb, text, 80, ZERO, SHADOW_POINT, Color.GRAY, null);
            g.drawImage(buf, null, getOffsetX(pos), getOffsetY(pos));

        }

        g.setComposite(oldComposite);
    }
}
