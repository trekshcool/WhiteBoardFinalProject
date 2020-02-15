package com.example.multiboard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private GeofencingClient geofencingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        geofencingClient = LocationServices.getGeofencingClient(this);



        //demo geofence, get user location
        geofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(entry.getKey())

                .setCircularRegion(
                        entry.getValue().latitude,
                        entry.getValue().longitude,
                        Constants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

        //create geofence
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        // ...
                    }
                });

        // Required if your app targets Android 10 or higher.
        if (ContextCompat.checkSelfPermission(thisActivity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (permissionRationaleAlreadyShown) {
                ActivityCompat.requestPermissions(thisActivity,
                        new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                        background-location-permission-request-code);
            } else {
                // Show an explanation to the user as to why your app needs the
                // permission. Display the explanation *asynchronously* -- don't block
                // this thread waiting for the user's response!
            }
        } else {
            // Background location runtime permission already granted.
            // You can now call geofencingClient.addGeofences().
        }
    }

    //demo Geofencie,  triggers
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);//trigger while divice is in the area
        builder.addGeofences(geofenceList);
        return builder.build();


        // builder.setInitialTrigger(GeofencingRequest.GEOFENCE_TRANSITION_ENTER);//trigger when divice enter area
        // builder.setInitialTrigger(GeofencingRequest.GEOFENCE_TRANSITION_EXIT); // trigger on exit
        // builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL); // trigger while still in area

    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }
}





