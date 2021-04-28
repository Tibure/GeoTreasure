package com.example.bonneappligeo;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class TestService extends Service {
    final String TAG = "TestService";
    PendingIntent geofencePendingIntent;
    List<Geofence> geofenceList;
    private GeofencingClient geofencingClient;

    public TestService() {
        geofenceList = new ArrayList<Geofence>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TestService", "onCreate");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(this, "42")
                    .setContentTitle("allo")
                    .setContentText("allo")
                    .setSmallIcon(R.drawable.chest)
                    .build();

            // Notification ID cannot be 0.
            startForeground(1, notification);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("TestService", "OnBind");
        return null;
    }

    public GeofencingRequest getGeofencingRequest() {
        // Mettre tout le code par rapport au Geofence dans une class à part
        Log.d(TAG, "getGeofencingRequest");
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    public PendingIntent getGeofencePendingIntent(Context context) {
        Log.d(TAG, "getGeofencePendingIntent");
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    public void addGeofencingTreasure(double lat, double lon, int id) {
        geofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(Integer.toString(id))
                .setNotificationResponsiveness(0)
                .setCircularRegion(
                        lat,
                        lon,
                        1000
                )
                .setExpirationDuration(600000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
        Log.d(TAG, "testAddGeofencingTreasure");
    }

    public void addGeofencing(GeofencingClient newGeofencingClient, Context context) {
        geofencingClient = newGeofencingClient;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d(TAG, "getGeofencePendingIntent");
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent(context))
                .addOnSuccessListener((Activity) context, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        // ...
                        Log.d(TAG, "succes");
                    }
                })
                .addOnFailureListener((Activity) context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        // ...
                        e.printStackTrace();
                        Log.d(TAG, e.getMessage());
                    }
                });
    }

    public void removeGeofences(Context context) {
        geofencingClient.removeGeofences(getGeofencePendingIntent(context))
                .addOnSuccessListener((Activity) context, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences removed
                        // ...
                    }
                })
                .addOnFailureListener((Activity) context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to remove geofences
                        // ...
                    }
                });
    }
}
