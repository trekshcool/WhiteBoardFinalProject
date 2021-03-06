package com.example.multiboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * This Activity shows all the Whiteboards in the database and displays them for the user to find.
 */
public class MainActivity extends AppCompatActivity {

    // Request codes
    public static final int REQUEST_CODE_PAINT = 4303;

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
    private String mUserId;

    // Location variables
    Location curLoc;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // GUI variables
    LayoutInflater mInflater;
    LinearLayout linearWhiteboards;

    // Arraylist of all Whiteboards
    private ArrayList<Whiteboard> whiteboardList;

    // Callbacks for Firebase Whiteboard data
    ValueEventListener whiteboardListener = new ValueEventListener() {
        /**
         * This method is called when the Activity is started and whenever something in the
         * 'whiteboards' node of the database is modified. This allows the activity to display all
         * the Whiteboards on the database.
         * @param dataSnapshot new data.
         */
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Clear Whiteboard list
            whiteboardList = new ArrayList<>();

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
                    wb.setupListCard(cardView);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            // Listen for user ink levels
            mFirebaseDatabaseReference
                    .child("users")
                    .addValueEventListener(userListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Getting data failed
            Log.w(TAG, databaseError.toException());
        }
    };

    // Callback for Firebase user data
    ValueEventListener userListener = new ValueEventListener() {
        /**
         * This method is called when the Activity is started to get ink level information
         * for the current user.
         * @param dataSnapshot new data.
         */
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // If user has no record for any Whiteboard
            for (Whiteboard wb : whiteboardList) {
                if (!dataSnapshot.hasChild(mUserId) ||
                        !dataSnapshot.child(mUserId).hasChild(wb.getName())) {
                    // Populate with max ink
                    mFirebaseDatabaseReference
                            .child("users")
                            .child(mUserId)
                            .child(wb.getName())
                            .setValue(Whiteboard.MAX_INK);
                }
            }

            // Get user data snapshot
            DataSnapshot dataUser = dataSnapshot.child(mUserId);

            // Set whiteboard ink data from database
            for (DataSnapshot dataWB : dataUser.getChildren()) {
                // Get Whiteboard object
                Whiteboard whiteboard = getWhiteboardByName(dataWB.getKey());

                // Get ink level for this whiteboard
                Float ink = dataWB.getValue(Float.class);
                if (ink != null) {
                    whiteboard.setInkLevel(ink);
                }

                // Display new ink level
                whiteboard.updateInkLevel();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Getting data failed
            Log.w(TAG, databaseError.toException());
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreferences initialization/restoration
        mSharedPreferences = getSharedPreferences(mSharedPrefFile, MODE_PRIVATE);

        // Initialize user ID
        mUserId = "";

        // Find views
        linearWhiteboards = findViewById(R.id.linear_whiteboards);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Whiteboards
        whiteboardList = new ArrayList<>();

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
                mUserId = Integer.toString(dispName.hashCode());
                Log.d(TAG, dispName);
                Log.d(TAG, mUserId);
            }
        }

        // Firebase
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Listen for Whiteboard changes
        mFirebaseDatabaseReference
                .child("whiteboards")
                .addValueEventListener(whiteboardListener);

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

        // Start the InkRefiller
        InkRefiller.getInstance().startRefilling(mUserId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.item_sign_out:
                mFirebaseAuth.signOut();

                // Need to sign back in, launch the Sign In activity
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Opens the PaintingActivity if possible.
     * @param v CardView that was clicked on.
     */
    public void cardClick(View v) {
        // Iterate over whiteboards in list until we find the one that was clicked
        for (Whiteboard wb : whiteboardList) {
            // Only open if active, ignore otherwise
            if (wb.getCardView() == v && wb.isActive()) {
                openPaintingActivity(wb);
            }
        }
    }

    @Override
    protected void onResume() {
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
        super.onStart();
        startLocationUpdates();
    }

    @Override
    protected void onStop(){
        super.onStop();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Updates the current location of the user. If the location result is null, curLoc unchanged.
     * @param loc the new location to update to.
     */
    public void updateCurLoc(Location loc) {
        //Store the new Location
        if (loc != null) {
            curLoc = loc;
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

        // Loop through Whiteboards in whiteboardList
        for (Whiteboard whiteboard : whiteboardList){
            whiteboard.updateDistance(curLoc.getLatitude(), curLoc.getLongitude());
        }

        // Re-order linearWhiteboards layout
        Collections.sort(whiteboardList, new WhiteboardSorter());
        linearWhiteboards.removeAllViews();
        for (Whiteboard wb : whiteboardList) {
            linearWhiteboards.addView(wb.getCardView());
        }
    }

    /**
     * Create an Intent to paint the given Whiteboard.
     * @param whiteboard the Whiteboard object to paint.
     */
    public void openPaintingActivity(Whiteboard whiteboard){
        Intent intent = PaintingActivity.makeIntent(
                MainActivity.this,
                whiteboard.getName(),
                mUserId);
        startActivityForResult(intent, REQUEST_CODE_PAINT);
    }

    /**
     * Get the Whiteboard with the given name from the whiteboardList.
     * @param name the name to search for.
     * @return the Whiteboard with this name, null if not found.
     */
    public Whiteboard getWhiteboardByName(String name) {
        for (Whiteboard wb : whiteboardList) {
            if (wb.getName().equals(name)) {
                return wb;
            }
        }
        return null;
    }


    class WhiteboardSorter implements Comparator<Whiteboard> {

        /**
         * Used for sorting a Collection of Whiteboards.
         * @param wb1 first Whiteboard object.
         * @param wb2 second Whiteboard object.
         */
        public int compare(Whiteboard wb1, Whiteboard wb2) {
            return wb1.compareTo(wb2);
        }

    }

}





