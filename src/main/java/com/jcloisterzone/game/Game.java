package com.jcloisterzone.game;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.common.eventbus.EventBus;
import com.google.common.hash.HashCode;
import com.jcloisterzone.EventBusExceptionHandler;
import com.jcloisterzone.EventProxy;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.action.PlayerAction;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.board.pointer.MeeplePointer;
import com.jcloisterzone.event.Event;
import com.jcloisterzone.event.GameChangedEvent;
import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.event.play.PlayEvent;
import com.jcloisterzone.event.setup.SupportedExpansionsChangeEvent;
import com.jcloisterzone.figure.Meeple;
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
import com.jcloisterzone.game.phase.RequiredCapability;
import com.jcloisterzone.game.phase.ScorePhase;
import com.jcloisterzone.game.phase.TilePhase;
import com.jcloisterzone.game.phase.TowerCapturePhase;
import com.jcloisterzone.game.phase.WagonPhase;
import com.jcloisterzone.game.state.ActionsState;
import com.jcloisterzone.game.state.GameState;
import com.jcloisterzone.game.state.GameStateBuilder;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.SlotMessage;

import io.vavr.Tuple2;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Queue;


/**
 * Other information than board needs in game. Contains players with their
 * points, followers ... and game rules of current game.
 */
//TODO remove extends from GameSettings
public class Game implements EventProxy {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private final String gameId;
    private String name;

    private GameSetup setup;
    private GameState state = GameState.createEmpty();
    private final ClassToInstanceMap<Phase> phases = MutableClassToInstanceMap.create();

    protected PlayerSlot[] slots;
    protected Expansion[][] slotSupportedExpansions = new Expansion[PlayerSlot.COUNT][];

    // -- old --

//    private final List<NeutralFigure> neutralFigures = new ArrayList<>();
//
    private List<GameState> undoState = List.empty();


    private final EventBus eventBus = new EventBus(new EventBusExceptionHandler("game event bus"));
    //events are delayed and fired after phase is handled (and eventually switched to the new one) - important especially for AI handlers to not start before switch is done
    //private final java.util.Deque<Event> eventQueue = new java.util.ArrayDeque<>();

    private int idSequenceCurrVal = 0;

    private final Random random;
    private long randomSeed;

    public Game(String gameId) {
        this(gameId, HashCode.fromBytes(gameId.getBytes()).asLong());
    }

    public Game(String gameId, long randomSeed) {
        this.gameId = gameId;
        this.randomSeed = randomSeed;
        this.random = new Random(randomSeed);
    }

    public String getGameId() {
        return gameId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GameState getState() {
        return state;
    }

    public void setSetup(GameSetup setup) {
        this.setup = setup;
    }

    public GameSetup getSetup() {
        return setup;
    }

    public void mapSetup(Function<GameSetup, GameSetup> mapper) {
        setSetup(mapper.apply(setup));
    }

    public void replaceState(GameState state) {
        GameState prev = this.state;
        this.state = state;
        GameChangedEvent ev = new GameChangedEvent(prev, state);
        post(ev);

        if (logger.isInfoEnabled()) {
            StringBuilder sb;
            Queue<PlayEvent> playEvents = ev.getNewPlayEvents();
            if (!playEvents.isEmpty()) {
                sb = new StringBuilder();
                sb.append("play events:");
                for (PlayEvent pev : ev.getNewPlayEvents()) {
                    sb.append("\n  - ");
                    sb.append(pev.toString());
                }
                logger.info(sb.toString());
            }

            ActionsState as = state.getPlayerActions();
            if (as != null) {
                sb = new StringBuilder();
                sb.append(as.getPlayer().getNick());
                sb.append("'s actions:");
                for (PlayerAction<?> action : as.getActions()) {
                    sb.append("\n  - ");
                    sb.append(action.toString());
                    if (action.getOptions() != null) { // bazaar actions can be empty, handled in differente way
                        sb.append("\n    ");
                        sb.append(String.join(", " , action.getOptions().map(Object::toString)));
                    }
                }
                logger.info(sb.toString());
            }
        }
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    public void markUndo() {
        undoState = undoState.prepend(state);
    }

    public void clearUndo() {
        undoState = List.empty();
    }

    public boolean isUndoAllowed() {
        return !undoState.isEmpty();
    }

    public void undo() {
        if (undoState.isEmpty()) {
            throw new IllegalStateException();
        }
        Tuple2<GameState, List<GameState>> head = undoState.pop2();
        undoState = head._2;
        replaceState(head._1);
    }

    @WsSubscribe
    public void handleSlotMessage(SlotMessage msg) {
        slotSupportedExpansions[msg.getNumber()] = msg.getSupportedExpansions();
        post(new SupportedExpansionsChangeEvent(mergeSupportedExpansions()));
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

    @Override
    public void post(Event event) {
        eventBus.post(event);
    }

    //TODO decouple from GameController ?
    public void start(GameController gc) {
        Phase firstPhase = createPhases(gc);
        GameStateBuilder builder = new GameStateBuilder(setup, slots, gc.getConfig());
        GameState initialState = builder.build(firstPhase);

        post(new GameStateChangeEvent(GameStateChangeEvent.GAME_START, null));

        replaceState(initialState);
        firstPhase.enter(initialState);
    }

    private Phase addPhase(Phase next, Phase phase) {
        RequiredCapability req = phase.getClass().getAnnotation(RequiredCapability.class);

        if (req != null && !setup.getCapabilities().contains(req.value())) {
            return next;
        }

        phases.put(phase.getClass(), phase);
        if (next != null) {
            phase.setDefaultNext(next);
        }
        return phase;
    }

    protected Phase createPhases(GameController gc) {
        Phase last, next = null;
        //if there isn't assignment - phase is out of standard flow
               addPhase(next, new GameOverPhase(this, gc));
        next = last = addPhase(next, new CleanUpTurnPhase(this));
        next = addPhase(next, new BazaarPhase(this, gc));
        next = addPhase(next, new EscapePhase(this));
        next = addPhase(next, new CleanUpTurnPartPhase(this));
        next = addPhase(next, new CornCirclePhase(this, gc));

        if (setup.getBooleanValue(CustomRule.DRAGON_MOVE_AFTER_SCORING)) {
            addPhase(next, new DragonMovePhase(this, gc));
            next = addPhase(next, new DragonPhase(this));
        }

               addPhase(next, new CocCountPhase(this));
        next = addPhase(next, new CocFollowerPhase(this));
        next = addPhase(next, new WagonPhase(this, gc));
        next = addPhase(next, new ScorePhase(this, gc));
        next = addPhase(next, new CocPreScorePhase(this, gc));
        next = addPhase(next, new CommitActionPhase(this));
        next = addPhase(next, new CastlePhase(this));

        if (!setup.getBooleanValue(CustomRule.DRAGON_MOVE_AFTER_SCORING)) {
               addPhase(next, new DragonMovePhase(this, gc));
               next = addPhase(next, new DragonPhase(this));
        }

        next = addPhase(next, new PhantomPhase(this));
               addPhase(next, new TowerCapturePhase(this));
               addPhase(next, new FlierActionPhase(this));
        next = addPhase(next, new ActionPhase(this));
        next = addPhase(next, new MageAndWitchPhase(this));
        next = addPhase(next, new GoldPiecePhase(this));
        next = addPhase(next, new TilePhase(this, gc));
        next = addPhase(next, new AbbeyPhase(this, gc));
        next = addPhase(next, new FairyPhase(this));
        last.setDefaultNext(next); //after last phase, the first is default

        return next;
    }

    public void setSlots(PlayerSlot[] slots) {
        this.slots = slots;
    }

    public PlayerSlot[] getPlayerSlots() {
        return slots;
    }

    public Phase getPhase() {
        if (state == null) {
            return null;
        }
        return phases.get(state.getPhase());
    }

    public ClassToInstanceMap<Phase> getPhases() {
        return phases;
    }

    public LinkedHashMap<Meeple, FeaturePointer> getDeployedMeeples() {
        return state.getDeployedMeeples();
    }

    public Random getRandom() {
        return random;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void updateRandomSeed(long update) {
        randomSeed = randomSeed ^ update;
        random.setSeed(randomSeed);
    }

    public Meeple getMeeple(MeeplePointer mp) {
        Tuple2<Meeple, FeaturePointer> match =
            getDeployedMeeples().find(t -> mp.match(t._1)).getOrNull();
        return match == null ? null : match._1;
    }

    @Deprecated
    public boolean isStarted() {
        return state != null;
    }

    @Deprecated
    public boolean isOver() {
        return getPhase() instanceof GameOverPhase;
    }

    public int idSequnceNextVal() {
        return ++idSequenceCurrVal;
    }
}
