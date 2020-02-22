package com.example.multiboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER;
import static com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT;
import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * This Activity is the default starting place for the app and allows the user to sign in.
 */
public class MainActivity extends AppCompatActivity {
    private GeofencingClient geofencingClient;
    GoogleApiClient googleApiClient = null;
    public static final  String GEOFENCE_ID = "MyGeofenceId";
    private GeoLocations geoLocations = new GeoLocations(); // hold locations for Fence to use

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

    private TextView mTextView;

    Location curLoc;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;


    // GUI variables
    LayoutInflater mInflater;
    LinearLayout linearWhiteboards;

    // Map of all present Whiteboards to their card views
    private HashMap<Whiteboard, View> mListCardMap;

    // Map of all geofences to their respective Whiteboards
    private HashMap<Geofence, Whiteboard> mWhiteboardFencesMap;

    // Callbacks for Firebase updates
    ValueEventListener whiteboardListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Iterate over all modified whiteboards
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                // Get Whiteboard object from database
                Whiteboard wb = ds.getValue(Whiteboard.class);

                // Add list card
                View cardView = mInflater.inflate(R.layout.whiteboard_list_card, linearWhiteboards, false);
                linearWhiteboards.addView(cardView);
                mListCardMap.put(wb, cardView);

                // Fill in information on the new list card
                try {
                    wb.setupListCard(getBaseContext(), cardView);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                startGeoFenceMonitoring();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Getting data failed
            Log.w(TAG, databaseError.toException());
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SharedPreferences initialization/restoration
        mSharedPreferences = getSharedPreferences(mSharedPrefFile, MODE_PRIVATE);

        // Initialize user ID
        mUserId = 0;

        mTextView = findViewById(R.id.text_lat_lon);

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

        // Other Firebase
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseDatabaseReference.child("whiteboards").addValueEventListener(whiteboardListener);

        // Whiteboards
        mListCardMap = new HashMap<>();
        mWhiteboardFencesMap = new HashMap<>();
        geofencingClient = LocationServices.getGeofencingClient(this);

        // Create callback function for realtime location results
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location loc : locationResult.getLocations()) {
                    updateCurLoc(loc);
                }
            }
        };

        // Start the service by getting the current location once
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Task<Location> locationTask = fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location loc) {
                        updateCurLoc(loc);
                    }
                });


        //Begin Realtime update Listenering
        startLocationUpdates();

        // Create callback function for realtime location results
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location loc : locationResult.getLocations()) {
                    updateCurLoc(loc);
                }
            }
        };

        //create Api Client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(TAG, "Connected to GoogleApiClient");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "Suspended connection to Google ApiClient");
                    }


                }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                        Log.d(TAG, "Failed to connect ot googleapiClient- " + result.getErrorMessage());
                    }
                })
                .build();
        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, 1234);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Geofence geofence : mWhiteboardFencesMap.keySet()) {
            //stopGeoFenceMonitoring(geofence.getRequestId());
        }
    }

    public void cardClick(View v) {
        // TODO: Get Whiteboard and start whiteboard activity
    }

    /**
     * Updates the distance reading for Whiteboard list cards.
     */
    private void updateDistances() {
        // TODO: Update Whiteboard distances
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        Log.d(TAG, "OnReume called");
        super.onResume();

        startLocationUpdates();

        int response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (response != ConnectionResult.SUCCESS){
            Log.d(TAG, "Google play Services Not Available - Show Dialog to ask User to Download it");
            GoogleApiAvailability.getInstance().getErrorDialog(this, response, 1).show();
        }
        else {
            Log.d(TAG, "Google play Services is Available - no action is required");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart(){
        Log.d(TAG, "onStart called");
        super.onStart();
        googleApiClient.reconnect();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStop(){
        Log.d(TAG, "onstop called");
        super.onStop();
        googleApiClient.disconnect();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }


    @SuppressLint("SetTextI18n")
    public void updateCurLoc(Location loc) {
        Log.d(TAG, "Updating");

        //Store the new Location and Display it on screen
        curLoc = loc;
        if (curLoc == null) {
            mTextView.setText("NULL location.");
        } else {
            String message =
                    "Latitude: " + curLoc.getLatitude() + "\n" +
                            "Longitude: " + curLoc.getLongitude();
            mTextView.setText(message);
        }
    }


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




    private void startGeoFenceMonitoring(){ //setup checking if your in a fence location
        Log.e(TAG, "StartMonitoring Called");
        try{
            //googleApiClient.connect();
            List<Geofence> geofences = getGeofenceList();


            for (Geofence geofence : geofences) {

                // Make request for each fence
                GeofencingRequest geofenceRequest = new GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofence(geofence).build();

                // Make pendingIntent
                Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                if(!googleApiClient.isConnected()){
                    Log.d(TAG, "GoogleApiClient is not connected");
                } else {
                    // Add geofences to pending intent to listen for geofencing triggers
                    geofencingClient.addGeofences(geofenceRequest, pendingIntent);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void stopGeoFenceMonitoring(String fenceID){ //remove a fence form checking
        Log.d(TAG, "StopMoniotring Called");
        ArrayList<String> geofenceIds = new ArrayList<String>();
        //geofenceIds.add(GEOFENCE_ID);
        geofenceIds.add(fenceID);
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceIds);
    }


    //Create a list of fences
    private  List<Geofence> getGeofenceList() {
        List<Geofence> geofenceList = new ArrayList<>();

        if (mListCardMap.isEmpty()) {
            return null;
        }

        for (Whiteboard wb : mListCardMap.keySet()){
            Geofence geofence = new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this geofence.
                    .setRequestId(wb.getName())
                    .setCircularRegion(
                            wb.getLatitude(),
                            wb.getLongitude(),
                            wb.getRadius()
                    )
                    .setExpirationDuration(NEVER_EXPIRE)
                    .setNotificationResponsiveness(1000)
                    //.setLoiteringDelay(LOITERING_DWELL_DELAY)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            geofenceList.add(geofence);
            mWhiteboardFencesMap.put(geofence, wb);
        }

        return geofenceList;
    }

    /**
     * Defines the behavior for geofence events received from the geofencing PendingIntent
     */
    private class GeofenceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get GeofenceEvent
            GeofencingEvent ev = GeofencingEvent.fromIntent(intent);

            // Check for errors
            if (ev.hasError()) {
                String errorMessage = GeofenceStatusCodes.getStatusCodeString(ev.getErrorCode());
                Log.e(TAG, errorMessage);
                return;
            }

            // Perform task based on transition type
            switch (ev.getGeofenceTransition()) {
                case GEOFENCE_TRANSITION_ENTER:
                    for (Geofence geofence : ev.getTriggeringGeofences()) {
                        mWhiteboardFencesMap.get(geofence).activate();
                    }
                    Log.d(TAG, "Entered fence.");
                    break;
                case GEOFENCE_TRANSITION_EXIT:
                    for (Geofence geofence : ev.getTriggeringGeofences()) {
                        mWhiteboardFencesMap.get(geofence).deactivate();
                    }
                    Log.d(TAG, "Exited fence.");
                    break;
            }
        }
    }

    /*---------- Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            Log.d(TAG, "LAT: " + loc.getLatitude() + "LON: " + loc.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}





