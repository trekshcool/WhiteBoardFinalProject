package com.example.multiboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;

/**
 * This Activity is the default starting place for the app and allows the user to sign in.
 */
public class MainActivity extends AppCompatActivity {
    private GeofencingClient geofencingClient;
    GoogleApiClient googleApiClient = null;
    public static final String TAG = "MainActivity";
    public static final  String GEOFENCE_ID = "MyGeofenceId";


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        geofencingClient = LocationServices.getGeofencingClient(this);

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



//        //demo geofence, get user location
//        geofenceList.add(new Geofence.Builder()
//                // Set the request ID of the geofence. This is a string to identify this
//                // geofence.
//                .setRequestId(entry.getKey())
//
//                .setCircularRegion(
//                        entry.getValue().latitude,
//                        entry.getValue().longitude,
//                        SyncStateContract.Constants.GEOFENCE_RADIUS_IN_METERS
//                )
//                .setExpirationDuration(SyncStateContract.Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
//                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
//                        Geofence.GEOFENCE_TRANSITION_EXIT)
//                .build());

//        //create geofence
//        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
//                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        // Geofences added
//                        // ...
//                    }
//                })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Failed to add geofences
//                        // ...
//                    }
//                });

//        // Required if your app targets Android 10 or higher.
//        if (ContextCompat.checkSelfPermission(thisActivity,
//                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            if (permissionRationaleAlreadyShown) {
//                ActivityCompat.requestPermissions(thisActivity,
//                        new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION },
//                        background-location-permission-request-code);
//            } else {
//                // Show an explanation to the user as to why your app needs the
//                // permission. Display the explanation *asynchronously* -- don't block
//                // this thread waiting for the user's response!
//            }
//        } else {
//            // Background location runtime permission already granted.
//            // You can now call geofencingClient.addGeofences().
//        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnReume called");
        super.onResume();

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
    protected void onStart(){
        Log.d(TAG, "onStart called");
        super.onStart();
        googleApiClient.reconnect();
    }

    @Override
    protected void onStop(){
        Log.d(TAG, "onstop called");
        super.onStop();
        googleApiClient.disconnect();
    }

    private void startLocationMonitoring(){
        Log.d(TAG, "startLocation called");
        try{
            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(10000)
                    .setFastestInterval(5000)
                    //.set
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // eat more battory
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d(TAG, "Location update lat/long " + location.getLatitude() + " " + location.getLongitude());
                }
            });
        } catch (SecurityException e){
            Log.e(TAG, "SecurityExecption - " + e.getMessage());
        }
    }

    private void startGeoFenceMonitoring(){
        Log.e(TAG, "StartMonitoring Called");
        try{
            //googleApiClient.connect();

            Geofence geofence = new Geofence.Builder()
                    .setRequestId(GEOFENCE_ID)
                    .setCircularRegion(33, -84, 100)
                    .setExpirationDuration(NEVER_EXPIRE)
                    .setNotificationResponsiveness(1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            GeofencingRequest geofenceRequest = new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence).build();

            Intent intent = new Intent(this, GeofenceService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if(!googleApiClient.isConnected()){
            Log.d(TAG, "GoogleApiClient is not connected");
        } else {
            LocationServices.GeofencingApi.addGeofences(googleApiClient, geofenceRequest, pendingIntent)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()){
                                Log.d(TAG, "succefuly added geofence");
                            } else {
                                Log.d(TAG, "Failed to add Geofence" + status.getStatus());
                            }
                        }
                    });
        }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  void stopGeoFenceMonitoring(){
        Log.d(TAG, "StopMoniotring Called");
        ArrayList<String> geofenceIds = new ArrayList<String>();
        geofenceIds.add(GEOFENCE_ID);
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceIds);
    }


    //Create a list of
    private static List<Geofence> getGeofenceList(List<Place> places) {
        List<Geofence> geofenceList = new ArrayList<>();

        for (Place place : places){
            geofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(String.valueOf(place.getId()))
                    .setCircularRegion(
                            place.getLatLng().latitude,
                            place.getLatLng().longitude,
                            place.getRating() // Radius??
                    )
                    .setExpirationDuration(NEVER_EXPIRE)
                    .setNotificationResponsiveness(1000)
                    //.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    //.setLoiteringDelay(LOITERING_DWELL_DELAY)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                    //.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .build());
        }

        return geofenceList;
    }


//    //demo Geofencie,  triggers
//    private GeofencingRequest getGeofencingRequest() {
//        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
//        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);//trigger while divice is in the area
//        builder.addGeofences(geofenceList);
//        return builder.build();
//
//
//        // builder.setInitialTrigger(GeofencingRequest.GEOFENCE_TRANSITION_ENTER);//trigger when divice enter area
//        // builder.setInitialTrigger(GeofencingRequest.GEOFENCE_TRANSITION_EXIT); // trigger on exit
//        // builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL); // trigger while still in area
//
//    }
//
//    private PendingIntent getGeofencePendingIntent() {
//        // Reuse the PendingIntent if we already have it.
//        if (geofencePendingIntent != null) {
//            return geofencePendingIntent;
//        }
//        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
//        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
//        // calling addGeofences() and removeGeofences().
//        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
//                FLAG_UPDATE_CURRENT);
//        return geofencePendingIntent;
//    }
}





