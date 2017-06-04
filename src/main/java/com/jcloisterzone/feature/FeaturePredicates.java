package com.jcloisterzone.feature;

import java.util.function.Predicate;

public final class FeaturePredicates {

    private FeaturePredicates() { }

    public static Predicate<Feature> uncompleted() {
        return f -> {
            if (f instanceof Completable) {
                return !((Completable) f).isCompleted();
            } else {
                return true;
            }
        };
    }


}
