package com.example.multiboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 * This Activity is the default starting place for the app and allows the user to sign in.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
