package com.jcloisterzone.game.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.ai.AiPlayer;
import com.jcloisterzone.board.TilePack;
import com.jcloisterzone.board.TilePackBuilder;
import com.jcloisterzone.board.TilePackBuilder.Tiles;
import com.jcloisterzone.config.Config;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.event.play.PlayEvent.PlayEventMeta;
import com.jcloisterzone.event.play.PlayerTurnEvent;
import com.jcloisterzone.event.setup.SupportedExpansionsChangeEvent;
import com.jcloisterzone.figure.Follower;
import com.jcloisterzone.figure.MeepleIdProvider;
import com.jcloisterzone.figure.SmallFollower;
import com.jcloisterzone.figure.Special;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.PigHerdCapability;
import com.jcloisterzone.game.phase.AbbeyPhase;
import com.jcloisterzone.game.phase.ActionPhase;
import com.jcloisterzone.game.phase.BazaarPhase;
import com.jcloisterzone.game.phase.CastlePhase;
import com.jcloisterzone.game.phase.CleanUpTurnPartPhase;
import com.jcloisterzone.game.phase.CleanUpTurnPhase;
import com.jcloisterzone.game.phase.CocCountPhase;
import com.jcloisterzone.game.phase.CocFollowerPhase;
import com.jcloisterzone.game.phase.CocPreScorePhase;
import com.jcloisterzone.game.phase.CommitActionPhase;
import com.jcloisterzone.game.phase.CornCirclePhase;
import com.jcloisterzone.game.phase.DragonMovePhase;
import com.jcloisterzone.game.phase.DragonPhase;
import com.jcloisterzone.game.phase.EscapePhase;
import com.jcloisterzone.game.phase.FairyPhase;
import com.jcloisterzone.game.phase.FlierActionPhase;
import com.jcloisterzone.game.phase.GameOverPhase;
import com.jcloisterzone.game.phase.GoldPiecePhase;
import com.jcloisterzone.game.phase.MageAndWitchPhase;
import com.jcloisterzone.game.phase.PhantomPhase;
import com.jcloisterzone.game.phase.Phase;
import com.jcloisterzone.game.phase.ScorePhase;
import com.jcloisterzone.game.phase.TilePhase;
import com.jcloisterzone.game.phase.TowerCapturePhase;
import com.jcloisterzone.game.phase.WagonPhase;
import com.jcloisterzone.reducers.PlaceTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.SlotMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;


public class GameStateBuilder {

    private final static class PlayerSlotComparator implements Comparator<PlayerSlot> {
        @Override
        public int compare(PlayerSlot o1, PlayerSlot o2) {
            if (o1.getSerial() == null) {
                return o2.getSerial() == null ? 0 : 1;
            }
            if (o2.getSerial() == null) return -1;
            if (o1.getSerial() < o2.getSerial()) return -1;
            if (o1.getSerial() > o2.getSerial()) return 1;
            return 0;
        }
    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final Game game;
    private final GameController gc;

    protected PlayerSlot[] slots;
    protected Expansion[][] slotSupportedExpansions = new Expansion[PlayerSlot.COUNT][];

    public GameStateBuilder(Game game, GameController gc) {
        this.game = game;
        this.gc = gc;
    }

    public void setSlots(PlayerSlot[] slots) {
        this.slots = slots;
    }

    public PlayerSlot[] getPlayerSlots() {
        return slots;
    }


    @WsSubscribe
    public void handleSlotMessage(SlotMessage msg) {
        slotSupportedExpansions[msg.getNumber()] = msg.getSupportedExpansions();
        game.post(new SupportedExpansionsChangeEvent(mergeSupportedExpansions()));
    }

    private EnumSet<Expansion> mergeSupportedExpansions() {
        EnumSet<Expansion> merged = null;
        for (int i = 0; i < slotSupportedExpansions.length; i++) {
            Expansion[] supported = slotSupportedExpansions[i];
            if (supported == null) continue;
            if (merged == null) {
                merged = EnumSet.allOf(Expansion.class);
            }
            EnumSet<Expansion> supp = EnumSet.noneOf(Expansion.class);
            Collections.addAll(supp, supported);
            merged.retainAll(supp);
        }
        return merged;
    }


    private Phase addPhase(Phase next, Phase phase) {
        if (!phase.isActive(game.getState().getCapabilities())) return next;

        ClassToInstanceMap<Phase> phases = game.getPhases();
        phases.put(phase.getClass(), phase);
        if (next != null) {
            phase.setDefaultNext(next);
        }
        return phase;
    }

    protected Phase preparePhases() {
        Phase last, next = null;
        //if there isn't assignment - phase is out of standard flow
               addPhase(next, new GameOverPhase(game, gc));
        next = last = addPhase(next, new CleanUpTurnPhase(game));
        next = addPhase(next, new BazaarPhase(game, gc));
        next = addPhase(next, new EscapePhase(game));
        next = addPhase(next, new CleanUpTurnPartPhase(game));
        next = addPhase(next, new CornCirclePhase(game, gc));

        if (game.getBooleanValue(CustomRule.DRAGON_MOVE_AFTER_SCORING)) {
            addPhase(next, new DragonMovePhase(game, gc));
            next = addPhase(next, new DragonPhase(game));
        }

               addPhase(next, new CocCountPhase(game));
        next = addPhase(next, new CocFollowerPhase(game));
        next = addPhase(next, new WagonPhase(game, gc));
        next = addPhase(next, new ScorePhase(game, gc));
        next = addPhase(next, new CocPreScorePhase(game, gc));
        next = addPhase(next, new CommitActionPhase(game));
        next = addPhase(next, new CastlePhase(game));

        if (!game.getBooleanValue(CustomRule.DRAGON_MOVE_AFTER_SCORING)) {
               addPhase(next, new DragonMovePhase(game, gc));
               next = addPhase(next, new DragonPhase(game));
        }

        next = addPhase(next, new PhantomPhase(game));
               addPhase(next, new TowerCapturePhase(game));
               addPhase(next, new FlierActionPhase(game));
        next = addPhase(next, new ActionPhase(game));
        next = addPhase(next, new MageAndWitchPhase(game));
        next = addPhase(next, new GoldPiecePhase(game));
        next = addPhase(next, new TilePhase(game, gc));
        next = addPhase(next, new AbbeyPhase(game, gc));
        next = addPhase(next, new FairyPhase(game));
        last.setDefaultNext(next); //after last phase, the first is default
        return next;
    }

    protected Array<Player> preparePlayers() {
        List<Player> players = new ArrayList<>();
        PlayerSlot[] sorted = new PlayerSlot[slots.length];
        System.arraycopy(slots, 0, sorted, 0, slots.length);
        Arrays.sort(sorted, new PlayerSlotComparator());
        for (int i = 0; i < sorted.length; i++) {
            PlayerSlot slot = sorted[i];
            if (slot.isOccupied()) {
                Player player = new Player(slot.getNickname(), i, slot);
                players.add(player);
            }
        }
        if (players.isEmpty()) {
            throw new IllegalStateException("No players in game");
        }
        return Array.ofAll(players);
    }

    protected Snapshot getSnapshot() {
        return null;
    }

    protected Tuple2<Seq<PlacedTile>, GameState> prepareTilePack(Set<Expansion> expansions, GameState state) {
        TilePackBuilder tilePackFactory = new TilePackBuilder();
        tilePackFactory.setGameState(state);
        tilePackFactory.setConfig(gc.getConfig());
        tilePackFactory.setExpansions(game.getExpansions());

        Tiles tiles = tilePackFactory.createTilePack();
        TilePack tilePack = tiles.getTilePack();
        state = state.setTilePack(tilePack);
        return new Tuple2<>(tiles.getPreplacedTiles(), state);
    }

    protected void preplaceTiles(Seq<PlacedTile> preplacedTiles) {
        for (PlacedTile pt : preplacedTiles) {
            game.replaceState(new PlaceTile(pt.getTile(), pt.getPosition(), pt.getRotation()));
        }
    }

    protected void prepareAiPlayers(boolean muteAi) {
        for (PlayerSlot slot : slots) {
            if (slot != null && slot.isAi() && slot.isOwn()) {
                try {
                    AiPlayer ai = (AiPlayer) Class.forName(slot.getAiClassName()).newInstance();
                    ai.setMuted(muteAi);
                    ai.setGame(game);
                    ai.setGameController(gc);
                    for (Player player : game.getState().getPlayers().getPlayers()) {
                        if (player.getSlot().getNumber() == slot.getNumber()) {
                            ai.setPlayer(player);
                            break;
                        }
                    }
                    slot.setAiPlayer(ai);
                    game.getEventBus().register(ai);
                    logger.info("AI player created - " + slot.getAiClassName());
                } catch (Exception e) {
                    logger.error("Unable to create AI player", e);
                }
            }
        }
    }

    private DebugConfig getDebugConfig() {
        Config config = gc.getConfig();
        return config == null ? null : config.getDebug();
    }

    protected io.vavr.collection.Set<Class<? extends Capability<?>>> getCapabilityClasses() {
        io.vavr.collection.Set<Class<? extends Capability<?>>> classes = io.vavr.collection.HashSet.empty();
        for (Expansion exp : game.getExpansions()) {
            classes = classes.addAll(Arrays.asList(exp.getCapabilities()));
        }

        if (game.getBooleanValue(CustomRule.USE_PIG_HERDS_INDEPENDENTLY)) {
            classes = classes.add(PigHerdCapability.class);
        }

        DebugConfig debugConfig = getDebugConfig();
        if (debugConfig != null && debugConfig.getOff_capabilities() != null) {
            List<String> offNames =  debugConfig.getOff_capabilities();
            for (String tok : offNames) {
                tok = tok.trim();
                try {
                    String className = "com.jcloisterzone.game.capability."+tok+"Capability";
                    @SuppressWarnings("unchecked")
                    Class<? extends Capability<?>> clazz = (Class<? extends Capability<?>>) Class.forName(className);
                    classes = classes.remove(clazz);
                } catch (Exception e) {
                    logger.warn("Invalid capability name: " + tok, e);
                }
            }
        }
        return classes;
    }

    private Capability<?> createCapabilityInstance(Class<? extends Capability<?>> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create " + clazz.getSimpleName(), e);
        }
    }

    public io.vavr.collection.List<Capability<?>> createCapabilities(io.vavr.collection.Set<Class<? extends Capability<?>>> classes) {
        return io.vavr.collection.List.narrow(
            classes.map(cls -> createCapabilityInstance(cls)).toList()
        );
    }

    private io.vavr.collection.List<Follower> createPlayerFollowers(Player p, Seq<Capability<?>> capabilities) {
        MeepleIdProvider idProvider = new MeepleIdProvider(p);
        Stream<Follower> stream = Stream.range(0, SmallFollower.QUANTITY)
                .map(i -> (Follower) new SmallFollower(idProvider.generateId(SmallFollower.class), p));
        io.vavr.collection.List<Follower> followers = io.vavr.collection.List.ofAll(stream);
        followers = followers.appendAll(capabilities.flatMap(c -> c.createPlayerFollowers(p, idProvider)));
        return followers;
    }

    public Seq<Special> createPlayerSpecialMeeples(Player p, Seq<Capability<?>> capabilities) {
        MeepleIdProvider idProvider = new MeepleIdProvider(p);
        return capabilities.flatMap(c -> c.createPlayerSpecialMeeples(p, idProvider));
    }

    public void startGame(boolean muteAi) {
        //temporary code should be configured by player as rules
        io.vavr.collection.List<Capability<?>> capabilities = createCapabilities(getCapabilityClasses());
        Array<Player> players = preparePlayers();

        GameState state = GameState.createInitial(
            HashMap.ofAll(game.getCustomRules()),
            capabilities,
            players,
            0
        );

        state = state.mapPlayers(ps ->
            ps.setFollowers(
                players.map(p -> createPlayerFollowers(p, capabilities))
            ).setSpecialMeeples(
                players.map(p -> createPlayerSpecialMeeples(p, capabilities))
            ).setClocks(
                players.map(p -> new PlayerClock(0))
            )
        );

        Tuple2<Seq<PlacedTile>, GameState> t =
                prepareTilePack(game.getExpansions(), state);

        Seq<PlacedTile> preplacedTiles = t._1;
        state = t._2;

        for (Capability<?> cap : capabilities) {
            state = cap.onStartGame(state);
        }

        game.replaceState(state);

        Phase first = preparePhases();
        prepareAiPlayers(muteAi);

        Player player = state.getTurnPlayer();
        game.post(new GameStateChangeEvent(GameStateChangeEvent.GAME_START, getSnapshot()));
        preplaceTiles(preplacedTiles);
        game.replaceState(
            s -> s.appendEvent(new PlayerTurnEvent(PlayEventMeta.createWithoutPlayer(), player)),
            s -> s.setPhase(first.getClass())
        );
        game.setStateBuilder(null);
        first.enter(game.getState());
    }

}