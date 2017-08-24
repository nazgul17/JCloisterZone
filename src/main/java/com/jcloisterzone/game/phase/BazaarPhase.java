package com.jcloisterzone.game.phase;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.action.BazaarBidAction;
import com.jcloisterzone.action.BazaarSelectBuyOrSellAction;
import com.jcloisterzone.action.BazaarSelectTileAction;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.capability.BazaarCapability;
import com.jcloisterzone.game.capability.BazaarCapabilityModel;
import com.jcloisterzone.game.capability.BazaarItem;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.Flag;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.AddPoints;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.BazaarBidMessage;
import com.jcloisterzone.wsio.message.BazaarBuyOrSellMessage;
import com.jcloisterzone.wsio.message.BazaarBuyOrSellMessage.BuyOrSellOption;
import com.jcloisterzone.wsio.message.PassMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Queue;

@RequiredCapability(BazaarCapability.class)
public class BazaarPhase extends Phase {

    public BazaarPhase(GameController gc) {
        super(gc);
    }

    @Override
    public void enter(GameState state) {
        if (!state.hasFlag(Flag.BAZAAR_AUCTION)) {
            next(state);
            return;
        }

        int size = state.getPlayers().length();
        TilePack tilePack = state.getTilePack();

        if (tilePack.size() < size) {
            next(state);
            return;
        }

        Queue<BazaarItem> supply = Queue.empty();

        for (int i = 0; i < size; i++) {
            int rndIndex = game.getRandom().nextInt(tilePack.size());
            Tuple2<TileDefinition, TilePack> t = tilePack.drawTile(rndIndex);
            state = state.setTilePack(t._2);
            supply = supply.append(new BazaarItem(t._1, 0, null, null));
        }

        Player player = state.getTurnPlayer().getNextPlayer(state);
        BazaarCapabilityModel model = new BazaarCapabilityModel(supply, null, player);

        state = state.setCapabilityModel(BazaarCapability.class, model);

        BazaarSelectTileAction action = new BazaarSelectTileAction(supply.toLinkedSet());
        state = state.setPlayerActions(
            new ActionsState(player, action, false)
        );
        toggleClock(player);
        promote(state);
    }

    private boolean hasTileAssigned(BazaarCapabilityModel model, Player p) {
        for (BazaarItem bi : model.getSupply()) {
            if (p.equals(bi.getOwner())) return true;
        }
        return false;
    }

    @WsSubscribe
    public void bazaarBid(BazaarBidMessage msg) {
        int supplyIndex = msg.getSupplyIndex();
        int price = msg.getPrice();

        game.clearUndo();
        GameState state = game.getState();
        boolean noAuction = state.getBooleanValue(CustomRule.BAZAAR_NO_AUCTION);

        Player player = state.getActivePlayer();
        PlayerAction<?> action = state.getPlayerActions().getActions().get();
        boolean isTileSelection = action instanceof BazaarSelectTileAction;

        state = state.mapCapabilityModel(BazaarCapability.class, model -> {
            BazaarItem item = model.getAuctionedItem();

            if (isTileSelection) {
                assert item == null;

                item = model.getSupply().get(supplyIndex);
                model = model.setAuctionedItemIndex(supplyIndex);
                if (noAuction) {
                    assert item.getCurrentPrice() == 0;
                    item = item.setOwner(player);
                    model = model.updateSupplyItem(supplyIndex, item);
                    return model;
                }
            }

            item = item.setCurrentPrice(price);
            item = item.setCurrentBidder(player);
            model = model.updateSupplyItem(supplyIndex, item);

            return model;
        });

        if (noAuction) {
            nextSelectingPlayer(state);
        } else {
            nextBidder(state);
        }
    }

    private void nextBidder(GameState state) {
        Player nextBidder = state.getActivePlayer();
        BazaarCapabilityModel model = state.getCapabilityModel(BazaarCapability.class);
        BazaarItem item = model.getAuctionedItem();
        Player tileSelectingPlayer = model.getTileSelectingPlayer();

        do {
            nextBidder = nextBidder.getNextPlayer(state);
            if (nextBidder.equals(tileSelectingPlayer)) {
                //all players makes bid
                if (tileSelectingPlayer.equals(item.getCurrentBidder())) {
                    buyOrSell(state, BuyOrSellOption.BUY);
                } else {
                    BazaarSelectBuyOrSellAction action = new BazaarSelectBuyOrSellAction();
                    ActionsState as = new ActionsState(nextBidder, action, false);
                    state = state.setPlayerActions(as);

                    toggleClock(nextBidder);
                    promote(state);
                }
                return;
            }
        } while (hasTileAssigned(model, nextBidder));

        BazaarBidAction action = new BazaarBidAction();
        ActionsState as = new ActionsState(nextBidder, action, false);
        state = state.setPlayerActions(as);

        toggleClock(nextBidder);
        promote(state);
    }

    private void nextSelectingPlayer(GameState state) {
        BazaarCapabilityModel model = state.getCapabilityModel(BazaarCapability.class);
        Player currentSelectingPlayer = model.getTileSelectingPlayer();
        Player player = currentSelectingPlayer;

        model = model.setAuctionedItemIndex(null);

        do {
            player = player.getNextPlayer(state);
            if (!hasTileAssigned(model, player)) {
                model = model.setTileSelectingPlayer(player);

                state = state.setCapabilityModel(BazaarCapability.class, model);

                BazaarSelectTileAction action = new BazaarSelectTileAction(model.getSupply().toLinkedSet());
                state = state.setPlayerActions(
                    new ActionsState(player, action, false)
                );
                toggleClock(player);
                promote(state);
                return;
            }
        } while (player != currentSelectingPlayer);

        //all tiles has been auctioned
        Queue<BazaarItem> supply =  model.getSupply();

        model = model.setSupply(
            state.getPlayers().getPlayersBeginWith(
                state.getTurnPlayer().getNextPlayer(state)
            )
            .map(p ->
                supply.find(bi -> bi.getOwner().equals(p)).get()
            )
            .toQueue()
        );
        model = model.setAuctionedItemIndex(null);
        model = model.setTileSelectingPlayer(null);

        state = state.setCapabilityModel(BazaarCapability.class, model);
        next(state);
    }

    @WsSubscribe
    public void handlePass(PassMessage msg) {
        GameState state = game.getState();
        BazaarCapabilityModel model = state.getCapabilityModel(BazaarCapability.class);
        Player p = state.getActivePlayer();

        if (p.equals(model.getTileSelectingPlayer())) {
            logger.error("Tile selecting player is not allowed to pass");
            return;
        }
        nextBidder(state);
    }

    @WsSubscribe
    public void handleBazaarBuyOrSellMessage(BazaarBuyOrSellMessage msg) {
        buyOrSell(game.getState(), msg.getValue());
    }

    private void buyOrSell(GameState state, BuyOrSellOption option) {
        BazaarCapabilityModel model = state.getCapabilityModel(BazaarCapability.class);

        BazaarItem bi = model.getAuctionedItem();
        int points = bi.getCurrentPrice();
        Player pSelecting = model.getTileSelectingPlayer();
        Player pBidding = bi.getCurrentBidder();

        assert !pSelecting.equals(pBidding) || option == BuyOrSellOption.BUY; //if same, buy is flag expected
        if (option == BuyOrSellOption.SELL) points *= -1;

        state = (new AddPoints(pSelecting, -points, PointCategory.BAZAAR_AUCTION)).apply(state);
        if (!pSelecting.equals(pBidding)) {
            state = (new AddPoints(pBidding, points, PointCategory.BAZAAR_AUCTION)).apply(state);
        }

        bi = bi.setOwner(option == BuyOrSellOption.BUY ? pSelecting : pBidding);
        bi = bi.setCurrentBidder(null);

        model = model.updateSupplyItem(model.getAuctionedItemIndex(), bi);
        state = state.setCapabilityModel(BazaarCapability.class, model);

        nextSelectingPlayer(state);
    }
}
