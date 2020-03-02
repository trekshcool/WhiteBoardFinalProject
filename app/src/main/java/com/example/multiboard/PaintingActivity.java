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

import java.util.Timer;
import java.util.TimerTask;

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
    private TextView textBoardName;
    private PaintView paintView;
    private ImageButton buttonPopup;
    private MenuPopupHelper popupPaint;

    // User information
    private float inkLevel;

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
                    whiteboard.setInkLevel(inkLevel);
                    paintView.init(Whiteboard.WIDTH, Whiteboard.HEIGHT, whiteboard);
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

        // Get Whiteboard data from Firebase and start listening for updates
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference
                .child("whiteboards")
                .addValueEventListener(whiteboardListener);
    }

    /**
     * Extract data from MainActivity's intent.
     */
    private void extractDataFromIntent() {
        Intent intent = getIntent();

        // Get Whiteboard name
        whiteboardName = intent.getStringExtra("whiteboardName");
        textBoardName.setText(whiteboardName);

        // Get user's information
        inkLevel = intent.getFloatExtra("inkLevel", 0f);
    }

    /**
     * Create an Intent to launch the PaintingActivity with the given Whiteboard name.
     * @param context current context to launch the Activity with.
     * @param whiteboardName the name of the Whiteboard to paint on.
     * @param inkLevel the inkLevel of the current user for this Whiteboard.
     * @return the new Intent ready to be started.
     */
    public static Intent makeIntent(Context context, String whiteboardName, float inkLevel) {
        // Create the intent with an extra for the Whiteboard name
        Intent intent = new Intent(context, PaintingActivity.class);
        intent.putExtra("whiteboardName", whiteboardName);
        intent.putExtra("inkLevel", inkLevel);
        return intent;
    }


    /**
     * Display the Popup Menu (called when buttonPopup is clicked).
     * @param view the button that was clicked.
     */
    public void openPopupPaint(View view){
        popupPaint.show();
    }

}
