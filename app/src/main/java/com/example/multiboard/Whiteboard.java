package com.example.multiboard;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Represents an individual Whiteboard, storing pixels for painting and rendering and GPS
 * information for checking user coordinates.
 */
public class Whiteboard {

    private final static int WIDTH = 5000;
    private final static int HEIGHT = 5000;
    public final static int MAX_INK = 150;

    private String mName; // Unique name representing this Whiteboard
    private Pixel[][] mBoard; // The board data itself
    private int mInkLevel;

    // TODO Whiteboard GPS location and GeoFence structure, getters and setters

    /**
     * Constructs a Whiteboard with the given name.
     * @param name the name for this Whiteboard. (All Whiteboards must have a name).
     */
    public Whiteboard(String name) {
        mName = name;
        mInkLevel = 0;
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

    /**
     * Prints all the necessary data to a given Whiteboard list card.
     * @param context the current context (for resource access).
     * @param view the ViewGroup corresponding to the Whiteboard's list card.
     */
    public void setupListCard(Context context, View view) {
        // Set Whiteboard text views
        ((TextView) view.findViewById(R.id.text_name)).setText(getName());
        int inkLevel = getInkLevel() * 100 / Whiteboard.MAX_INK;
        String inkMessage = context.getString(R.string.text_card_ink_level) + " " + inkLevel + "%";
        ((TextView) view.findViewById(R.id.text_ink_level)).setText(inkMessage);
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getInkLevel() {
        return mInkLevel;
    }

    public void setInkLevel(int inkLevel) {
        mInkLevel = inkLevel;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }
}
