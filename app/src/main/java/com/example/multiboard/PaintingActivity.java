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

    private static final String TAG = "PaintingActivity";
    private Whiteboard whiteboard;
    private String whiteboardName;

    // GUI Views
    private TextView textBoardName;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting);

        Log.v(TAG, "log load");

        // Find views
        textBoardName = findViewById(R.id.text_board_name);

        extractDataFromIntent();
        setupEndActivityButton();
    }

    // Extract data from Main's intent
    private void extractDataFromIntent() {
        Intent intent = getIntent();
        Log.v(TAG, "get intent from Main");

        whiteboardName = intent.getStringExtra("whiteboardName");
        Log.v(TAG, "whiteboardName: " + whiteboardName);
        Log.v("Lab1", "finish extract");

        textBoardName.setText(whiteboardName);
    }

    //close Painting Activity
    private void setupEndActivityButton() {
        // Setup and listen to
        closeButton = findViewById(R.id.btn_change_to_main);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();

                // Pass entry and close Paint
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    public static Intent makeIntent(Context context, String whiteboardName){
        Log.v(TAG, "make new intent");

        Intent intent = new Intent(context, PaintingActivity.class);

        intent.putExtra("whiteboardName", whiteboardName);
        Log.v(TAG, "name: " + whiteboardName);

        return intent;
    }
}
