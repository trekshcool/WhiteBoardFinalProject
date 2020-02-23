package com.example.multiboard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class PaintingActivity extends AppCompatActivity {

    private static final String TAG = "PaintingActivity";
    private Whiteboard whiteboard;
    private String whiteBoardName;
    private Button closeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_painting);

        Log.v(TAG, "log load");

        extractDataFromIntent();
        setupEndActivityButton();
    }

    //Extract data from Main's intent
    private void extractDataFromIntent() {
        Intent intent = getIntent();
        Log.v(TAG, "get intent from Main");

        whiteBoardName = intent.getStringExtra("whiteBoardName");
        Log.v(TAG, "whiteBoardName: " + whiteBoardName);

        Log.v("Lab1", "finish extract");
    }

    //close Painting Activity
    private void setupEndActivityButton() {
        //setup and listen to
        closeButton = (Button) findViewById(R.id.changeToMain);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
//                intent.putExtra("whiteBoardName", whiteBoardName);
//                Log.v("Lab1", intent.getStringExtra("whiteBoardName"));

                //pass entry and close Paint
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    public static Intent makeIntent(Context context, String whiteBoardName){
        Log.v(TAG, "make new intent");

        Intent intent = new Intent(context, PaintingActivity.class);

        Log.v(TAG, "get Whiteboards");
        intent.putExtra("whiteBoardName", whiteBoardName);
        Log.v("Lab1", "name: " + whiteBoardName);

        return intent;
    }
}
