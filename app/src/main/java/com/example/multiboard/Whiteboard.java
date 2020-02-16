package com.example.multiboard;

import android.util.Log;

/**
 * Represents an individual Whiteboard, storing pixels for painting and rendering and GPS
 * information for checking user coordinates.
 */
public class Whiteboard {

    private final static int width = 10000;
    private final static int height = 10000;

    private String name; // Unique name representing this Whiteboard
    private Pixel[][] board = new Pixel[width][height]; // The board data itself

    public Whiteboard(String name) {
        this.name = name;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                board[j][i] = new Pixel();
            }
        }
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
