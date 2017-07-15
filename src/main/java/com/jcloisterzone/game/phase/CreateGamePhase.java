package com.jcloisterzone.game.phase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ClassToInstanceMap;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerAttributes;
import com.jcloisterzone.ai.AiPlayer;
import com.jcloisterzone.board.Position;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.board.TileDefinition;
import com.jcloisterzone.board.TileGroupState;
import com.jcloisterzone.board.TilePackFactory;
import com.jcloisterzone.board.TilePackFactory.Tiles;
import com.jcloisterzone.board.TilePackState;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.event.PlayerTurnEvent;
import com.jcloisterzone.event.setup.SupportedExpansionsChangeEvent;
import com.jcloisterzone.game.Capability;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.PigHerdCapability;
import com.jcloisterzone.reducers.PlaceTile;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.SlotMessage;

import io.vavr.Tuple2;
import io.vavr.collection.Array;


public class CreateGamePhase extends ServerAwarePhase {

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

    protected PlayerSlot[] slots;
    protected Expansion[][] slotSupportedExpansions = new Expansion[PlayerSlot.COUNT][];

    public CreateGamePhase(Game game, GameController controller) {
        super(game, controller);
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
        if (!phase.isActive()) return next;

        ClassToInstanceMap<Phase> phases = game.getPhases();
        phases.put(phase.getClass(), phase);
        if (next != null) {
            phase.setDefaultNext(next);
        }
        return phase;
    }

    protected void preparePhases() {
        GameController gc = getGameController();
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
        next = addPhase(next, new PlaguePhase(game));
        next = addPhase(next, new TilePhase(game));
        next = addPhase(next, new DrawPhase(game, gc));
        next = addPhase(next, new AbbeyPhase(game, gc));
        next = addPhase(next, new FairyPhase(game));
        setDefaultNext(next); //set next phase for this (CreateGamePhase) instance
        last.setDefaultNext(next); //after last phase, the first is default
    }

    protected void preparePlayers() {
        List<PlayerAttributes> players = new ArrayList<>();
        PlayerSlot[] sorted = new PlayerSlot[slots.length];
        System.arraycopy(slots, 0, sorted, 0, slots.length);
        Arrays.sort(sorted, new PlayerSlotComparator());
        for (int i = 0; i < sorted.length; i++) {
            PlayerSlot slot = sorted[i];
            if (slot.isOccupied()) {
                PlayerAttributes player = new PlayerAttributes(slot.getNickname(), i, slot);
                players.add(player);
            }
        }
        if (players.isEmpty()) {
            throw new IllegalStateException("No players in game");
        }
        game.setPlayers(Array.ofAll(players), 0);
    }

    protected Snapshot getSnapshot() {
        return null;
    }

    protected Tiles prepareTilePack() {
        TilePackFactory tilePackFactory = new TilePackFactory();
        tilePackFactory.setGame(game);
        tilePackFactory.setConfig(getGameController().getConfig());
        tilePackFactory.setExpansions(game.getExpansions());

        Tiles tiles = tilePackFactory.createTilePack();
        TilePackState tilePack = tiles.getTilePack();
        tilePack = tilePack.setGroupState("default", TileGroupState.ACTIVE);
        tilePack = tilePack.setGroupState("count", TileGroupState.ACTIVE);
        tilePack = tilePack.setGroupState("wind-rose-initial", TileGroupState.ACTIVE);
        return new Tiles(tilePack, tiles.getPreplacedTiles());
    }

    protected void preplaceTiles(Iterable<Tuple2<TileDefinition, Position>> preplacedTiles) {
        for (Tuple2<TileDefinition, Position> t : preplacedTiles) {
            game.replaceState(new PlaceTile(t._1, t._2, Rotation.R0));
        }
    }

    protected void prepareAiPlayers(boolean muteAi) {
        for (PlayerSlot slot : slots) {
            if (slot != null && slot.isAi() && slot.isOwn()) {
                try {
                    AiPlayer ai = (AiPlayer) Class.forName(slot.getAiClassName()).newInstance();
                    ai.setMuted(muteAi);
                    ai.setGame(game);
                    ai.setGameController(getGameController());
                    for (Player player : game.getAllPlayers()) {
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

    protected void prepareCapabilities() {
        for (Expansion exp : game.getExpansions()) {
            game.getCapabilityClasses().addAll(Arrays.asList(exp.getCapabilities()));
        }

        if (game.getBooleanValue(CustomRule.USE_PIG_HERDS_INDEPENDENTLY)) {
            game.getCapabilityClasses().add(PigHerdCapability.class);
        }

        DebugConfig debugConfig = getDebugConfig();
        if (debugConfig != null && debugConfig.getOff_capabilities() != null) {
            List<String> offNames =  debugConfig.getOff_capabilities();
            Set<Class<? extends Capability>> off = new HashSet<>();
            for (String tok : offNames) {
                tok = tok.trim();
                try {
                    String className = "com.jcloisterzone.game.capability."+tok+"Capability";
                    @SuppressWarnings("unchecked")
                    Class<? extends Capability> clazz = (Class<? extends Capability>) Class.forName(className);
                    off.add(clazz);
                } catch (Exception e) {
                    logger.warn("Invalid capability name: " + tok, e);
                }
            }
            game.getCapabilityClasses().removeAll(off);
        }
    }

    public void startGame(boolean muteAi) {
        //temporary code should be configured by player as rules
        prepareCapabilities();

        game.createCapabilities();
        preparePlayers();
        preparePhases();
        Tiles tiles = prepareTilePack();
        game.replaceState(state -> state.setTilePack(tiles.getTilePack()));
        game.begin();
        prepareAiPlayers(muteAi);

        game.post(new GameStateChangeEvent(GameStateChangeEvent.GAME_START, getSnapshot()));
        preplaceTiles(tiles.getPreplacedTiles());
        game.post(new PlayerTurnEvent(game.getTurnPlayer()));;
        toggleClock(game.getTurnPlayer());
        next();
    }

}