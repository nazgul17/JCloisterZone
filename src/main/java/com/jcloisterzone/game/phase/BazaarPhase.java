package com.jcloisterzone.game.phase;

import java.util.ArrayList;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.action.BazaarSelectTileAction;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePackState;
import com.jcloisterzone.event.BazaarAuctionEndEvent;
import com.jcloisterzone.event.BazaarMakeBidEvent;
import com.jcloisterzone.event.BazaarSelectBuyOrSellEvent;
import com.jcloisterzone.event.BazaarSelectTileEvent;
import com.jcloisterzone.event.BazaarTileSelectedEvent;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarCapabilityModel;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameState.Flag;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.PassMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Queue;

public class BazaarPhase extends ServerAwarePhase {

    public BazaarPhase(Game game, GameController controller) {
        super(game, controller);
    }

    @Override
    public boolean isActive(CapabilitiesState capabilities) {
        return capabilities.contains(BazaarCapability.class);
    }

//    @Override
//    public Player getActivePlayer() {
//        Player bidding =  bazaarCap.getBazaarBiddingPlayer();
//        return bidding == null ? bazaarCap.getBazaarTileSelectingPlayer() : bidding;
//    }

    @Override
    public void enter(GameState state) {
        if (!state.hasFlag(Flag.BAZAAR_AUCTION)) {
            next(state);
            return;
        }

        int size = state.getPlayers().length();
        TilePackState tilePack = state.getTilePack();

        if (tilePack.size() < size) {
            next(state);
            return;
        }

        Queue<BazaarItem> supply = Queue.empty();

        for (int i = 0; i < size; i++) {
            int rndIndex = game.getRandom().nextInt(tilePack.size());
            Tuple2<TileDefinition, TilePackState> t = tilePack.drawTile(rndIndex);
            state = state.setTilePack(t._2);
            supply = supply.append(new BazaarItem(t._1, 0, null, null));
        }

        Player player = state.getTurnPlayer().getNextPlayer(state);
        BazaarCapabilityModel model = new BazaarCapabilityModel(supply, null, player);

        state = state.setCapabilityModel(BazaarCapability.class, model);

        toggleClock(player);
        BazaarSelectTileAction action = new BazaarSelectTileAction(supply.toLinkedSet());
        state = state.setPlayerActions(
            new ActionsState(player, action, false)
        );
        promote(state);
    }

//
//
//    private boolean isBazaarTriggered() {
//        if (!bazaarCap.isBazaarTriggered()) return false;
//        if (getTilePack().size() < game.getAllPlayers().length()) return false; //there isn't one tile for each player available
//        if (bazaarCap.getBazaarSupply() != null) return false;
//        return true;
//    }

//    private boolean canPlayerBid(Player p) {
//        for (BazaarItem bi : bazaarCap.getBazaarSupply()) {
//            if (bi.getOwner() == p) return false;
//        }
//        return true;
//    }

    @Override
    public void bazaarBid(Integer supplyIndex, Integer price) {
        BazaarItem bi = bazaarCap.getCurrentBazaarAuction();
        boolean isTileSelection = bi == null;
        if (bi == null) {
            bi = bazaarCap.getBazaarSupply().get(supplyIndex);
            bazaarCap.setCurrentBazaarAuction(bi);

            if (game.getBooleanValue(CustomRule.BAZAAR_NO_AUCTION)) {
                bi.setOwner(getActivePlayer());
                nextSelectingPlayer();
                return;
            }
        }
        bi.setCurrentPrice(price);
        bi.setCurrentBidder(getActivePlayer());

        if (isTileSelection) {
            toggleClock(getActivePlayer());
            game.post(new BazaarTileSelectedEvent(getActivePlayer(), bi, supplyIndex));
        }
        nextBidder();
    }

    private void nextBidder() {
        Player nextBidder = getActivePlayer();
        BazaarItem currentItem = bazaarCap.getCurrentBazaarAuction();
        int supplyIdx = bazaarCap.getBazaarSupply().indexOf(currentItem);
        do {
            nextBidder = game.getNextPlayer(nextBidder);
            if (nextBidder == bazaarCap.getBazaarTileSelectingPlayer()) {
                //all players makes bid
                BazaarItem bi = bazaarCap.getCurrentBazaarAuction();
                if (bazaarCap.getBazaarTileSelectingPlayer() == bi.getCurrentBidder()) {
                    bazaarBuyOrSell(true);
                } else {
                    bazaarCap.setBazaarBiddingPlayer(bazaarCap.getBazaarTileSelectingPlayer()); //need for correct save&load
                    toggleClock(getActivePlayer());
                    game.post(new BazaarSelectBuyOrSellEvent(getActivePlayer(), currentItem, supplyIdx));
                }
                return;
            }
        } while (!canPlayerBid(nextBidder));

        bazaarCap.setBazaarBiddingPlayer(nextBidder);
        toggleClock(getActivePlayer());
        game.post(new BazaarMakeBidEvent(getActivePlayer(), currentItem, supplyIdx));
    }

    private void nextSelectingPlayer() {
        bazaarCap.setCurrentBazaarAuction(null);
        bazaarCap.setBazaarBiddingPlayer(null);
        Player currentSelectingPlayer = bazaarCap.getBazaarTileSelectingPlayer();
        Player player = currentSelectingPlayer;
        do {
            player = game.getNextPlayer(player);
            if (!bazaarCap.hasTileAuctioned(player)) {
                bazaarCap.setBazaarTileSelectingPlayer(player);
                toggleClock(getActivePlayer());
                game.post(new BazaarSelectTileEvent(getActivePlayer(), bazaarCap.getBazaarSupply()));
                return;
            }
        } while (player != currentSelectingPlayer);
        //all tiles has been auctioned
        bazaarCap.setBazaarTileSelectingPlayer(null);
        game.post(new BazaarAuctionEndEvent());
        next();
    }

    @WsSubscribe
    public void handlePass(PassMessage msg) {
        if (bazaarCap.getBazaarBiddingPlayer() == bazaarCap.getBazaarTileSelectingPlayer()) {
            logger.error("Tile selecting player is not allowed to pass");
            return;
        }
        nextBidder();
    }

    @Override
    public void bazaarBuyOrSell(boolean buy) {
        BazaarItem bi = bazaarCap.getCurrentBazaarAuction();
        int points = bi.getCurrentPrice();
        Player pSelecting = bazaarCap.getBazaarTileSelectingPlayer();
        Player pBidding = bi.getCurrentBidder();

        assert pSelecting != pBidding || buy; //if same, buy is flag expected
        if (!buy) points *= -1;
        pSelecting.addPoints(-points, PointCategory.BAZAAR_AUCTION);
        if (pSelecting != pBidding) {
            pBidding.addPoints(points, PointCategory.BAZAAR_AUCTION);
        }

        bi.setOwner(buy ? pSelecting : pBidding);
        bi.setCurrentBidder(null);
        nextSelectingPlayer();
    }


}
