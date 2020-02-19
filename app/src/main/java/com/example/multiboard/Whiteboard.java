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
    private float mLatitude; // Coordinates for the centroid
    private float mLongitude;
    private float mRadius; // Radius of geofence circle
    private int mInkLevel;

    // TODO Whiteboard GPS location and GeoFence structure, getters and setters

    /**
     * Construct an empty Whiteboard.
     */
    public Whiteboard() {
        mName = "no_name";
        mLatitude = 0;
        mLongitude = 0;
        mRadius = 1;
    }

    /**
     * Construct a Whiteboard with the given information.
     * @param name the Whiteboard's name.
     * @param latitude the latitude coordinate of the location.
     * @param longitude the longitude coordinate of the location.
     * @param radius the radius of the geofence.
     */
    public Whiteboard(String name, float latitude, float longitude, float radius) {
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
        mRadius = radius;
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

    public void activate() {
        // TODO: Make clickable and bright
    }

    public void deactivate() {
        // TODO: Make un-clickable and grayed
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public float getLatitude() {
        return mLatitude;
    }

    public void setLatitude(float mLatitude) {
        this.mLatitude = mLatitude;
    }

    public float getLongitude() {
        return mLongitude;
    }

    public void setLongitude(float mLongitude) {
        this.mLongitude = mLongitude;
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float mRadius) {
        this.mRadius = mRadius;
    }

    int getInkLevel() {
        return mInkLevel;
    }

    void setInkLevel(int inkLevel) {
        mInkLevel = inkLevel;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }
}
