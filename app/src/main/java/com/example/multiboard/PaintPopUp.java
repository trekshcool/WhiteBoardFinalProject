package com.example.multiboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;

class PaintPopUp extends Activity {

    private int mainColor;
    private float mainSize;
    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout(width, height);

        ImageButton blackButton = (ImageButton) findViewById(R.id.BlackButton);
        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainColor = 1;
            }
        });

        ImageButton blueButton = (ImageButton) findViewById(R.id.BlueButton);
        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainColor = 2;
            }
        });

        ImageButton brownButton = (ImageButton) findViewById(R.id.BrownButton);
        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainColor = 3;
            }
        });

        ImageButton greenButton = (ImageButton) findViewById(R.id.GreenButton);
        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainColor = 4;
            }
        });

        ImageButton yellowButton = (ImageButton) findViewById(R.id.YellowButton);
        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainColor = 5;
            }
        });

        ImageButton orangeButton = (ImageButton) findViewById(R.id.OrangeButton);
        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainColor = 6;
            }
        });

        ImageButton redButton = (ImageButton) findViewById(R.id.RedeButton);
        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainColor = 7;
            }
        });

    }


    private void extractDataFromIntent() {
        Intent intent = getIntent();

        mainColor = intent.getIntExtra("color",0);
        mainSize = intent.getFloatExtra("size", 0f);

    }


    public static Intent makeIntent(Context context, int color, float size) {
        // Create the intent with an extra for the Whiteboard name
        Intent intent = new Intent(context, PaintingActivity.class);
        intent.putExtra("color", color);
        intent.putExtra("size", size);
        return intent;
    }
}
