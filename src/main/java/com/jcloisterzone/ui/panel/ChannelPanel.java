package com.jcloisterzone.ui.panel;

import static com.jcloisterzone.ui.I18nUtils._;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.Expansion;
import com.jcloisterzone.event.ClientListChangedEvent;
import com.jcloisterzone.event.GameListChangedEvent;
import com.jcloisterzone.event.setup.ExpansionChangedEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.ui.ChannelController;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.LengthRestrictedDocument;
import com.jcloisterzone.ui.controls.chat.ChannelChatPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.ui.gtk.ThemedJLabel;
import com.jcloisterzone.ui.gtk.ThemedJPanel;
import com.jcloisterzone.wsio.WebSocketConnection;
import com.jcloisterzone.wsio.message.AbandonGameMessage;
import com.jcloisterzone.wsio.message.CreateGameMessage;
import com.jcloisterzone.wsio.message.GameMessage.GameStatus;
import com.jcloisterzone.wsio.message.JoinGameMessage;
import com.jcloisterzone.wsio.server.RemoteClient;

import net.miginfocom.swing.MigLayout;

@SuppressWarnings("serial")
public class ChannelPanel extends ThemedJPanel {

    private static final int MAX_GAME_TITLE_LENGTH = 60;

    private final Client client;
    private final ChannelController cc;

    private ChatPanel chatPanel;
    private ConnectedClientsPanel connectedClientsPanel;
    private JPanel gameListPanel;

    public ChannelPanel(Client client, ChannelController cc) {
        this.client = client;
        this.cc = cc;
        setLayout(new MigLayout("ins 0", "[][]0[grow]", "[][grow]"));
        setBackground(client.getTheme().getMainBg());

        add(connectedClientsPanel = new ConnectedClientsPanel(client, "play.jcz"), "cell 0 0, sy 2, width 150::, grow");

        chatPanel = new ChannelChatPanel(client, cc);
        add(chatPanel, "cell 1 0, grow, w 250, sy 2");
        add(createCreateGamePanel(), "cell 2 0, growx");

        gameListPanel = new JPanel();
        gameListPanel.setLayout(new MigLayout("ins rel 0, gap 0 rel", "[grow]", ""));
        //HACK
        if (client.getTheme().isDark()) {
            gameListPanel.setBackground(client.getTheme().getMainBg());
        } else {
            gameListPanel.setBackground(new Color(180, 180, 180));
        }


        JScrollPane scroll = new JScrollPane(gameListPanel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setViewportBorder(null);  //ubuntu jdk
        scroll.setBorder(BorderFactory.createEmptyBorder()); //win jdk
        add(scroll, "cell 2 1, grow");

        cc.register(this);
        cc.register(chatPanel);
    }

    private JPanel createCreateGamePanel() {
        String maintenance = ((WebSocketConnection) cc.getConnection()).getMaintenance();
        JPanel createGamePanel = new ThemedJPanel(new MigLayout());

        if (maintenance != null) {
            createGamePanel.add(new ThemedJLabel("Server is in maintenance mode."), "wrap");
            createGamePanel.add(new ThemedJLabel(maintenance), "wrap");
        } else {
            createGamePanel.add(new ThemedJLabel(_("Game title")+":"));

            String defaultTitle = cc.getConnection().getNickname() + "'s game";
            final JTextField gameTitle = new JTextField();
            gameTitle.setDocument(new LengthRestrictedDocument(MAX_GAME_TITLE_LENGTH));
            gameTitle.setText(defaultTitle); //set after document
            createGamePanel.add(gameTitle, "wrap, width 250::");

            createGamePanel.add(new ThemedJLabel(_("Password")+":"));
            final JTextField password = new JPasswordField();
            createGamePanel.add(password, "wrap, width 250::");

            JLabel passwordHint = new ThemedJLabel(_("If you leave password empty, anybody can connect to your game."));
            passwordHint.setFont(new Font(null, Font.ITALIC, 12));
            createGamePanel.add(passwordHint, "wrap, span 2");

            JButton createGameButton = new JButton(_("Create game"));
            createGameButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String title = gameTitle.getText().trim();
                    String pwd = password.getText().toString();
                    cc.getConnection().send(new CreateGameMessage(title, cc.getChannel().getName(), pwd));
                }
            });
            createGamePanel.add(createGameButton,"wrap, gaptop 20, span 2");

        }
        return createGamePanel;
    }


    @Subscribe
    public void clientListChanged(ClientListChangedEvent ev) {
        connectedClientsPanel.updateClients(ev.getClients());
    }

    @Subscribe
    public void gameListChanged(GameListChangedEvent ev) {
        //TODO optimize, update instead recreate all
        gameListPanel.removeAll();
        for (GameController gc : ev.getGameControllers()) {
            gameListPanel.add(new GameItemPanel(gc), "wrap, growx");
        }
        gameListPanel.validate();
        gameListPanel.repaint();
    }

    public ChatPanel getChatPanel() {
        return chatPanel;
    }

    private static Font FONT_GAME_TITLE = new Font(null, Font.BOLD, 20);

    class GameItemPanel extends ThemedJPanel {

        private JLabel name;
        private JLabel expansionNames;
        private JLabel connectedClients;
        private JButton joinButton, abandonButton;

        private Joiner joiner = Joiner.on(", ").skipNulls();
        private Set<Expansion> expansions;

        public GameItemPanel(final GameController gc) {
            final Game game = gc.getGame();
            setLayout(new MigLayout());
            expansions = game.getSetup().getExpansions().toJavaSet();
            expansions.remove(Expansion.BASIC);

            name = new ThemedJLabel(game.getName());
            name.setFont(FONT_GAME_TITLE);

            expansionNames = new ThemedJLabel();
            updateExpansionsLabel();
            connectedClients = new ThemedJLabel();
            updateClientsLabel(gc.getRemoteClients());

            JPanel buttons = new ThemedJPanel();
            buttons.setLayout(new MigLayout("ins 0"));

            final JPasswordField password = new JPasswordField();
            if (gc.isPasswordProtected()) {
                buttons.add(new ThemedJLabel(_("Password")+":"));
                buttons.add(password, "width 160");
            }

            joinButton = new JButton(gc.getGameStatus() == GameStatus.OPEN ? _("Join game") : _("Continue"));
            joinButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JoinGameMessage msg = new JoinGameMessage(game.getGameId());
                    if (gc.isPasswordProtected()) {
                        msg.setPassword(password.getText().toString());
                    }
                    cc.getConnection().send(msg);
                }
            });
            buttons.add(joinButton);

            if (gc.getGameStatus() != GameStatus.OPEN) {
                abandonButton = new JButton(_("Remove game"));
                abandonButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int result = JOptionPane.showConfirmDialog(client,
                            _("Do you want to remove game permanently?"), _("Remove game"),
                            JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            cc.getConnection().send(new AbandonGameMessage(gc.getGame().getGameId()));
                        }
                    }
                });
                buttons.add(abandonButton);
            }

            add(name, "wrap");
            add(expansionNames, "wrap");
            add(connectedClients, "wrap");
            add(buttons, "wrap");

            //TODO but what about unregister
            gc.register(this);
        }

        private void updateClientsLabel(List<RemoteClient> clients) {
            if (clients == null) return;

            String label = joiner.join(Lists.transform(clients, new Function<RemoteClient, String>() {
                @Override
                public String apply(RemoteClient rc) {
                    return rc.getName();
                }
            }));
            connectedClients.setText(_("Players") + ": " + label);
        }

        private void updateExpansionsLabel() {
            String label = joiner.join(expansions);
            if (label.length() == 0) label = Expansion.BASIC.toString();
            expansionNames.setText(_("Expansions") + ": " + label);
        }

        @Subscribe
        public void clientListChanged(ClientListChangedEvent ev) {
            updateClientsLabel(ev.getClients());
        }

        @Subscribe
        public void expansionsChanged(ExpansionChangedEvent ev) {
            if (ev.getExpansion() == Expansion.BASIC) return;
            if (ev.isEnabled()) {
                expansions.add(ev.getExpansion());
            } else {
                expansions.remove(ev.getExpansion());
            }
            updateExpansionsLabel();
        }
    }
}
