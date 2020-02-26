package com.example.multiboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PaintingActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_PAINT = 4345;

    // Debugging
    private static final String TAG = "PaintingActivity";

    // Whiteboard variables
    private Whiteboard whiteboard;
    private String whiteboardName;

    // GUI Views
    private boolean isPixelGUISetup = false;
    private TextView textBoardName;
    private PaintView paintView;

    // User information
    private String mUserId;

    // Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;

    // Callback for Firebase Whiteboard data
    ValueEventListener whiteboardListener = new ValueEventListener() {
        /**
         * This method is called when the Activity is started to get information
         * for the current Whiteboard.
         * @param dataSnapshot new data.
         */
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Iterate over all modified whiteboards
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                // Get Whiteboard object from database
                Whiteboard dsWhiteboard = ds.getValue(Whiteboard.class);
                if (dsWhiteboard.getName().equals(whiteboardName)) {
                    whiteboard = dsWhiteboard;
                    whiteboard.initBoard();
                }
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
                if (ds.getKey() == null) {
                    Log.e(TAG, "DataSnapshot key is null");
                    continue;
                }

                // Get pixel coordinates
                int pixelId = Integer.parseInt(ds.getKey());
                int x = whiteboard.getXFromId(pixelId);
                int y = whiteboard.getYFromId(pixelId);

                // Get Pixel object from database and write to the Whiteboard object
                Pixel newPixel = ds.getValue(Pixel.class);
                whiteboard.writePixel(x, y, newPixel);

                // Update GUI
                updatePixelGUI(pixelId, newPixel.getColor());
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

        paintView = findViewById(R.id.paint);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);

        // Find views
        textBoardName = findViewById(R.id.text_board_name);

        // Setup Whiteboard using the extras that were passed in
        extractDataFromIntent();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Get Whiteboard data from Firebase
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference
                .child("whiteboards")
                .addValueEventListener(whiteboardListener);

        // Get Whiteboard Pixel data and start listening for Pixel updates
        mFirebaseDatabaseReference
                .child("board-data")
                .child(whiteboardName)
                .addValueEventListener(pixelListener);

        ImageButton popb = (ImageButton) findViewById(R.id.paintMenu);

        popb.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(PaintingActivity.this, PaintPopUp.class));
            }
        });
    }

    private void colorPixel(int x, int y, int color) {
        // Write to local Whiteboard object
        Pixel pixel = new Pixel(mUserId, color);
        whiteboard.writePixel(x, y, pixel);

        // Upload new Pixel object to database
        int pixelId = whiteboard.getIdFromXY(x, y);
        mFirebaseDatabaseReference
                .child("board-data")
                .child(whiteboardName)
                .child(Integer.toString(pixelId))
                .setValue(pixel);

        // Update GUI
        updatePixelGUI(pixelId, color);
    }

    private void updatePixelGUI(int pixelId, int newColor) {
        // Make sure the GUI board exists
        if (!isPixelGUISetup) {
            setupPixelGUI();
        }

        // TODO: Update the GUI with the new Pixel color
    }

    /**
     * Called after the Whiteboard data has been fetched from the database, but before any calls
     * to updatePixelGUI().
     */
    private void setupPixelGUI() {
        // TODO: Create a bunch of pixel Views in the GUI based on the current whiteboard

        isPixelGUISetup = true;
    }

    /**
     * Extract data from MainActivity's intent.
     */
    private void extractDataFromIntent() {
        Intent intent = getIntent();

        // Get Whiteboard name
        whiteboardName = intent.getStringExtra("whiteboardName");
        textBoardName.setText(whiteboardName);

        // Get user's ID
        mUserId = intent.getStringExtra("userID");
    }

    /**
     * Create an Intent to launch the PaintingActivity with the given Whiteboard name.
     * @param context current context to launch the Activity with.
     * @param whiteboardName the name of the Whiteboard to paint on.
     * @param userId the ID of the current user.
     * @return the new Intent ready to be started.
     */
    public static Intent makeIntent(Context context, String whiteboardName, String userId) {
        // Create the intent with an extra for the Whiteboard name
        Intent intent = new Intent(context, PaintingActivity.class);
        intent.putExtra("whiteboardName", whiteboardName);
        intent.putExtra("userId", userId);
        return intent;
    }


    /**
     * Create an Intent to popup the given paint and pointsize.
     * @param whiteboard the Whiteboard object to paint.
     */
    public void openPaintPopUp(PaintView paint ){
        Intent intent = PaintingActivity.makeIntent(
                PaintingActivity.this,
                paint.getColor,
                paint.getStrokeWidth);
        startActivityForResult(intent, REQUEST_CODE_PAINT);
    }

    //Return infomation on return
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "get from PaintingActivity");

        switch (requestCode){
            case REQUEST_CODE_PAINT: {
                Log.v(TAG, "request code corect");
                if (resultCode == Activity.RESULT_OK) {
                    Log.v(TAG, "Activity ok");
                    //Log.v(TAG, data.getStringExtra("count"));
                    setColor(data.getIntExtra("color"));
                    setStrokeWidth(data.getIntExtra("Size"))
                } else { // if fails
                    Log.v(TAG, "Activity canciled");
                }
                break;
            }
            //if request is wrong
            default:  Log.v(TAG, "request code wrong");
                break;
        }
    }
}
