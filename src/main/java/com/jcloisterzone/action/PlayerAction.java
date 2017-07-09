package com.jcloisterzone.action;

import java.awt.Color;
import java.awt.Image;

import com.jcloisterzone.Player;
import com.jcloisterzone.ui.annotations.LinkedImage;
import com.jcloisterzone.ui.resources.LayeredImageDescriptor;
import com.jcloisterzone.ui.resources.ResourceManager;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Iterator;
import io.vavr.collection.Set;

//TODO decouple UI ordering (comparable) outside actions
public abstract class PlayerAction<T> implements Iterable<T> {

    protected final Set<T> options;

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
}
