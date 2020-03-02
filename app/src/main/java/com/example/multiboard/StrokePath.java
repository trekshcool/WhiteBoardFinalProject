package com.example.multiboard;

import android.graphics.Path;

public class StrokePath {

    private int color;
    private float strokeWidth;
    private Path path;

    public StrokePath(int color, float strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }

    public int getColor() {
        return color;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public Path getPath() {
        return path;
    }
}
