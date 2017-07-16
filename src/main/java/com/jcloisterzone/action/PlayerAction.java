package com.jcloisterzone.action;

import com.jcloisterzone.ui.GameController;
import com.jcloisterzone.wsio.RmiProxy;

import io.vavr.collection.Iterator;
import io.vavr.collection.Set;

//TODO decouple UI ordering (comparable) outside actions
public abstract class PlayerAction<T> implements Iterable<T> {

    protected final Set<T> options;

    public PlayerAction(Set<T> options) {
       this.options = options;
    }

    public abstract void perform(GameController gc, T target);

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
