package com.example.multiboard;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * This Activity shows all the Whiteboards in the database and displays them for the user to find.
 */
public class MainActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "MainActivity";

    // Shared Preferences variables
    private SharedPreferences mSharedPreferences;
    private String mSharedPrefFile = "com.example.multiboard";

    // Firebase variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;

    // User information
    private int mUserId;

    // Location variables
    Location curLoc;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // GUI variables
    LayoutInflater mInflater;
    LinearLayout linearWhiteboards;

    // Arraylist of all Whiteboards
    private ArrayList<Whiteboard> whiteboardList;

    // Callbacks for Firebase updates
    ValueEventListener whiteboardListener = new ValueEventListener() {
        /**
         * This method is called when the Activity is started and whenever something in the
         * 'whiteboards' node of the database is modified. This allows the activity to display all
         * the Whiteboards on the database.
         * @param dataSnapshot new data.
         */
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Iterate over all modified whiteboards
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                // Get Whiteboard object from database
                Whiteboard wb = ds.getValue(Whiteboard.class);

                // Add list card
                View cardView = mInflater.inflate(R.layout.whiteboard_list_card, linearWhiteboards, false);
                linearWhiteboards.addView(cardView);
                whiteboardList.add(wb);

                // Fill in information on the new list card
                try {
                    wb.setupListCard(getBaseContext(), cardView);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Getting data failed
            Log.w(TAG, databaseError.toException());
        }
    };

    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreferences initialization/restoration
        mSharedPreferences = getSharedPreferences(mSharedPrefFile, MODE_PRIVATE);

        // Initialize user ID
        mUserId = 0;

        // Find views
        linearWhiteboards = findViewById(R.id.linear_whiteboards);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            // Anonymize user data by hashing the name to create an ID number before storing it
            String dispName = mFirebaseUser.getDisplayName();
            if (dispName != null) {
                mUserId = dispName.hashCode();
                Log.d(TAG, dispName);
                Log.d(TAG, Integer.toString(mUserId));
            }
        }

        // Firebase data
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child("whiteboards").addValueEventListener(whiteboardListener);

        // Whiteboards
        whiteboardList = new ArrayList<>();

        // Create callback function for realtime location results
        locationCallback = new LocationCallback() {
            /**
             * This method is called regularly as the user's GPS location changes.
             * @param locationResult the new location of the user.
             */
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location loc : locationResult.getLocations()) {
                    updateCurLoc(loc);
                    updateWhiteboardAvailability();
                }
            }
        };

        // Request permissions for location tracking
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1234);
        }

        // Start the service by getting the current location once
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location loc) {
                        updateCurLoc(loc);
                        updateWhiteboardAvailability();
                    }
                });

        //Begin realtime update listening
        startLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public void cardClick(View v) {
        // TODO: Get Whiteboard and start whiteboard activity
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnReume called");
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStart(){
        Log.d(TAG, "onStart called");
        super.onStart();
        startLocationUpdates();
    }

    @Override
    protected void onStop(){
        Log.d(TAG, "onstop called");
        super.onStop();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Updates the current location of the user. If the location result is null, curLoc unchanged.
     * @param loc the new location to update to.
     */
    public void updateCurLoc(Location loc) {
        Log.d(TAG, "Updating");

        //Store the new Location
        curLoc = loc;
        if (curLoc != null) {
            Log.d(TAG, "LAT: " + loc.getLatitude() + ", LON: " + loc.getLongitude());
        }
    }

    /**
     * Tells the fusedLocationClient to begin listening for updates.
     */
    public void startLocationUpdates() {
        // Request for location
        LocationRequest locationRequest = new LocationRequest()
                .setInterval(100)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Begin the listener
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );
    }

    /**
     * Updates all Whiteboard availability using the distance function and curLoc.
     */
    private void updateWhiteboardAvailability(){
        Log.d(TAG, "Updating Whiteboards");

        // Loop through Whiteboards in whiteboardList
        for (Whiteboard whiteboard: whiteboardList){
            Log.d(TAG, "Update Whiteboard: " + whiteboard.getName());
            whiteboard.updateDistance(curLoc.getLatitude(), curLoc.getLongitude());
        }
    }

}





