package com.jcloisterzone.game.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.TileEvent;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;

public class BazaarCapability extends Capability {

    private ArrayList<BazaarItem> bazaarSupply;
    private BazaarItem currentBazaarAuction;
    private Player bazaarTileSelectingPlayer;
    private Player bazaarBiddingPlayer;
    private boolean bazaarTriggered;

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("bazaar").getLength() > 0) {
            tile.setTrigger(TileTrigger.BAZAAR);
        }
    }

    @Override
    public void saveToSnapshot(Document doc, Element node) {
        if (bazaarTriggered) {
            node.setAttribute("bazaar-triggered", "true");
        }
        if (bazaarSupply != null) {
            for (BazaarItem bi : bazaarSupply) {
                Element el = doc.createElement("bazaar-supply");
                el.setAttribute("tile", bi.getTile().getId());
                if (bi.getOwner() != null) el.setAttribute("owner", ""+bi.getOwner().getIndex());
                if (bi.getCurrentBidder() != null) el.setAttribute("bidder", ""+bi.getCurrentBidder().getIndex());
                el.setAttribute("price", ""+bi.getCurrentPrice());

                if (currentBazaarAuction == bi) {
                    el.setAttribute("selected", "true");
                }
                node.appendChild(el);
            }
        }
        if (bazaarTileSelectingPlayer != null) {
            Element el = doc.createElement("bazaar-selecting-player");
            el.setAttribute("player", ""+bazaarTileSelectingPlayer.getIndex());
            node.appendChild(el);
        }
        if (bazaarBiddingPlayer != null) {
            Element el = doc.createElement("bazaar-bidding-player");
            el.setAttribute("player", ""+bazaarBiddingPlayer.getIndex());
            node.appendChild(el);
        }
    }



    @Override
    public void loadFromSnapshot(Document doc, Element node) {
        bazaarTriggered = XMLUtils.attributeBoolValue(node,"bazaar-triggered");
        NodeList nl = node.getElementsByTagName("bazaar-supply");
        if (nl.getLength() > 0) {
            bazaarSupply = new ArrayList<BazaarItem>(nl.getLength());
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                Tile tile = game.getTilePack().drawTile(el.getAttribute("tile"));
                BazaarItem bi = new BazaarItem(tile);
                bazaarSupply.add(bi);
                if (el.hasAttribute("owner")) bi.setOwner(game.getPlayer(Integer.parseInt(el.getAttribute("owner"))));
                if (el.hasAttribute("bidder")) bi.setCurrentBidder(game.getPlayer(Integer.parseInt(el.getAttribute("bidder"))));
                bi.setCurrentPrice(XMLUtils.attributeIntValue(el, "price"));
                if (XMLUtils.attributeBoolValue(el, "selected")) {
                    currentBazaarAuction = bi;
                }
            }
        }

        nl = node.getElementsByTagName("bazaar-selecting-player");
        if (nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            bazaarTileSelectingPlayer = game.getPlayer(Integer.parseInt(el.getAttribute("player")));
        }
        nl = node.getElementsByTagName("bazaar-bidding-player");
        if (nl.getLength() > 0) {
            Element el = (Element) nl.item(0);
            bazaarBiddingPlayer = game.getPlayer(Integer.parseInt(el.getAttribute("player")));
        }
    }

    public ArrayList<BazaarItem> getBazaarSupply() {
        return bazaarSupply;
    }

    public void setBazaarSupply(ArrayList<BazaarItem> bazaarSupply) {
        this.bazaarSupply = bazaarSupply;
    }

    public Player getBazaarTileSelectingPlayer() {
        return bazaarTileSelectingPlayer;
    }

    public void setBazaarTileSelectingPlayer(Player bazaarTileSelectingPlayer) {
        this.bazaarTileSelectingPlayer = bazaarTileSelectingPlayer;
    }

    public Player getBazaarBiddingPlayer() {
        return bazaarBiddingPlayer;
    }

    public void setBazaarBiddingPlayer(Player bazaarBiddingPlayer) {
        this.bazaarBiddingPlayer = bazaarBiddingPlayer;
    }

    public BazaarItem getCurrentBazaarAuction() {
        return currentBazaarAuction;
    }

    public void setCurrentBazaarAuction(BazaarItem currentBazaarAuction) {
        this.currentBazaarAuction = currentBazaarAuction;
    }

    public boolean isBazaarTriggered() {
        return bazaarTriggered;
    }

    @Override
    public void turnCleanUp() {
        bazaarTriggered = false;
    }

    @Override
    public void handleEvent(Event event) {
       if (event instanceof TileEvent) {
           tileDrawn((TileEvent) event);
       }

    }

    private void tileDrawn(TileEvent ev) {
        if (ev.getType() == TileEvent.DRAW && ev.getTile().hasTrigger(TileTrigger.BAZAAR)) {
            bazaarTriggered = true;
        }
    }

    public boolean hasTileAuctioned(Player p) {
        for (BazaarItem bi : bazaarSupply) {
            if (bi.getOwner() == p) return true;
        }
        return false;
    }

    public Tile drawNextTile() {
        if (bazaarSupply == null) return null;
        Player p = game.getActivePlayer();
        Tile tile = null;
        BazaarItem currentItem = null;
        for (BazaarItem bi : bazaarSupply) {
            if (bi.getOwner() == p) {
                currentItem = bi;
                tile = bi.getTile();
                break;
            }
        }
        bazaarSupply.remove(currentItem);
        if (bazaarSupply.isEmpty()) {
            bazaarSupply = null;
        }
        return tile;
    }

    public List<TileDefinition> getDrawQueue() {
        if (bazaarSupply == null) return Collections.emptyList();
        List<Tile> result = new ArrayList<>();
        Player turnPlayer = game.getTurnPlayer();
        Player p = game.getNextPlayer(turnPlayer);
        while (p != turnPlayer) {
            for (BazaarItem bi : bazaarSupply) {
                if (bi.getOwner() == p) {
                    result.add(bi.getTile());
                    break;
                }
            }
            p = game.getNextPlayer(p);
        }
        return result;
    }

}