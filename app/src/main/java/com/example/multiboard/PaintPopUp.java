//package com.example.multiboard;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.DisplayMetrics;
//
//class PaintPopUp extends Activity {
//
//    int mainColor;
//    int mainSize;
//    @Override
//    protected  void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.popup);
//
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//
//        int width = displayMetrics.widthPixels;
//        int height = displayMetrics.heightPixels;
//
//        getWindow().setLayout(width, height);
//
//
//    }
//
//
//    private void extractDataFromIntent() {
//        Intent intent = getIntent();
//
//        // Get Whiteboard name
////        mainColor = intent.getIntExtra("color");
////        mainSize = intent.getIntExtra("size");
//
//    }
//
//
//    public static Intent makeIntent(Context context, int color, int size) {
//        // Create the intent with an extra for the Whiteboard name
//        Intent intent = new Intent(context, PaintingActivity.class);
//        intent.putExtra("color", color);
//        intent.putExtra("size", size);
//        return intent;
//    }
//}
