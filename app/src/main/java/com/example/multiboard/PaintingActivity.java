package com.example.multiboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    // Paint colors
    private int BLACK = Color.BLACK;
    private int RED = 0xFFA61A1A;
    private int ORANGE = 0xFFD9971E;
    private int YELLOW = 0xFFD3D91E;
    private int GREEN = 0xFF2B7B36;
    private int BLUE = 0xFF2B2D7A;
    private int PURPLE = 0xFF6B4C9E;
    private int PINK = 0xFFF586B6;
    private int BROWN = 0xFF6C4F30;

    // GUI Views
    private boolean isPixelGUISetup = false;
    private TextView textBoardName;
    private PaintView paintView;
    private ImageButton buttonPopup;
    private MenuPopupHelper popupPaint;

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
                    paintView.init(Whiteboard.WIDTH, Whiteboard.HEIGHT);
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

                // UpdatePaint
                updatePaintLevel(whiteboard.getInkLevel());
            }
        }

        /**
         * change icon based on whitboard ink level percentage
         * @param inkLevel double the current amout the user has left
         */
        private void updatePaintLevel(double inkLevel) {
            ImageView inkMeeter = (ImageView) findViewById(R.id.InkMeeter);

            //Full bottle
            if(inkLevel == (double) Whiteboard.MAX_INK){
                inkMeeter.setImageResource(R.drawable.ink_bottle_4);
            }

            //25% or less
            else if ((inkLevel / (double) Whiteboard.MAX_INK) <= 0.25){
                inkMeeter.setImageResource(R.drawable.ink_bottle_1);
            }

            //50% or less
            else if ((inkLevel / (double) Whiteboard.MAX_INK) <= 0.50){
                inkMeeter.setImageResource(R.drawable.ink_bottle_2);
            }

            //75% or less
            else if ((inkLevel / (double) Whiteboard.MAX_INK) <= 0.75){
                inkMeeter.setImageResource(R.drawable.ink_bottle_3);
            }

            //empty
            //else if (inkLevel == 0){}
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Getting data failed
            Log.w(TAG, databaseError.toException());
        }
    };

    // Popup menu callback
    MenuBuilder.Callback popupListener = new MenuBuilder.Callback() {
        /**
         * This method is called when an item in the popup menu is clicked.
         * @param builder the menu that was clicked.
         * @param item the MenuItem that was clicked.
         * @return true if the event was handled.
         */
        @Override
        public boolean onMenuItemSelected(MenuBuilder builder, MenuItem item) {
            switch (item.getTitle().toString()) {
                case "Black":
                    paintView.setColor(BLACK);
                    return true;
                case "Red":
                    paintView.setColor(RED);
                    return true;
                case "Orange":
                    paintView.setColor(ORANGE);
                    return true;
                case "Yellow":
                    paintView.setColor(YELLOW);
                    return true;
                case "Green":
                    paintView.setColor(GREEN);
                    return true;
                case "Blue":
                    paintView.setColor(BLUE);
                    return true;
                case "Purple":
                    paintView.setColor(PURPLE);
                    return true;
                case "Pink":
                    paintView.setColor(PINK);
                    return true;
                case "Brown":
                    paintView.setColor(BROWN);
                    return true;
            }

            // Event not handled
            return false;
        }

        @Override
        public void onMenuModeChange(MenuBuilder builder) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting);

        // GUI
        textBoardName = findViewById(R.id.text_board_name);
        paintView = findViewById(R.id.paint);
        buttonPopup = findViewById(R.id.button_popup);

        // Popup GUI
        MenuBuilder menuBuilder = new MenuBuilder(this);
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.popup_paint, menuBuilder);
        popupPaint = new MenuPopupHelper(this, menuBuilder, buttonPopup);
        popupPaint.setForceShowIcon(true);
        menuBuilder.setCallback(popupListener);

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

//        ImageButton popb = (ImageButton) findViewById(R.id.paintMenu);
//
//        popb.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(PaintingActivity.this, popupPaint.class));
//            }
//        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        whiteboard.deleteBoard();
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
     * Display the Popup Menu (called when buttonPopup is clicked).
     * @param view the button that was clicked.
     */
    public void openPopupPaint(View view){
        popupPaint.show();
//        Intent intent = PaintingActivity.makeIntent(
//                PaintingActivity.this,
//                paint.getColor,
//                paint.getStrokeWidth);
//        startActivityForResult(intent, REQUEST_CODE_PAINT);
    }

}
