package com.jcloisterzone.game.capability;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.action.TunnelAction;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.TunnelPiecePlacedEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.feature.Road;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.ui.Client;


public final class TunnelCapability extends Capability<Void> {

    private Road placedTunnelCurrentTurn;

    private final Map<Player, Integer> tunnelTokensA = new HashMap<>();
    private final Map<Player, Integer> tunnelTokensB = new HashMap<>();

    private final List<Road> tunnels = new ArrayList<>();

    public TunnelCapability() {
        for (PlayerSlot slot : game.getPlayerSlots()) {
            if (!slot.isOccupied()) continue;
            int slotNumber = (slot.getNumber() + 2) % PlayerSlot.COUNT;
            if (game.getPlayerSlots()[slotNumber].isOccupied()) {
                slotNumber = (slotNumber + 1) % PlayerSlot.COUNT;
            }
            PlayerSlot fakeSlot = new PlayerSlot(slotNumber);
            //HACK to get second color - TODO fix it!
            Color tunnelBColor = Client.getInstance().getConfig().getPlayerColor(fakeSlot).getMeepleColor();
            slot.getColors().setTunnelBColor(tunnelBColor);

        }
    }


    @Override
    public void initPlayer(Player player) {
        tunnelTokensA.put(player, 2);
        tunnelTokensB.put(player, game.getAllPlayers().length() <= 2 ? 2 : 0);
    }

    @Override
    public Feature initFeature(GameState state, String tileId, Feature feature, Element xml) {
        if (!(feature instanceof Road)) return;
        Road road = (Road) feature;
        if (road.isTunnelEnd()) {
            tunnels.add(road);
        }
    }

    public List<Road> getOpenTunnels() {
        List<Road> openTunnels = new ArrayList<>();
        for (Road road : tunnels) {
            if (road.getTile().getPosition() != null && road.isTunnelOpen()) {
                openTunnels.add(road);
            }
        }
        return openTunnels;
    }

    public int getTunnelTokens(Player player, boolean isB) {
        Map<Player, Integer> map = isB ? tunnelTokensB : tunnelTokensA;
        return map.get(player);
    }

    public void decreaseTunnelTokens(Player player, boolean isB) {
        Map<Player, Integer> map = isB ? tunnelTokensB : tunnelTokensA;
        int tokens = map.get(player);
        if (tokens == 0) throw new IllegalStateException("Player has no tunnel token");
        map.put(player, tokens-1);
    }

    @Override
    public void prepareActions(List<PlayerAction<?>> actions, Set<FeaturePointer> followerOptions) {
        if (isTunnelUsedThisTurn()) return;
        List<Road> openTunnels = getOpenTunnels();
        if (openTunnels.isEmpty()) return;

        List<TunnelAction> tunnelActions = new ArrayList<>(2);
        if (getTunnelTokens(game.getActivePlayer(), false) > 0) {
            tunnelActions.add(new TunnelAction(false));
        }
        if (getTunnelTokens(game.getActivePlayer(), true) > 0) {
            tunnelActions.add(new TunnelAction(true));
        }
        for (TunnelAction ta : tunnelActions) {
            for (Road tunnelEnd : openTunnels) {
                ta.add(new FeaturePointer(tunnelEnd.getTile().getPosition(), tunnelEnd.getLocation()));
            }
        }
        actions.addAll(tunnelActions);
    }

    public boolean isTunnelUsedThisTurn() {
        return placedTunnelCurrentTurn != null;
    }
    public Road getPlacedTunnel() {
        return placedTunnelCurrentTurn;
    }

    @Override
    public void turnCleanUp() {
        placedTunnelCurrentTurn = null;
    }

    private int getTunnelId(Player p, boolean isB) {
        return p.getIndex() + (isB ? 100 : 0);
    }

    public void placeTunnelPiece(FeaturePointer fp, boolean isB) {
        Road road = (Road) getBoard().getPlayer(fp);
        if (!road.isTunnelOpen()) {
            throw new IllegalStateException("No open tunnel here.");
        }
        placedTunnelCurrentTurn = road;
        Player player = game.getActivePlayer();
        placeTunnelPiece(road, player, fp, isB);
    }

    private void placeTunnelPiece(Road road, Player player, FeaturePointer fp, boolean isB) {
        int connectionId = getTunnelId(player, isB);
        decreaseTunnelTokens(player, isB);
        for (Road r : tunnels) {
            if (r.getTunnelEnd() == connectionId) {
                r.setTunnelEdge(road);
                road.setTunnelEdge(r);
                break;
            }
        }
        road.setTunnelEnd(connectionId);
        game.post(new TunnelPiecePlacedEvent(player, fp, isB));
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (placedTunnelCurrentTurn != null) {
            Element el = doc.createElement("placed-tunnel");
            XMLUtils.injectPosition(el, placedTunnelCurrentTurn.getTile().getPosition());
            el.setAttribute("location", placedTunnelCurrentTurn.getLocation().toString());
            node.appendChild(el);
        }
        for (Road tunnel : tunnels) {
            if (tunnel.getTile().getPosition() != null && tunnel.getTunnelEnd() != Road.OPEN_TUNNEL) {
                Element el = doc.createElement("tunnel");
                node.appendChild(el);
                XMLUtils.injectFeaturePoiner(el, new FeaturePointer(tunnel));
                el.setAttribute("player", "" + (tunnel.getTunnelEnd() % 100));
                el.setAttribute("b", tunnel.getTunnelEnd() > 100 ? "yes" : "no");
            }
        }
    }

    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        NodeList nl = node.getElementsByTagName("placed-tunnel");
        if (nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            placedTunnelCurrentTurn = (Road) getBoard().getPlayer(XMLUtils.extractPosition(el)).getFeature(Location.valueOf(el.getAttribute("location")));
        }
        nl = node.getElementsByTagName("tunnel");
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            FeaturePointer fp = XMLUtils.extractFeaturePointer(el);
            Road road = (Road) getBoard().getPlayer(fp);
            if (!road.isTunnelEnd()) {
                logger.error("Tunnel end does not exist.");
                continue;
            }
            Player player = game.getPlayer(Integer.parseInt(el.getAttribute("player")));
            boolean isB = "yes".equals(el.getAttribute("b"));
            placeTunnelPiece(road, player, fp, isB);
        }
    }

}
