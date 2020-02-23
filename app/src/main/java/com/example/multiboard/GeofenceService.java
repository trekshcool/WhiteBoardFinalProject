package com.example.multiboard;

import android.app.IntentService;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceService extends IntentService {
    public static final  String TAG = "GEOfence Service";

    public GeofenceService(){
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()){
            //Todo: handle Error
        }
        else {
            int transition = event.getGeofenceTransition();
            List<Geofence> geofences = event.getTriggeringGeofences();
            Geofence geofence = geofences.get(0);
            String requestId = geofence.getRequestId();


            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER){
                Log.d(TAG, "Entering geofence - " + requestId);
            }
            else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                Log.d(TAG, "Exiting geofence - " + requestId);
            }
        }
    }
}
