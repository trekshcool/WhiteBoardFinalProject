package com.example.multiboard;

/**
 * Represents an individual Whiteboard, storing pixels for painting and rendering and GPS
 * information for checking user coordinates.
 */
public class Whiteboard {

    private final static int WIDTH = 5000;
    private final static int HEIGHT = 5000;

    private String mName; // Unique name representing this Whiteboard
    private Pixel[][] mBoard = new Pixel[WIDTH][HEIGHT]; // The board data itself

    // TODO Whiteboard GPS location and GeoFence structure, getters and setters

    /**
     * Constructs a Whiteboard with the given name.
     * @param name the name for this Whiteboard. (All Whiteboards must have a name).
     */
    public Whiteboard(String name) {
        mName = name;
    }

    /**
     * Initialize a blank board of new Pixels.
     */
    private void initBoard() {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                mBoard[i][j] = new Pixel();
            }
        }
    }

    private void loadBoard() {
        // TODO: Get pixel data from database and write to board
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }
}
