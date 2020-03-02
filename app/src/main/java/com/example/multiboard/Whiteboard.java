package com.example.multiboard;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import java.util.Locale;

/**
 * Represents an individual Whiteboard, storing user information and
 * GPS information for checking user coordinates.
 */
public class Whiteboard implements Comparable<Whiteboard> {

    // Constants
    private final static String TAG = "Whiteboard";
    final static int WIDTH = 1400;
    final static int HEIGHT = 2000;
    public final static int MAX_INK = 150;

    // Whiteboard variables
    private String mName; // Unique name representing this Whiteboard
    private double mLatitude; // Coordinates for the centroid
    private double mLongitude;
    private double mRadius; // Radius of geofence circle
    private int mInkLevel;
    private boolean active = false; // Whether the Whiteboard is in range or not
    private double distFromUser = Double.MAX_VALUE;

    // GUI Views
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
        distFromUser = findDistance(latitude, longitude);

        // Check for activation within radius
        if (distFromUser <= getRadius()) {
            distText.setText(R.string.dist_text_available);
            activate();
        } else {
            String text = String.format(Locale.ENGLISH, "Distance: %.0f meters", distFromUser);
            distText.setText(text);
            deactivate();
        }
    }

    /**
     * Makes the Whiteboard active and its CardView brightly colored.
     */
    private void activate() {
        Context context = mCardView.getContext();
        int colorId = R.color.color_card_active;
        mCardView.setCardBackgroundColor(context.getResources().getColor(colorId));

        // This makes the CardView respond to clicks
        active = true;
    }

    /**
     * Makes the Whiteboard inactive and its CardView grayed out.
     */
    private void deactivate() {
        Context context = mCardView.getContext();
        int colorId = R.color.color_card_inactive;
        mCardView.setCardBackgroundColor(context.getResources().getColor(colorId));

        // This makes the CardView ignore clicks
        active = false;
    }

    /**
     * @return Whiteboard name.
     */
    public String getName() {
        return mName;
    }

    /**
     * Set Whiteboard name.
     * @param name
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * @return Whiteboeard latitude.
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Set Whiteboard latitude location.
     * @param mLatitude
     */
    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    /**
     * @return Whiteboard latitude.
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Set Whiteboard longitude location.
     * @param mLongitude
     */
    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    /**
     * @return radius from Whiteboard center in meters.
     */
    public double getRadius() {
        return mRadius;
    }

    /**
     * Set radius from Whiteboard center.
     * @param mRadius in meters
     */
    public void setRadius(double mRadius) {
        this.mRadius = mRadius;
    }

    /**
     * @return amount of ink user has left.
     */
    int getInkLevel() {
        return mInkLevel;
    }

    /**
     * Set the user's ink level.
     * @param inkLevel new ink remaining.
     */
    void setInkLevel(int inkLevel) {
        mInkLevel = inkLevel;
    }

    /**
     * @return true if the board is within range (activated), false otherwise.
     */
    boolean isActive() {
        return active;
    }

    /**
     * @return the CardView associated with this Whiteboard.
     */
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

    /**
     * Compares to sort by distance from user. If distances are within a meter, sort by name.
     * @param other whiteboard to compare with.
     * @return positive int if this Whiteboard is greater in value, negative if less, 0 if same.
     */
    @Override
    public int compareTo(@NonNull Whiteboard other) {
        int deltaDist = (int)(this.distFromUser - other.distFromUser);
        if (deltaDist == 0) {
            // Compare names for alphabetical order if distances are within a meter
            return this.getName().compareTo(other.getName());
        } else {
            return deltaDist;
        }
    }

    /**
     * Compare if two Whiteboards have the same name.
     * @param otherObj other Whiteboard.
     * @return return true if equal, else false.
     */
    @Override
    public boolean equals(Object otherObj) {
        Whiteboard otherBoard = (Whiteboard) otherObj;
        return this.mName.equals(otherBoard.getName());
    }
}
