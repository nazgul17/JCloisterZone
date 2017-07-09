package com.jcloisterzone.action;

import java.awt.Color;
import java.awt.Image;

import com.jcloisterzone.Player;
import com.jcloisterzone.ui.Client;
import com.jcloisterzone.ui.grid.ActionLayer;
import com.jcloisterzone.ui.grid.MainPanel;
import com.jcloisterzone.ui.resources.DisplayableEntity;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.view.GameView;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Iterator;
import io.vavr.collection.Set;

//TODO decouple UI ordering (comparable) outside actions
public abstract class PlayerAction<T> implements Comparable<PlayerAction<?>>, Iterable<T> {

    protected final Set<T> options;

    @Deprecated
    protected Client client;
    @Deprecated
    protected MainPanel mainPanel;

    public PlayerAction(Set<T> options) {
       this.options = options;
    }

    public abstract void perform(RmiProxy server, T target);

    @Override
    public Iterator<T> iterator() {
        return options.iterator();
    }

    public Set<T> getOptions() {
        return options;
    }

    public boolean isEmpty() {
        return options.isEmpty();
    }

    public Image getImage(Player player, boolean active) {
        return getImage(player != null && active ? player.getColors().getMeepleColor() : Color.GRAY);
    }


    protected final ActionLayer<?> getActionLayer(Class<? extends ActionLayer<?>> layerType) {
        return mainPanel.getGridPanel().findLayer(layerType);
    }

    abstract protected Class<? extends ActionLayer<?>> getActionLayerType();


    /** Called when user select action in action panel */
    public void select(boolean active) {
        @SuppressWarnings("unchecked")
        ActionLayer<? super PlayerAction<?>> layer = (ActionLayer<? super PlayerAction<?>>) getActionLayer(getActionLayerType());
        layer.setAction(active, this);
        mainPanel.getGridPanel().showLayer(layer);
    }

    /** Called when user deselect action in action panel */
    public void deselect() {
        @SuppressWarnings("unchecked")
        ActionLayer<? super PlayerAction<?>> layer = (ActionLayer<? super PlayerAction<?>>) getActionLayer(getActionLayerType());
        layer.setAction(false, null);
        mainPanel.getGridPanel().hideLayer(layer);
    }

    protected Image getImage(Color color) {
        if (!this.getClass().isAnnotationPresent(DisplayableEntity.class)) {
            throw new UnsupportedOperationException("Annotate with DisplayableEntity or override getImage()");
        }
        DisplayableEntity disp = this.getClass().getAnnotation(DisplayableEntity.class);
        return client.getResourceManager().getLayeredImage(new LayeredImageDescriptor(disp.value(), color));
    }


    protected int getSortOrder() {
        return 1024;
    }

    @Override
    public int compareTo(PlayerAction<?> o) {
        return getSortOrder() - o.getSortOrder();
    }

    public void setClient(Client client) {
        this.client = client;
        this.mainPanel = ((GameView) client.getView()).getMainPanel();
    }

    public MainPanel getMainPanel() {
        return mainPanel;
    }
}
