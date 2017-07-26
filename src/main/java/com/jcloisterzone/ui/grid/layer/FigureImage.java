package com.jcloisterzone.ui.grid.layer;

import java.awt.Image;

import com.jcloisterzone.figure.Figure;
import com.jcloisterzone.ui.ImmutablePoint;

public class FigureImage {

    Figure<?> fig;
    ImmutablePoint offset;
    Image img;
    double scaleX, scaleY;

    private Image scaledImage;
    private int scaledForSize = -1;

    public FigureImage(Figure<?> fig) {
        this.fig = fig;
    }

    public Image getScaledInstance(int baseSize) {
        if (scaledForSize != baseSize) {
            int width = (int) (baseSize * scaleX);
            int height = (int) (baseSize * scaleX);
            scaledImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            scaledForSize = baseSize;
        }
        return scaledImage;
    }

}
