package com.jcloisterzone.event;

import com.jcloisterzone.IPlayer;

public class MoveEvent<T> extends PlayEvent {

    final T from, to;

    public MoveEvent(IPlayer triggeringPlayer, T from, T to) {
        this(0, triggeringPlayer, from, to);
    }

    public MoveEvent(int type, IPlayer triggeringPlayer, T from, T to) {
        super(type, triggeringPlayer, null);
        this.from = from;
        this.to = to;
    }

    public T getFrom() {
        return from;
    }

    public T getTo() {
        return to;
    }

    @Override
    public String toString() {
        return super.toString() + " from:" + from + " to:" + to;
    }

}
