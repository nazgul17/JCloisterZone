package com.jcloisterzone.game.capability;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.jcloisterzone.Player;
import com.jcloisterzone.XMLUtils;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileTrigger;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.state.GameState;

public class BazaarCapability extends Capability<BazaarCapabilityModel> {

//    private ArrayList<BazaarItem> bazaarSupply;
//    private BazaarItem currentBazaarAuction;
//    private Player bazaarTileSelectingPlayer;
//    private Player bazaarBiddingPlayer;
//    private boolean bazaarTriggered;

    @Override
    public TileDefinition initTile(TileDefinition tile, Element xml) {
        if (xml.getElementsByTagName("bazaar").getLength() > 0) {
            tile = tile.setTileTrigger(TileTrigger.BAZAAR);
        }
        return tile;
    }

    @Override
    public GameState onStartGame(GameState state) {
        return setModel(state, new BazaarCapabilityModel());
    }

/*
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
*/
}