package com.jcloisterzone.feature;

public interface Completable extends Scoreable {

    boolean isOpen();
    default boolean isCompleted() {
        return !isOpen();
    }

}
