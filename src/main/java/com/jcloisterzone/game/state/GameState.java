package com.jcloisterzone.game.state;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Function;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.state.mixins.ActionsStateMixin;
import com.jcloisterzone.game.state.mixins.BoardMixin;
import com.jcloisterzone.game.state.mixins.CapabilitiesMixin;
import com.jcloisterzone.game.state.mixins.EventsStateMixin;
import com.jcloisterzone.game.state.mixins.FlagsStateMixin;
import com.jcloisterzone.game.state.mixins.PlacementsMixin;
import com.jcloisterzone.game.state.mixins.PlayersStsteMixin;
import com.jcloisterzone.game.state.mixins.RulesMixin;

import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Queue;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;

@Immutable
public class GameState implements ActionsStateMixin, BoardMixin,
        RulesMixin, CapabilitiesMixin, PlayersStsteMixin, EventsStateMixin,
        FlagsStateMixin, PlacementsMixin, Serializable {

    private static final long serialVersionUID = 1L;

    //TODO group some fields into sub states

    private final Map<CustomRule, Object> rules;

    private final CapabilitiesState capabilities;
    private final PlayersState players;

    private final TilePack tilePack;
    private final TileDefinition drawnTile;

    private final LinkedHashMap<Position, PlacedTile> placedTiles;
    private final List<TileDefinition> discardedTiles;
    private final Map<FeaturePointer, Feature> featureMap;

    private final NeutralFiguresState neutralFigures;
    private final LinkedHashMap<Meeple, FeaturePointer> deployedMeeples;

    //Flags for marking once per turn actions (like princess, portal, ransom ...)
    private final Set<Flag> flags;

    private final ActionsState playerActions;
    private final Queue<PlayEvent> events;

    private final Class<? extends Phase> phase;

    public static GameState createInitial(
            Map<CustomRule, Object> rules,
            Seq<Capability<?>> capabilities,
            Array<Player> players,
            int turnPlayerIndex) {
        return new GameState(
            rules,
            CapabilitiesState.createInitial(capabilities),
            PlayersState.createInitial(players, turnPlayerIndex),
            null,
            null,
            LinkedHashMap.empty(),
            List.empty(),
            HashMap.empty(),
            new NeutralFiguresState(),
            LinkedHashMap.empty(),
            null,
            HashSet.empty(),
            Queue.empty(),
            null
        );
    }



    public GameState(
            Map<CustomRule, Object> rules,
            CapabilitiesState capabilities,
            PlayersState players,
            TilePack tilePack, TileDefinition drawnTile,
            LinkedHashMap<Position, PlacedTile> placedTiles,
            List<TileDefinition> discardedTiles, Map<FeaturePointer, Feature> featureMap,
            NeutralFiguresState neutralFigures,
            LinkedHashMap<Meeple, FeaturePointer> deployedMeeples,
            ActionsState playerActions,
            Set<Flag> flags,
            Queue<PlayEvent> events,
            Class<? extends Phase> phase) {
        this.rules = rules;
        this.capabilities = capabilities;
        this.players = players;
        this.tilePack = tilePack;
        this.drawnTile = drawnTile;
        this.placedTiles = placedTiles;
        this.discardedTiles = discardedTiles;
        this.featureMap = featureMap;
        this.neutralFigures = neutralFigures;
        this.deployedMeeples = deployedMeeples;
        this.playerActions = playerActions;
        this.flags = flags;
        this.events = events;
        this.phase = phase;
    }

    @Override
    public GameState setCapabilities(CapabilitiesState capabilities) {
        if (capabilities == this.capabilities) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    @Override
    public GameState setPlayers(PlayersState players) {
        if (players == this.players) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState setTilePack(TilePack tilePack) {
        if (tilePack == this.tilePack) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState mapTilePack(Function<TilePack, TilePack> fn) {
        return setTilePack(fn.apply(tilePack));
    }

    public GameState setDrawnTile(TileDefinition drawnTile) {
        if (drawnTile == this.drawnTile) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    @Override
    public GameState setPlacedTiles(LinkedHashMap<Position, PlacedTile> placedTiles) {
        if (placedTiles == this.placedTiles) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    @Override
    public GameState setFeatureMap(Map<FeaturePointer, Feature> featureMap) {
        if (featureMap == this.featureMap) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState setDiscardedTiles(List<TileDefinition> discardedTiles) {
        if (discardedTiles == this.discardedTiles) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState setNeutralFigures(NeutralFiguresState neutralFigures) {
        if (neutralFigures == this.neutralFigures) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState mapNeutralFigures(Function<NeutralFiguresState, NeutralFiguresState> fn) {
        return setNeutralFigures(fn.apply(neutralFigures));
    }

    public GameState setDeployedMeeples(LinkedHashMap<Meeple, FeaturePointer> deployedMeeples) {
        if (deployedMeeples == this.deployedMeeples) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState setPlayerActions(ActionsState playerActions) {
        if (playerActions == this.playerActions) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    @Override
    public GameState setFlags(Set<Flag> flags) {
        if (flags == this.flags) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    @Override
    public GameState setEvents(Queue<PlayEvent> events) {
        if (events == this.events) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState setPhase(Class<? extends Phase> phase) {
        if (phase == this.phase) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            featureMap, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    @Override
    public Map<CustomRule, Object> getRules() {
        return rules;
    }

    @Override
    public CapabilitiesState getCapabilities() {
        return capabilities;
    }

    public PlayersState getPlayers() {
        return players;
    }

    public TilePack getTilePack() {
        return tilePack;
    }

    public TileDefinition getDrawnTile() {
        return drawnTile;
    }

    @Override
    public LinkedHashMap<Position, PlacedTile> getPlacedTiles() {
        return placedTiles;
    }

    public List<TileDefinition> getDiscardedTiles() {
        return discardedTiles;
    }

    @Override
    public Map<FeaturePointer, Feature> getFeatureMap() {
        return featureMap;
    }

    public NeutralFiguresState getNeutralFigures() {
        return neutralFigures;
    }

    public LinkedHashMap<Meeple, FeaturePointer> getDeployedMeeples() {
        return deployedMeeples;
    }

    @Override
    public ActionsState getPlayerActions() {
        return playerActions;
    }

    @Override
    public Set<Flag> getFlags() {
        return flags;
    }

    @Override
    public Queue<PlayEvent> getEvents() {
        return events;
    }

    public Class<? extends Phase> getPhase() {
        return phase;
    }
}
