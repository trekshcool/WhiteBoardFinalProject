package com.example.multiboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // Shared Preferences variables
    private final String SALT_KEY = "SALT_KEY";
    private SharedPreferences mSharedPreferences;
    private String sharedPrefFile = "com.example.multiboard";

    // Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;

    // User information
    private final int SALT_LENGTH = 64;
    private final String SALT_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private int mUserId;
    private String mUserSalt;

    // GUI variables
    LinearLayout linearWhiteboards;

    // Map of all present Whiteboards to their card views
    private HashMap<Whiteboard, View> mWhiteboardMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreferences initialization/restoration
        // Generate salt for username if none exists, store in shared preferences
        mSharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        mUserSalt = mSharedPreferences.getString(SALT_KEY, genSaltString());
        SharedPreferences.Editor preferencesEditor = mSharedPreferences.edit();
        preferencesEditor.putString(SALT_KEY, mUserSalt);
        preferencesEditor.apply();

        // Initialize user ID
        mUserId = mUserSalt.hashCode();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            // Anonymize user data by salting and hashing the name before storing it
            String dispName = mFirebaseUser.getDisplayName();
            if (dispName != null) {
                String saltedName = dispName + mUserSalt;
                mUserId = saltedName.hashCode();
                Log.d(TAG, dispName);
                Log.d(TAG, saltedName);
                Log.d(TAG, Integer.toString(mUserId));
            }
        }

        // Other Firebase
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Find views
        linearWhiteboards = findViewById(R.id.linear_whiteboards);

        // Whiteboards
        mWhiteboardMap = new HashMap<>();
        populateWhiteboardCards();
    }

    public void cardClick(View v) {
        // TODO: Get Whiteboard and start whiteboard activity
    }

    /**
     * Gets nearby Whiteboards and populates the layout and hashmap with their info.
     */
    private void populateWhiteboardCards() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < 15; i++) {
            Whiteboard wb = new Whiteboard("test" + i);
            View cardView = inflater.inflate(R.layout.whiteboard_list_card, linearWhiteboards);
            mWhiteboardMap.put(wb, cardView);
        }
    }

    /**
     * Generate a salt to modify the user's display name uniquely.
     * @return a random string of length SALT_LENGTH using the SALT_ALPHABET.
     */
    private String genSaltString() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();

        // Select SALT_LENGTH characters
        for (int i = 0; i < SALT_LENGTH; i++) {
            // Pick one letter in SALT_ALPHABET
            char randChar = SALT_ALPHABET.charAt(r.nextInt(SALT_ALPHABET.length()));
            sb.append(randChar);
        }

        // Build the string and return
        return sb.toString();
    }
}
