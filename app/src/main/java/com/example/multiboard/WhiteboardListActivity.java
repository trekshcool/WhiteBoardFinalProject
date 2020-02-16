package com.example.multiboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class WhiteboardListActivity extends AppCompatActivity {

    private List<Whiteboard> whiteboardList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whiteboard_list);

        whiteboardList = new ArrayList<>();
    }
}
