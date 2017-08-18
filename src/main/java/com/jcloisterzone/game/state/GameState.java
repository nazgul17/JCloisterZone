package com.jcloisterzone.game.state;

import java.io.Serializable;
import java.util.function.Function;

import com.jcloisterzone.Immutable;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.PlayerScore;
import com.jcloisterzone.action.ActionsState;
import com.jcloisterzone.board.Board;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TilePackState;
import com.jcloisterzone.board.pointer.BoardPointer;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.play.PlayerTurnEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.Meeple;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.figure.neutral.NeutralFigure;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.game.phase.Phase;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Queue;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.collection.Vector;

@Immutable
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    //TODO group some fields into sub states

    private final HashMap<CustomRule, Object> rules;

    private final CapabilitiesState capabilities;
    private final PlayersState players;

    private final TilePackState tilePack;
    private final TileDefinition drawnTile;

    private final LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles;
    private final List<TileDefinition> discardedTiles;
    private final Map<FeaturePointer, Feature> features;

    private final NeutralFiguresState neutralFigures;
    private final LinkedHashMap<Meeple, FeaturePointer> deployedMeeples;

    //Flags for marking once per turn actions (like princess, portal, ransom ...)
    private final Set<Flag> flags;

    private final ActionsState playerActions;
    private final Queue<PlayEvent> events;

    private final Class<? extends Phase> phase;

    public static GameState createInitial(
            HashMap<CustomRule, Object> rules,
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
            HashMap<CustomRule, Object> rules,
            CapabilitiesState capabilities,
            PlayersState players,
            TilePackState tilePack, TileDefinition drawnTile,
            LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles,
            List<TileDefinition> discardedTiles, Map<FeaturePointer, Feature> features,
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
        this.features = features;
        this.neutralFigures = neutralFigures;
        this.deployedMeeples = deployedMeeples;
        this.playerActions = playerActions;
        this.flags = flags;
        this.events = events;
        this.phase = phase;
    }

    public GameState setCapabilities(CapabilitiesState capabilities) {
        if (capabilities == this.capabilities) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState updateCapabilities(Function<CapabilitiesState, CapabilitiesState> fn) {
        return setCapabilities(fn.apply(capabilities));
    }

    public <M> GameState updateCapabilityModel(Class<? extends Capability<M>> cls, Function<M, M> fn) {
        return setCapabilities(getCapabilities().updateModel(cls, fn));
    }

    public <M> GameState setCapabilityModel(Class<? extends Capability<M>> cls, M model) {
        return setCapabilities(getCapabilities().setModel(cls, model));
    }

    public GameState setPlayers(PlayersState players) {
        if (players == this.players) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState updatePlayers(Function<PlayersState, PlayersState> fn) {
        return setPlayers(fn.apply(players));
    }

    public GameState setTilePack(TilePackState tilePack) {
        if (tilePack == this.tilePack) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState setDrawnTile(TileDefinition drawnTile) {
        if (drawnTile == this.drawnTile) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState setPlacedTiles(LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> placedTiles) {
        if (placedTiles == this.placedTiles) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState setFeatures(Map<FeaturePointer, Feature> features) {
        if (features == this.features) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, neutralFigures,
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
            features, neutralFigures,
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
            features, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState updateNeutralFigures(Function<NeutralFiguresState, NeutralFiguresState> fn) {
        return setNeutralFigures(fn.apply(neutralFigures));
    }

    public GameState setDeployedMeeples(LinkedHashMap<Meeple, FeaturePointer> deployedMeeples) {
        if (deployedMeeples == this.deployedMeeples) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, neutralFigures,
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
            features, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState setFlags(Set<Flag> flags) {
        if (flags == this.flags) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState addFlag(Flag flag) {
        //HashSet makes contains check and returns same instance, no need to do it again here
        return setFlags(flags.add(flag));
    }

    public GameState setEvents(Queue<PlayEvent> events) {
        if (events == this.events) return this;
        return new GameState(
            rules, capabilities, players,
            tilePack, drawnTile, placedTiles, discardedTiles,
            features, neutralFigures,
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
            features, neutralFigures,
            deployedMeeples, playerActions,
            flags, events,
            phase
        );
    }

    public GameState appendEvent(PlayEvent ev) {
        return setEvents(events.append(ev));
    }

    public HashMap<CustomRule, Object> getRules() {
        return rules;
    }

    public CapabilitiesState getCapabilities() {
        return capabilities;
    }

    public PlayersState getPlayers() {
        return players;
    }

    public TilePackState getTilePack() {
        return tilePack;
    }

    public TileDefinition getDrawnTile() {
        return drawnTile;
    }

    public LinkedHashMap<Position, Tuple2<TileDefinition, Rotation>> getPlacedTiles() {
        return placedTiles;
    }

    public List<TileDefinition> getDiscardedTiles() {
        return discardedTiles;
    }

    //TODO rename to featureMap to avoid confusion
    public Map<FeaturePointer, Feature> getFeatures() {
        return features;
    }

    public NeutralFiguresState getNeutralFigures() {
        return neutralFigures;
    }

    public LinkedHashMap<Meeple, FeaturePointer> getDeployedMeeples() {
        return deployedMeeples;
    }

    public ActionsState getPlayerActions() {
        return playerActions;
    }

    public Set<Flag> getFlags() {
        return flags;
    }

    public boolean hasFlag(Flag flag) {
        return flags.contains(flag);
    }

    public Queue<PlayEvent> getEvents() {
        return events;
    }

    public Class<? extends Phase> getPhase() {
        return phase;
    }

    // ------ helpers -------------
    //TOOD move something to GameStateHelpers

    private Board board;

    public boolean getBooleanValue(CustomRule rule) {
        assert rule.getType().equals(Boolean.class);
        return (Boolean) rules.get(rule).getOrElse(Boolean.FALSE);
    }

    public Player getTurnPlayer() {
        return players.getTurnPlayer();
    }

    public Player getActivePlayer() {
        if (playerActions == null) {
            return null;
        }
        return playerActions.getPlayer();
    }

    public Board getBoard() {
        if (board == null) board = new Board(this);
        return board;
    }

    public boolean isGameOver() {
        return GameOverPhase.class.equals(phase);
    }

    public Queue<PlayEvent> getCurrentTurnEvents() {
        Queue<PlayEvent> res = Queue.empty();
        for (PlayEvent ev : events.reverseIterator()) {
            res.prepend(ev);
            if (ev instanceof PlayerTurnEvent) {
                break;
            }
        }
        return res;
    }


    public static enum Flag {
        // Cleared at the turn end
        RANSOM_PAID, BAZAAR_AUCTION,

        // Cleared at the turn part end
        PORTAL_USED, PRINCESS_USED
    }
}
