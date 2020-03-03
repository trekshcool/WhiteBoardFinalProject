package com.example.multiboard;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Singleton class that controls the automatic refilling of ink for user's Whiteboards.
 */
public class InkRefiller {

    // Singleton instance
    private static InkRefiller instance;

    // Ink refill rates
    private final float INK_REFILL_RATE = 1f;//0.01f;  // per millisecond
    private final long TIMER_UPDATE_RATE = 800;

    // Update information
    private String userId;
    private Long lastTime;
    private long millisElapsed;

    // Firebase
    private DatabaseReference dbReference;

    // Timer
    private Timer refillTimer;
    private TimerTask refillTask = new TimerTask() {
        @Override
        public void run() {
            refill();
        }
    };

    private ValueEventListener userLoginTimeListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Long curTime = new Date().getTime();
            if (!dataSnapshot.hasChild(userId)) {
                dbReference
                        .child("user-login-times")
                        .child(userId)
                        .setValue(curTime);
                lastTime = curTime;
            }
            else {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (userId.equals(ds.getKey())) {
                        lastTime = ds.getValue(Long.class);
                    }
                }
            }

            // If somehow lastTime has still not been set
            if (lastTime == null) {
                lastTime = curTime;
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {}
    };

    private ValueEventListener inkUpdateListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Iterate over all Whiteboards
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                // Get previous ink level
                Float oldInk = ds.getValue(Float.class);
                if (oldInk == null) {
                    // Getting ink level failed
                    continue;
                }

                // Refill
                Float addInk = millisElapsed * INK_REFILL_RATE;
                Float newInk = Math.min(oldInk + addInk, Whiteboard.MAX_INK);

                // Send to database
                ds.getRef().setValue(newInk);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {}
    };

    /**
     * Private constructor for singleton class.
     */
    private InkRefiller() {
        dbReference = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Get the singleton instance of the InkRefiller.
     * @return the singleton InkRefiller.
     */
    static InkRefiller getInstance() {
        if (instance == null) {
            instance = new InkRefiller();
        }
        return instance;
    }

    /**
     * Set the InkRefiller's userId to update when refilling.
     * @param userId the ID of the user whose information to update.
     */
    private void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Refill all of the Whiteboard ink levels based on the elapsed time.
     */
    private void refill() {
        // Check if lastTime has been retrieved from database yet
        if (lastTime == null) {
            return;
        }

        // Measure elapsed milliseconds
        Long curTime = new Date().getTime();
        millisElapsed = curTime - lastTime;
        lastTime = curTime;

        // Modify all ink levels from database
        dbReference
                .child("users")
                .child(userId)
                .addListenerForSingleValueEvent(inkUpdateListener);

        // Send last update time to user-login-times node in database
        dbReference
                .child("user-login-times")
                .child(userId)
                .setValue(curTime);
    }

    /**
     * Start the recurrent refilling of all the Whiteboard's ink levels for the given user.
     * @param userId the ID of the user to refill ink levels for.
     */
    public void startRefilling(String userId) {
        setUserId(userId);
        if (refillTimer == null) {
            dbReference
                    .child("user-login-times")
                    .addListenerForSingleValueEvent(userLoginTimeListener);
            refillTimer = new Timer();
            refillTimer.schedule(refillTask, 0, TIMER_UPDATE_RATE);
        }
    }
}
