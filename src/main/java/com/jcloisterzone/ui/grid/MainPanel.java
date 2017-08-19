package com.jcloisterzone.ui.grid;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import com.google.common.eventbus.Subscribe;
import com.jcloisterzone.board.Rotation;
import com.jcloisterzone.event.CornCirclesOptionEvent;
import com.jcloisterzone.game.Game;
import com.jcloisterzone.game.Snapshot;
import com.jcloisterzone.game.capability.BridgeCapability;
import com.jcloisterzone.game.capability.CastleCapability;
import com.jcloisterzone.game.capability.GoldminesCapability;
import com.jcloisterzone.game.capability.LittleBuildingsCapability;
import com.jcloisterzone.game.capability.PlagueCapability;
import com.jcloisterzone.game.capability.TowerCapability;
import com.jcloisterzone.game.capability.TunnelCapability;
import com.jcloisterzone.game.state.CapabilitiesState;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.ui.controls.ControlPanel;
import com.jcloisterzone.ui.controls.chat.ChatPanel;
import com.jcloisterzone.ui.grid.actionpanel.CornCirclesPanel;
import com.jcloisterzone.ui.grid.layer.AnimationLayer;
import com.jcloisterzone.ui.grid.layer.BridgeLayer;
import com.jcloisterzone.ui.grid.layer.CastleLayer;
import com.jcloisterzone.ui.grid.layer.FarmHintsLayer;
import com.jcloisterzone.ui.grid.layer.FeatureAreaLayer;
import com.jcloisterzone.ui.grid.layer.FollowerAreaLayer;
import com.jcloisterzone.ui.grid.layer.GoldLayer;
import com.jcloisterzone.ui.grid.layer.LittleBuildingActionLayer;
import com.jcloisterzone.ui.grid.layer.MeepleLayer;
import com.jcloisterzone.ui.grid.layer.PlacementHistory;
import com.jcloisterzone.ui.grid.layer.PlagueLayer;
import com.jcloisterzone.ui.grid.layer.TileActionLayer;
import com.jcloisterzone.ui.grid.layer.TileLayer;
import com.jcloisterzone.ui.grid.layer.TilePlacementLayer;
import com.jcloisterzone.ui.grid.layer.TokenLayer;
import com.jcloisterzone.ui.grid.layer.TowerLayer;
import com.jcloisterzone.ui.view.GameView;


@SuppressWarnings("serial")
public class MainPanel extends JPanel {

    private final Client client;
    private final GameView gameView;
    private final GameController gc;
    private final Game game;

    private GridPanel gridPanel;
    private ControlPanel controlPanel;
    private ChatPanel chatPanel;

    private FarmHintsLayer farmHintLayer;
    private PlacementHistory placementHistoryLayer;

    public MainPanel(Client client, GameView gameView, ChatPanel chatPanel) {
        this.client = client;
        this.gameView = gameView;
        this.gc = gameView.getGameController();
        this.game = gc.getGame();
        this.chatPanel = chatPanel;
        gc.register(this);

        setLayout(new BorderLayout());
    }

    public GridPanel getGridPanel() {
        return gridPanel;
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public void setShowFarmHints(boolean showFarmHints) {
        if (showFarmHints) {
            getGridPanel().showLayer(farmHintLayer);
        } else {
            getGridPanel().hideLayer(farmHintLayer);
        }
    }

    public void started(Snapshot snapshot) {
        controlPanel = new ControlPanel(gameView);
        gridPanel = new GridPanel(client, gameView, controlPanel, chatPanel, snapshot);
        MeepleLayer meepleLayer = new MeepleLayer(gridPanel, gc);
        TilePlacementLayer tilePlacementLayer = new TilePlacementLayer(gridPanel, gc);
        TileLayer tileLayer = new TileLayer(gridPanel, gc);
        tileLayer.setTilePlacmentLayer(tilePlacementLayer);
        farmHintLayer = new FarmHintsLayer(gridPanel, gc);

        CapabilitiesState capabs = game.getState().getCapabilities();

        gridPanel.addLayer(tilePlacementLayer, false);
        gridPanel.addLayer(tileLayer);  //zindex 2

        if (capabs.contains(TowerCapability.class)) {
            gridPanel.addLayer(new TowerLayer(gridPanel, gc)); //5
        }

        gridPanel.addLayer(farmHintLayer, false); //zindex 10


        if (capabs.contains(CastleCapability.class)) {
            gridPanel.addLayer(new CastleLayer(gridPanel, gc)); //45
        }
        if (capabs.contains(PlagueCapability.class)) {
            gridPanel.addLayer(new PlagueLayer(gridPanel, gc)); //45
        }

        gridPanel.addLayer(meepleLayer); //zindex 50
        //TODO add always
        if (capabs.contains(LittleBuildingsCapability.class) ||
            capabs.contains(TunnelCapability.class) ) {
            gridPanel.addLayer(new TokenLayer(gridPanel, gc));
        }

        if (capabs.contains(BridgeCapability.class)) {
            BridgeLayer bridgeLayer = new BridgeLayer(gridPanel, gc);
            bridgeLayer.setMeepleLayer(meepleLayer);
            gridPanel.addLayer(bridgeLayer);
        }

        if (capabs.contains(GoldminesCapability.class)) {
            gridPanel.addLayer(new GoldLayer(gridPanel, gc));
        }

        gridPanel.addLayer(new FollowerAreaLayer(gridPanel, gc, meepleLayer), false); //70

//        if (capabs.contains(DragonCapability.class)) {
//            gridPanel.addLayer(new DragonLayer(gridPanel, gc));
//        }

        gridPanel.addLayer(new FeatureAreaLayer(gridPanel, gc), false);
        gridPanel.addLayer(new TileActionLayer(gridPanel, gc), false);

//        if (capabs.contains(AbbeyCapability.class)) {
//            gridPanel.addLayer(new AbbeyPlacementLayer(gridPanel, gc), false);
//        }
        if (capabs.contains(LittleBuildingsCapability.class)) {
            gridPanel.addLayer(new LittleBuildingActionLayer(gridPanel, gc), false); //100
        }

        //abstractare - zindex 100
        //tile placement 3

        gridPanel.addLayer(new AnimationLayer(gridPanel, gc)); //zindex 800

        placementHistoryLayer = new PlacementHistory(gridPanel, gc);
        gridPanel.addLayer(placementHistoryLayer, false);

        add(gridPanel);
    }

    public void toggleRecentHistory(boolean show) {
        if (show) {
            gridPanel.showLayer(placementHistoryLayer);
        } else {
            gridPanel.hideLayer(placementHistoryLayer);
        }
    }

    public void closeGame() {
        gridPanel.clearActionDecorations();
        gridPanel.hideLayer(TilePlacementLayer.class);
        gridPanel.removeInteractionPanels();
    }


//    private void hideMageWitchPanel() {
//        if (gridPanel.getMageWitchPanel() != null) {
//            gridPanel.remove(gridPanel.getMageWitchPanel());
//            gridPanel.revalidate();
//        }
//    }

    @Subscribe
    public void cornOptionSelected(CornCirclesOptionEvent ev) {
        for (Component comp : gridPanel.getComponents()) {
            if (comp instanceof CornCirclesPanel) {
                gridPanel.remove(comp);
                gridPanel.revalidate();
            }
        }
    }
}
