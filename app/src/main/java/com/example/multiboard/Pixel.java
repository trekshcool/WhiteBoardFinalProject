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

    /**
     * Default constructor uses null for user and default white color.
     */
    public Pixel() {
        this(null, DEFAULT_COLOR);
    }

    /**
     * Construct a Pixel object with the given user and color.
     * @param user user who last modified this Pixel.
     * @param color current color of this Pixel.
     */
    public Pixel(String user, int color) {
        this.user = user;
        this.color = color;
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
            return String.format("No user, #%8x", color);
        } else {
            return String.format("%s, #%8x", user, color);
        }
    }
}
