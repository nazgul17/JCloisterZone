package com.jcloisterzone.ui.view;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.event.GameStateChangeEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.PlayerSlot;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.MenuBar;
import com.jcloisterzone.ui.MenuBar.MenuItem;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.ui.controls.chat.GameChatPanel;
import com.jcloisterzone.ui.panel.BackgroundPanel;
import com.jcloisterzone.ui.panel.ConnectedClientsPanel;
import com.jcloisterzone.ui.panel.CreateGamePanel;

import net.miginfocom.swing.MigLayout;

public class GameSetupView extends AbstractUiView {

    private final GameController gc;
    private final Game game;
    private final boolean mutableSlots;

    private ChatPanel chatPanel;
    private CreateGamePanel createGamePanel;
    private ConnectedClientsPanel connectedClientsPanel;

    public GameSetupView(Client client, GameController gc, boolean mutableSlots) {
        super(client);
        this.gc = gc;
        this.game = gc.getGame();
        this.mutableSlots = mutableSlots;
    }

    public GameController getGameController() {
        return gc;
    }

    public Game getGame() {
        return game;
    }

    @Override
    public void show(Container pane, Object ctx) {
        Game game = gc.getGame();

        BackgroundPanel bg = new BackgroundPanel();
        bg.setLayout(new BorderLayout());
        pane.add(bg);

        showCreateGamePanel(bg, mutableSlots, game.getPlayerSlots());

        MenuBar menu = client.getJMenuBar();
        menu.setItemActionListener(MenuItem.LEAVE_GAME, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gc.leaveGame();
            }
        });
        menu.setItemEnabled(MenuItem.LEAVE_GAME, true);
    }

    private void showCreateGamePanel(Container panel, boolean mutableSlots, PlayerSlot[] slots) {
        createGamePanel = new CreateGamePanel(client, gc, mutableSlots, slots);
        createGamePanel.setBackground(client.getTheme().getMainBg());
        JPanel envelope = new BackgroundPanel();
        envelope.setLayout(new MigLayout("align 50% 50%", "[]", "[]")); //to have centered inner panel
        envelope.add(createGamePanel, "grow");

        panel.add(envelope, BorderLayout.CENTER);


        JPanel chatColumn = new JPanel();
        chatColumn.setOpaque(false);
        chatColumn.setLayout(new MigLayout("ins 0, gap 0 10", "[grow]", "[60px][grow]"));
        chatColumn.setPreferredSize(new Dimension(250, panel.getHeight()));
        panel.add(chatColumn, BorderLayout.WEST);

        chatColumn.add(connectedClientsPanel = new ConnectedClientsPanel(client, game.getSetup().getName()), "cell 0 0, grow");

        chatPanel = new GameChatPanel(client, game);
        chatColumn.add(chatPanel, "cell 0 1, grow");

        gc.register(createGamePanel);
        gc.register(chatPanel);
        gc.register(this);
    }

    @Override
    public boolean requestHide(UiView nextView, Object nextCtx) {
        if (nextCtx != this) {
            return client.closeGame();
        } else {
            return true;
        }
    }

    @Override
    public void hide(UiView nextView, Object nextCtx) {
        gc.unregister(createGamePanel);
        gc.unregister(chatPanel);
        gc.unregister(this);

        MenuBar menu = client.getJMenuBar();
        menu.setItemEnabled(MenuItem.LEAVE_GAME, false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (chatPanel.getInput().hasFocus()) return false;
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            if (e.getKeyChar() == '`' || e.getKeyChar() == ';') {
                e.consume();
                chatPanel.activateChat();
                return true;
            }
        }
        return false;
    }

    @Subscribe
    public void started(GameStateChangeEvent ev) {
        if (GameStateChangeEvent.GAME_START == ev.getType()) {
            GameView view = new GameView(client, gc);
            view.setChatPanel(chatPanel);
            view.setSnapshot(ev.getSnapshot());
            client.mountView(view, this);
        }
    }

    @Subscribe
    public void clientListChanged(ClientListChangedEvent ev) {
        connectedClientsPanel.updateClients(ev.getClients());
    }

    @Override
    public void onWebsocketClose(int code, String reason, boolean remote) {
        client.mountView(new StartView(client));
    }
}
