package com.jcloisterzone.ui.resources;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

public class FeatureArea {

    public final static int DEFAULT_FARM_ZINDEX = 10;
    public final static int DEFAULT_CITY_ZINDEX = 20;
    public final static int DEFAULT_ROAD_ZINDEX = 30;
    public final static int DEFAULT_STRUCTURE_ZINDEX = 40;
    public final static int DEFAULT_BRIDGE_ZINDEX = 50;

    private final Area trackingArea; //mouse tracking area
    private final Area displayArea; //mouse tracking area
    private final int zIndex;
    private final Color forceAreaColor;
    private final AreaRotationScaling rotationScaling;

    public FeatureArea(Area trackingArea, int zIndex) {
        this(trackingArea, null, zIndex, null, AreaRotationScaling.NORMAL);
    }

    public FeatureArea(Area trackingArea, Area displayArea, int zIndex) {
        this(trackingArea, displayArea, zIndex, null, AreaRotationScaling.NORMAL);
    }

    private FeatureArea(Area trackingArea, Area displayArea, int zIndex, Color forceAreaColor, AreaRotationScaling rotationScaling) {
        this.trackingArea = trackingArea;
        this.displayArea = displayArea;
        this.zIndex = zIndex;
        this.forceAreaColor = forceAreaColor;
        this.rotationScaling = rotationScaling;
    }

    public FeatureArea transform(AffineTransform t) {
        Area trackingArea = null, displayArea = null;
        if (this.trackingArea != null) {
            trackingArea  = this.trackingArea.createTransformedArea(t);
        }
        if (this.displayArea != null) {
            displayArea = this.displayArea.createTransformedArea(t);
        }
        return new FeatureArea(trackingArea, displayArea, zIndex, forceAreaColor, rotationScaling);
    }

    public Area getTrackingArea() {
        return trackingArea;
    }

    public Area getDisplayArea() {
        return displayArea;
    }

    public int getzIndex() {
        return zIndex;
    }

    public Color getForceAreaColor() {
        return forceAreaColor;
    }

    public AreaRotationScaling getRotationScaling() {
        return rotationScaling;
    }

    public FeatureArea setForceAreaColor(Color forceAreaColor) {
        return new FeatureArea(trackingArea, displayArea, zIndex, forceAreaColor, rotationScaling);
    }

    public FeatureArea setRotationScaling(AreaRotationScaling rotationScaling) {
        return new FeatureArea(trackingArea, displayArea, zIndex, forceAreaColor, rotationScaling);
    }

    @Override
    public String toString() {
        return zIndex + "/" + trackingArea.toString();
    }
}
