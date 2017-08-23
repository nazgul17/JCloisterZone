package com.jcloisterzone.ui;

import static com.jcloisterzone.ui.I18nUtils._;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcloisterzone.Expansion;
import com.jcloisterzone.Player;
import com.jcloisterzone.PlayerClock;
import com.jcloisterzone.config.Config.AutostartConfig;
import com.jcloisterzone.config.Config.DebugConfig;
import com.jcloisterzone.config.Config.PresetConfig;
import com.jcloisterzone.event.ChatEvent;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.event.ClockUpdateEvent;
import com.jcloisterzone.event.GameListChangedEvent;
import com.jcloisterzone.event.setup.ExpansionChangedEvent;
import com.jcloisterzone.event.setup.PlayerSlotChangeEvent;
import com.jcloisterzone.event.setup.RuleChangeEvent;
import com.jcloisterzone.game.CustomRule;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.game.PlayerSlot.SlotState;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.state.GameStateBuilder;
import com.jcloisterzone.online.Channel;
import com.jcloisterzone.ui.controls.chat.GameChatPanel;
import com.jcloisterzone.ui.view.ChannelView;
import com.jcloisterzone.ui.view.GameSetupView;
import com.jcloisterzone.ui.view.GameView;
import com.jcloisterzone.wsio.Connection;
import com.jcloisterzone.wsio.MessageDispatcher;
import com.jcloisterzone.wsio.MessageListener;
import com.jcloisterzone.wsio.MutedConnection;
import com.jcloisterzone.wsio.WebSocketConnection;
import com.jcloisterzone.wsio.WsSubscribe;
import com.jcloisterzone.wsio.message.ChannelMessage;
import com.jcloisterzone.wsio.message.ChannelMessage.ChannelMessageGame;
import com.jcloisterzone.wsio.message.ChatMessage;
import com.jcloisterzone.wsio.message.ClientUpdateMessage;
import com.jcloisterzone.wsio.message.ClientUpdateMessage.ClientState;
import com.jcloisterzone.wsio.message.ClockMessage;
import com.jcloisterzone.wsio.message.ErrorMessage;
import com.jcloisterzone.wsio.message.GameMessage;
import com.jcloisterzone.wsio.message.GameMessage.GameState;
import com.jcloisterzone.wsio.message.GameSetupMessage;
import com.jcloisterzone.wsio.message.GameUpdateMessage;
import com.jcloisterzone.wsio.message.SetExpansionMessage;
import com.jcloisterzone.wsio.message.SetRuleMessage;
import com.jcloisterzone.wsio.message.SlotMessage;
import com.jcloisterzone.wsio.message.StartGameMessage;
import com.jcloisterzone.wsio.message.TakeSlotMessage;
import com.jcloisterzone.wsio.message.UndoMessage;
import com.jcloisterzone.wsio.message.WsInChannelMessage;
import com.jcloisterzone.wsio.message.WsInGameMessage;
import com.jcloisterzone.wsio.message.WsMessage;
import com.jcloisterzone.wsio.server.RemoteClient;

import io.vavr.collection.Array;


public class ClientMessageListener implements MessageListener {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private WebSocketConnection conn;
    private MessageDispatcher dispatcher = new MessageDispatcher();

    private Map<String, GameController> gameControllers = new HashMap<>();
    private Map<String, ChannelController> channelControllers = new HashMap<>();

    private final boolean playOnline;
    private final Client client;
    private boolean autostartPerfomed;

    public ClientMessageListener(Client client, boolean playOnline) {
        this.client = client;
        this.playOnline = playOnline;
    }

    public WebSocketConnection connect(String username, URI uri) {
        conn = new WebSocketConnection(username, client.getConfig(), uri, this);
        return conn;
    }

    public boolean isPlayOnline() {
        return playOnline;
    }

    public Connection getConnection() {
        return conn;
    }

    public Map<String, ChannelController> getChannelControllers() {
        return channelControllers;
    }

    public List<GameController> getGameControllers(String channel) {
        List<GameController> gcs = new ArrayList<GameController>();
        for (GameController gc : gameControllers.values()) {
            if (gc.getChannel().equals(channel)) {
                gcs.add(gc);
            }
        }
        return gcs;
    }

    @Override
    public void onWebsocketError(Exception ex) {
        client.onWebsocketError(ex);
    }

    @Override
    public void onWebsocketClose(int code, String reason, boolean remote) {
        channelControllers.clear();
        gameControllers.clear();
        client.onWebsocketClose(code, reason, remote);
    }

    @Override
    public void onWebsocketMessage(WsMessage msg) {
        //TODO pass game as context to dispatch
        EventProxyUiController<?> controller = getController(msg);
        if (controller instanceof GameController) {
            GameController gc = (GameController) controller;
            // TODO IMMUTABLE temp hack
            if (gc.getGame().getStateBuilder() != null) {
                dispatcher.dispatch(msg, conn, this, gc.getGame().getStateBuilder());
            } else {
                dispatcher.dispatch(msg, conn, this, gc.getGame().getPhase());
            }
        } else {
            dispatcher.dispatch(msg, conn, this);
        }
    }

    public RemoteClient getClientBySessionId(EventProxyUiController<?> controller, String sessionId) {
        if (sessionId == null) return null;
        for (RemoteClient remote: controller.getRemoteClients()) {
            if (remote.getSessionId().equals(sessionId)) {
                return remote;
            }
        }
        throw new NoSuchElementException();
    }

    private EventProxyUiController<?> getController(WsMessage msg) {
        if (msg instanceof WsInGameMessage) {
            GameController gc = gameControllers.get(((WsInGameMessage) msg).getGameId());
            if (gc != null) return gc;
        }
        if (msg instanceof WsInChannelMessage) {
            ChannelController cc = channelControllers.get(((WsInChannelMessage) msg).getChannel());
            if (cc != null) return cc;
        }
        return null;
    }

    private Game getGame(WsInGameMessage msg) {
        GameController gc = (GameController) getController(msg);
        return gc == null ? null : gc.getGame();
    }

    private void updateSlot(PlayerSlot[] slots, SlotMessage slotMsg) {
        PlayerSlot slot = slots[slotMsg.getNumber()];
        slot.setNickname(slotMsg.getNickname());
        slot.setSessionId(slotMsg.getSessionId());
        slot.setClientId(slotMsg.getClientId());
        if (slotMsg.getClientId() == null) {
            slot.setState(SlotState.OPEN);
        } else {
            slot.setState(conn.getSessionId().equals(slotMsg.getSessionId()) ? SlotState.OWN : SlotState.REMOTE);
        }
        slot.setSerial(slotMsg.getSerial());
        slot.setAiClassName(slotMsg.getAiClassName());
    }

    private void createGameSlots(GameController gc, GameMessage msg) {
        PlayerSlot[] slots = new PlayerSlot[PlayerSlot.COUNT];
        for (SlotMessage slotMsg : msg.getSlots()) {
            int number = slotMsg.getNumber();
            PlayerSlot slot = new PlayerSlot(number);
            slot.setColors(client.getConfig().getPlayerColor(slot));
            slots[number] = slot;
            updateSlot(slots, slotMsg);
        }
        gc.getGame().getStateBuilder().setSlots(slots);
    }

    private GameController createGameController(GameMessage msg) {
        Snapshot snapshot = null;
        if (msg.getSnapshot() != null) {
            try {
                snapshot = new Snapshot(msg.getSnapshot());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        Game game;
        GameController gc;
        GameStateBuilder phase;
 //       if (snapshot == null) {
            game = new Game(msg.getGameId());
            game.setName(msg.getName());
            gc = new GameController(client, game);
            phase = new GameStateBuilder(game, gc);
//        } else {
//            game = snapshot.asGame(msg.getGameId());
//            gc = new GameController(client, game);
//            phase = new LoadGamePhase(game, snapshot, gc);
//        }
        gc.setReportingTool(conn.getReportingTool());
        gc.setChannel(msg.getChannel());
        gc.setPasswordProtected(msg.isPasswordProtected());
        if (msg instanceof ChannelMessageGame) {
            for (RemoteClient client : ((ChannelMessageGame)msg).getClients()) {
                if (client.getState() == null) client.setState(ClientState.ACTIVE);
                gc.getRemoteClients().add(client);
            }
        }
        game.setStateBuilder(phase);
        if (msg.getSlots() != null) {
            createGameSlots(gc, msg);
        }
        return gc;
    }

    private void handleGameStarted(final GameController gc, String[] replay) throws InvocationTargetException, InterruptedException {
        conn.getReportingTool().setGame(gc.getGame());
        GameStateBuilder phase = gc.getGame().getStateBuilder();
        phase.startGame(replay != null);

        if (replay != null) {
            //TODO IMMUTABLE
            gc.setConnection(new MutedConnection(conn));

            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    GameView view = new GameView(client, gc);
                    view.setChatPanel(new GameChatPanel(client, gc.getGame()));
                    client.mountView(view);
                }
            });

            //gc.phaseLoop();
            for (int i = 0; i < replay.length; i++) {
                if (i == replay.length - 1) {
                    for (PlayerSlot slot : phase.getPlayerSlots()) {
                        if (slot != null && slot.getAiPlayer() != null) {
                            slot.getAiPlayer().setMuted(false);
                        }
                    }
                }

                WsMessage msg = conn.getParser().fromJson(replay[i]);
                dispatcher.dispatch(msg, conn, this, gc.getGame().getPhase());
                //gc.phaseLoop();
            }
            gc.setConnection(conn);
        }
    }

    private void openGameSetup(final GameController gc, final GameMessage msg) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                client.mountView(new GameSetupView(client, gc, msg.getSnapshot() == null));
                performAutostart(gc.getGame()); //must wait for panel is created
            }
        });
    }

    @WsSubscribe
    public void handleGame(final GameMessage msg) throws InvocationTargetException, InterruptedException {
        handleGame(msg, false);
    }

    public GameController handleGame(final GameMessage msg, boolean channelList) throws InvocationTargetException, InterruptedException {
        msg.getGameSetup().setGameId(msg.getGameId());  //fill omitted id
        if (msg.getSlots() != null) {
            for (SlotMessage slotMsg : msg.getSlots()) {
                slotMsg.setGameId(msg.getGameId()); //fill omitted id
            }
        }

        GameController gc = (GameController) getController(msg);
        if (gc == null || msg.getReplay() != null) { //if replay, reinit controller
            //copy clients to new controller
            List<RemoteClient> remoteClients = new ArrayList<>();
            if (gc != null) {
                remoteClients.addAll(gc.getRemoteClients());
            }
            gc = createGameController(msg);
            gc.getRemoteClients().addAll(remoteClients);
            //TODO remove games on game over
            gameControllers.put(msg.getGameId(), gc);
        }

        handleGameSetup(msg.getGameSetup());
        if (msg.getSlots() != null) {
            createGameSlots(gc, msg);
            GameStateBuilder phase = gc.getGame().getStateBuilder();
            for (SlotMessage slotMsg : msg.getSlots()) {
                handleSlot(slotMsg);
                if (phase != null) {
                    phase.handleSlotMessage(slotMsg);
                }
            }
        }

        gc.setGameState(msg.getState());
        if (!channelList) {
            switch (msg.getState()) {
            case OPEN:
                openGameSetup(gc, msg);
                break;
            case RUNNING:
                handleGameStarted(gc, msg.getReplay());
                break;
            }
        }
        return gc;
    }

    @WsSubscribe
    public void handleChannel(final ChannelMessage msg) throws InvocationTargetException, InterruptedException {
        Channel channel = new Channel(msg.getName());
        final ChannelController channelController = new ChannelController(client, channel);
        channelController.getRemoteClients().addAll(Arrays.asList(msg.getClients()));
        channelControllers.clear();
        channelControllers.put(channel.getName(), channelController);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                client.mountView(new ChannelView(client, channelController));
            }
        });
        GameController[] gameControllers = new GameController[msg.getGames().length];
        int i = 0;
        for (ChannelMessageGame game : msg.getGames()) {
            game.setChannel(msg.getName()); //fill omitted channel
            gameControllers[i++] = handleGame(game, true);
        }
        channelController.getEventProxy().post(new GameListChangedEvent(gameControllers));
    }

    @WsSubscribe
    public void handleGameUpdate(GameUpdateMessage msg) throws InvocationTargetException, InterruptedException {
        msg.getGame().setChannel(msg.getChannel()); //fill omitted channel
        GameState state = msg.getGame().getState();
        if (GameState.OPEN.equals(state) || GameState.PAUSED.equals(state)) {
            GameController gc = (GameController) getController(msg.getGame());
            if (gc != null) {
                gc.setGameState(msg.getGame().getState());
                logger.warn("Unexpected state - should never happen");
                return; //can't happen now - but eq. rename etc is possible in future
            }
            handleGame(msg.getGame(), true);
        } else {
            if (GameState.RUNNING.equals(state) &&
                    client.getView() instanceof GameSetupView) {
                GameController gc = ((GameSetupView) client.getView()).getGameController();
                if (gc.getGame().getGameId().equals(msg.getGame().getGameId())) {
                    gc.setGameState(msg.getGame().getState());
                    return;
                }
            }
            gameControllers.remove(msg.getGame().getGameId());
        }
        List<GameController> gcs = getGameControllers(msg.getChannel());
        ChannelController channelController = (ChannelController) getController(msg);
        channelController.getEventProxy().post(
            new GameListChangedEvent(gcs.toArray(new GameController[gcs.size()]))
        );
    }


    @WsSubscribe
    public void handleClientUpdate(ClientUpdateMessage msg) {
        EventProxyUiController<?> controller = getController(msg);
        if (controller != null) {
            RemoteClient rc = new RemoteClient(msg.getSessionId(), msg.getName(), msg.getState());
            //TODO what about use Set?
            List<RemoteClient> clients = controller.getRemoteClients();
            if (ClientState.OFFLINE.equals(msg.getState())) {
                clients.remove(rc);
            } else {
                int idx = clients.indexOf(rc);
                if (idx == -1) {
                    clients.add(rc);
                } else {
                    clients.set(idx, rc);
                }
            }
            ArrayList<RemoteClient> frozenList = new ArrayList<RemoteClient>(clients);
            controller.getEventProxy().post(new ClientListChangedEvent(frozenList));
        } else {
            logger.warn("No controller for message {}", msg);
        }
    }

    @WsSubscribe
    public void handleChat(ChatMessage msg) {
        EventProxyUiController<?> controller = getController(msg);
        if (controller != null) {
            ChatEvent ev = new ChatEvent(getClientBySessionId(controller, msg.getSessionId()), msg.getText());
            controller.getEventProxy().post(ev);
        } else {
            logger.warn("No controller for message {}", msg);
        }
    }

    @WsSubscribe
    public void handleClockMessage(ClockMessage msg) {
        Game game = getGame(msg);
        Array<Player> players = game.getState().getPlayers().getPlayers();
        Player runningClockPlayer = msg.getRunning() == null ? null : players.get(msg.getRunning());

        game.replaceState(state -> state.mapPlayers(ps -> ps.setClocks(
          players.map(p -> new PlayerClock(
            msg.getClocks()[p.getIndex()],
            p.equals(runningClockPlayer)
          ))
        )));
        game.post(new ClockUpdateEvent(runningClockPlayer));
    }

    @WsSubscribe
    public void handleSlot(SlotMessage msg) {
        Game game = getGame(msg);
        //slot's can be updated also for running game
        PlayerSlot[] slots = game.getPlayerSlots();
        updateSlot(slots, msg);
        game.post(new PlayerSlotChangeEvent(slots[msg.getNumber()]));
    }

    @WsSubscribe
    public void handleGameSetup(GameSetupMessage msg) {
        Game game = getGame(msg);
        game.getExpansions().clear();
        game.getExpansions().addAll(msg.getExpansions());
        game.getCustomRules().clear();
        game.getCustomRules().putAll(msg.getRules());

        for (Expansion exp : Expansion.values()) {
            if (!exp.isImplemented()) continue;
            game.post(new ExpansionChangedEvent(exp, game.getExpansions().contains(exp)));
        }
        for (CustomRule rule : CustomRule.values()) {
            Object value = game.getCustomRules().get(rule);
            game.post(new RuleChangeEvent(rule, value));
        }
    }


    @WsSubscribe
    public void handleSetExpansion(SetExpansionMessage msg) {
        Game game = getGame(msg);
        Expansion expansion = msg.getExpansion();
        if (msg.isEnabled()) {
            game.getExpansions().add(expansion);
        } else {
            game.getExpansions().remove(expansion);
        }
        game.post(new ExpansionChangedEvent(expansion, msg.isEnabled()));
    }

    @WsSubscribe
    public void handleSetRule(SetRuleMessage msg) {
        Game game = getGame(msg);
        CustomRule rule = msg.getRule();
        if (msg.getValue() == null) {
            game.getCustomRules().remove(rule);
        } else {
            game.getCustomRules().put(rule, msg.getValue());
        }
        game.post(new RuleChangeEvent(rule, msg.getValue()));
    }

    @WsSubscribe
    public void handleUndo(UndoMessage msg) {
        Game game = getGame(msg);
        game.undo();
    }

    @WsSubscribe
    public void handleError(Connection conn, ErrorMessage err) {
        switch (err.getCode()) {
        case ErrorMessage.BAD_VERSION:
            logger.warn(err.getMessage());
            String msg;
            if (playOnline) {
                msg = _("Online play server is not compatible with your application. Please upgrade JCloisterZone to the latest version.");
            } else {
                msg = _("Remote JCloisterZone is not compatible with local application. Please upgrade both applications to same version.");
            }
            JOptionPane.showMessageDialog(client, msg, _("Incompatible versions"), JOptionPane.ERROR_MESSAGE);
            break;
        case ErrorMessage.INVALID_PASSWORD:
            JOptionPane.showMessageDialog(client, _("Invalid password"), _("Invalid password"), JOptionPane.WARNING_MESSAGE);
        default:
            JOptionPane.showMessageDialog(client, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getSessionId() {
        return conn.getSessionId();
    }


  protected void performAutostart(Game game) {
      DebugConfig debugConfig = client.getConfig().getDebug();
      if (!autostartPerfomed && debugConfig != null && debugConfig.isAutostartEnabled()) {
          autostartPerfomed = true; //apply autostart only once
          AutostartConfig autostartConfig = debugConfig.getAutostart();
          final PresetConfig presetCfg = client.getConfig().getPresets().get(autostartConfig.getPreset());
          if (presetCfg == null) {
              logger.warn("Autostart profile {} not found.", autostartConfig.getPreset());
              return;
          }

          final List<String> players = autostartConfig.getPlayers() == null ? new ArrayList<String>() : autostartConfig.getPlayers();
          if (players.isEmpty()) {
              players.add("Player");
          }

          int i = 0;
          for (String name: players) {
              Class<?> clazz = null;;
              PlayerSlot slot = game.getStateBuilder().getPlayerSlots()[i];
              try {
                  clazz = Class.forName(name);
                  slot.setAiClassName(name);
                  name = "AI-" + i + "-" + clazz.getSimpleName().replace("AiPlayer", "");
              } catch (ClassNotFoundException e) {
                  //empty
              }
              TakeSlotMessage msg = new TakeSlotMessage(game.getGameId(), i, name);
              if (slot.getAiClassName() != null) {
                  msg.setAiClassName(slot.getAiClassName());
              }
              conn.send(msg);
              i++;
          }

          presetCfg.updateGameSetup(conn, game.getGameId());
          conn.send(new StartGameMessage(game.getGameId()));
      }
  }

}
