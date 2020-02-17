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
    private SharedPreferences mSharedPreferences;
    private String sharedPrefFile = "com.example.multiboard";

    // Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;

    // User information
    private int mUserId;

    // GUI variables
    LinearLayout linearWhiteboards;

    // Map of all present Whiteboards to their card views
    private HashMap<Whiteboard, View> mWhiteboardMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreferences initialization/restoration
        mSharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        // Initialize user ID
        mUserId = 0;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            // Anonymize user data by hashing the name to create an ID number before storing it
            String dispName = mFirebaseUser.getDisplayName();
            if (dispName != null) {
                mUserId = dispName.hashCode();
                Log.d(TAG, dispName);
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
        // Test code
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < 15; i++) {
            Whiteboard wb = new Whiteboard("test" + i);
            View cardView = inflater.inflate(R.layout.whiteboard_list_card, linearWhiteboards);
            mWhiteboardMap.put(wb, cardView);
        }

        // TODO: Retrieve Whiteboard data from Firebase
    }
}
