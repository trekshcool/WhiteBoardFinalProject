package com.example.multiboard;

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
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
