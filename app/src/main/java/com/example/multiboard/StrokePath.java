package com.example.multiboard;

import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information about a given mark on the board, from finger down to finger up.
 */
public class StrokePath {

    private int color;
    private float strokeWidth;
    private List<Float> pathPoints;

    /**
     * Construct a StrokePath with default properties
     */
    public StrokePath() {
        color = PaintView.DEFAULT_COLOR;
        strokeWidth = PaintView.DEFAULT_SIZE;
        pathPoints = null;
    }

    /**
     * Construct a StrokePath with the given variables.
     * @param color the color of the stroke.
     * @param strokeWidth the width of the stroke.
     */
    public StrokePath(int color, float strokeWidth) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        pathPoints = new ArrayList<>();
    }

    /**
     * Adds a point in the drawing path.
     * @param x the x-coordinate of the point.
     * @param y the y-coordinate of the point.
     */
    public void addPathPoint(float x, float y) {
        pathPoints.add(x);
        pathPoints.add(y);
    }

    /**
     * @return the color of the stroke.
     */
    public int getColor() {
        return color;
    }

    /**
     * @return the width of the stroke.
     */
    public float getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * @return the List of path points.
     */
    public List<Float> getPathPoints() {
        return pathPoints;
    }

    /**
     * @return the Path object that has the stroke's location and shape information.
     */
    Path getPath() {
        // Start a new path
        Path path = new Path();

        // No path
        if (pathPoints.size() < 2) {
            return null;
        }

        // Set path to start position
        float prevX = pathPoints.get(0);
        float prevY = pathPoints.get(1);
        path.reset();
        path.moveTo(prevX, prevY);

        // Move path through all the points
        float x = prevX;
        float y = prevY;
        for (int i = 2; i < pathPoints.size(); i += 2) {
            prevX = x;
            prevY = y;
            x = pathPoints.get(i);
            y = pathPoints.get(i + 1);
            path.quadTo(prevX, prevY, x, y);
        }

        return path;
    }
}
