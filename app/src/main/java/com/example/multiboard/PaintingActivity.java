package com.example.multiboard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PaintingActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "PaintingActivity";

    // Whiteboard variables
    private Whiteboard whiteboard;
    private String whiteboardName;

    // GUI Views
    private TextView textBoardName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting);

        // Find views
        textBoardName = findViewById(R.id.text_board_name);

        // Setup Whiteboard using the extras that were passed in
        extractDataFromIntent();
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
