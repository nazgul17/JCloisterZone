package com.jcloisterzone.feature;

import static com.jcloisterzone.ui.I18nUtils._;

import com.jcloisterzone.board.Edge;
import com.jcloisterzone.board.pointer.FeaturePointer;

import io.vavr.collection.List;
import io.vavr.collection.Set;

/*
 * Bridge is used only for initialFeature. When placed on board it's immediately converted to plain Road.
 */
public class Bridge extends Road {

    private static final long serialVersionUID = 1L;

    public Bridge(List<FeaturePointer> places, Set<Edge> openEdges) {
        super(places, openEdges);
    }

    public static String name() {
        return _("Bridge");
    }
}
