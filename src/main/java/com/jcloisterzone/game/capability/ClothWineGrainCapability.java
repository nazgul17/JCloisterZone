package com.jcloisterzone.game.capability;

import org.w3c.dom.Element;

import com.jcloisterzone.Player;
import com.jcloisterzone.PointCategory;
import com.jcloisterzone.TradeResource;
import com.jcloisterzone.feature.City;
import com.jcloisterzone.feature.Completable;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.GameSettings;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.reducers.AddPoints;

import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;

public class ClothWineGrainCapability extends Capability {

    private static final long serialVersionUID = 1L;

    private final Array<Map<TradeResource, Integer>> tradeResources;

    public ClothWineGrainCapability() {
        this(null);
    }

    public ClothWineGrainCapability(Array<Map<TradeResource, Integer>> tradeResources) {
        this.tradeResources = tradeResources;
    }

    public ClothWineGrainCapability setTradeResources(Array<Map<TradeResource, Integer>> tradeResources) {
        return new ClothWineGrainCapability(tradeResources);
    }

    public Array<Map<TradeResource, Integer>> getTradeResources() {
        return tradeResources;
    }

    @Override
    public GameState onStartGame(GameState state) {
        return state.updateCapability(ClothWineGrainCapability.class, cap ->
            cap.setTradeResources(
                state.getPlayers().getPlayers().map(p -> HashMap.empty())
            )
        );
    }

    @Override
    public GameState onCompleted(GameState state, Completable feature) {
        if (!(feature instanceof City)) return state;

        City city = (City) feature;
        Map<TradeResource, Integer> cityResources = city.getTradeResources();
        if (cityResources.isEmpty()) {
            return state;
        }

        int playerIdx = state.getPlayers().getTurnPlayerIndex();
        state = state.updateCapability(ClothWineGrainCapability.class, cap ->
            cap.setTradeResources(
                cap.getTradeResources().update(playerIdx,
                    res -> res.merge(cityResources, (a, b) -> a+b)
                )
            )
        );

        return state;
    }

    @Override
    public Feature initFeature(GameSettings gs, String tileId, Feature feature, Element xml) {
        if (feature instanceof City && xml.hasAttribute("resource")) {
            City city = (City) feature;
            String val = xml.getAttribute("resource");
            TradeResource res = TradeResource.valueOf(val.toUpperCase());
            return city.setTradeResources(HashMap.of(res, 1));
        }
        return feature;
    }


    @Override
    public GameState finalScoring(GameState state) {
        for (TradeResource tr : TradeResource.values()) {
            int hiVal = 1;
            List<Player> hiPlayers = List.empty();

            for (Player player: state.getPlayers().getPlayers()) {
                int playerValue = player.getTradeResources(state, tr);
                if (playerValue > hiVal) {
                    hiVal = playerValue;
                    hiPlayers = List.of(player);
                } else if (playerValue == hiVal) {
                    hiPlayers.prepend(player);
                }
            }
            for (Player player: hiPlayers) {
                state = (new AddPoints(player, 10, PointCategory.TRADE_GOODS)).apply(state);
            }
        }
        return state;
    }
}
