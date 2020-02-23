package com.example.multiboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.Locale;

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
    private double mLatitude; // Coordinates for the centroid
    private double mLongitude;
    private double mRadius; // Radius of geofence circle
    private int mInkLevel;
    private boolean active = false; // Whether the Whiteboard is in range or not
    private CardView mCardView; // CardView in list of Whiteboards
    private TextView distText;

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

    /**
     * Load in and start listening for Pixel data from the database's board-data node.
     */
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
        mCardView = (CardView) view;
        ((TextView) view.findViewById(R.id.text_name)).setText(getName());

        int inkLevel = getInkLevel() * 100 / Whiteboard.MAX_INK;
        String inkMessage = context.getString(R.string.text_card_ink_level) + " " + inkLevel + "%";
        ((TextView) view.findViewById(R.id.text_ink_level)).setText(inkMessage);

        distText = view.findViewById(R.id.text_dist);
        distText.setText(R.string.default_dist_text);
    }

    /**
     * Updates the current distance display for the Whiteboard and activates/deactivates the
     * CardView as necessary.
     * @param latitude the user's latitude.
     * @param longitude the user's longitude.
     */
    public void updateDistance(double latitude, double longitude) {
        double distance = findDistance(latitude, longitude);

        // Check for activation within radius
        if (distance <= getRadius()) {
            distText.setText(R.string.dist_text_available);
            activate();
        } else {
            distText.setText(String.format(Locale.ENGLISH, "Distance: %.0f meters", distance));
            deactivate();
        }
    }

    /**
     * Makes the Whiteboard's CardView clickable and brightly colored.
     */
    public void activate() {
        // TODO: Make clickable and bright
        active = true;
    }

    /**
     * Makes the Whiteboard's CardView unclickable and grayed out.
     */
    public void deactivate() {
        // TODO: Make un-clickable and grayed
        active = false;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getRadius() {
        return mRadius;
    }

    public void setRadius(double mRadius) {
        this.mRadius = mRadius;
    }

    int getInkLevel() {
        return mInkLevel;
    }

    void setInkLevel(int inkLevel) {
        mInkLevel = inkLevel;
    }

    boolean isActive() {
        return active;
    }

    CardView getCardView() {
        return mCardView;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    /**
     * Calculate distance from whiteboard. (Use Haversine formula for spherical distance).
     * @param latitude latitude to check.
     * @param longitude longitude to check.
     * @return distance from given coordinates to this whiteboard.
     */
    private double findDistance(double latitude, double longitude){
        // Radius of earth in KM
        double R = 6378.137;

        // Convert to radians
        double userLat = latitude * Math.PI / 180;
        double userLon = longitude * Math.PI / 180;
        double wbLat = getLatitude() * Math.PI / 180;
        double wbLon = getLongitude() * Math.PI / 180;

        // Get deltas
        double dLat = userLat - wbLat;
        double dLon = userLon - wbLon;

        // Formula
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(userLat) * Math.cos(wbLat) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;

        return d * 1000; // In meters
    }
}
