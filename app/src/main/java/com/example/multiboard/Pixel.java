package com.example.multiboard;

import android.graphics.Color;

import androidx.annotation.NonNull;

/**
 * Represents a single pixel in a Whiteboard. Stores the user info and color.
 */
public class Pixel {

    private final static int DEFAULT_COLOR = Color.WHITE;

    private String user;
    private int color;

    public Pixel() {
        color = DEFAULT_COLOR;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override @NonNull
    public String toString() {
        if (user == null) {
            return "No user, " + Integer.toString(color, 16);
        } else {
            return user + ", " + Integer.toString(color, 16);
        }
    }
}
