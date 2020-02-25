package com.example.multiboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PaintingActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "PaintingActivity";

    // Whiteboard variables
    private Whiteboard whiteboard;
    private String whiteboardName;

    // GUI Views
    private TextView textBoardName;

    // User information
    private int mUserId;

    // Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;

    // Callback for Firebase Whiteboard data
    ValueEventListener whiteboardListener = new ValueEventListener() {
        /**
         * This method is called when the Activity is started to get information for the current
         * Whiteboard.
         * @param dataSnapshot new data.
         */
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Iterate over all modified whiteboards
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                // Get Whiteboard object from database
                whiteboard = ds.getValue(Whiteboard.class);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Getting data failed
            Log.w(TAG, databaseError.toException());
            finish();
        }
    };

    // Callback for Firebase Pixel updates
    ValueEventListener pixelListener = new ValueEventListener() {
        /**
         * This method is called when the board data is loaded and whenever a Pixel in the
         * 'board-data/BOARD_NAME' node of the database is modified. This allows the activity to
         * get realtime updates to the board's Pixels.
         * @param dataSnapshot new data.
         */
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Iterate over all modified Pixels
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                // Get Pixel object from database
                Pixel newPixel = ds.getValue(Pixel.class);
                // TODO: write pixel data based on database index (key)
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Getting data failed
            Log.w(TAG, databaseError.toException());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting);

        // Find views
        textBoardName = findViewById(R.id.text_board_name);

        // Setup Whiteboard using the extras that were passed in
        extractDataFromIntent();

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

        // Get Whiteboard data from Firebase
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child("whiteboards").child(whiteboardName)
                .addValueEventListener(whiteboardListener);

        // Get Whiteboard Pixel data and start listening for Pixel updates
        whiteboard.initBoard();
        mFirebaseDatabaseReference.child("board-data").child(whiteboardName)
                .addValueEventListener(pixelListener);
    }

    /**
     * Extract data from MainActivity's intent.
     */
    private void extractDataFromIntent() {
        Intent intent = getIntent();
        whiteboardName = intent.getStringExtra("whiteboardName");
        textBoardName.setText(whiteboardName);
    }

    /**
     * Create an Intent to launch the PaintingActivity with the given Whiteboard name.
     * @param context current context to launch the Activity with.
     * @param whiteboardName the name of the Whiteboard to paint on.
     * @return the new Intent ready to be started.
     */
    public static Intent makeIntent(Context context, String whiteboardName) {
        // Create the intent with an extra for the Whiteboard name
        Intent intent = new Intent(context, PaintingActivity.class);
        intent.putExtra("whiteboardName", whiteboardName);
        return intent;
    }
}
