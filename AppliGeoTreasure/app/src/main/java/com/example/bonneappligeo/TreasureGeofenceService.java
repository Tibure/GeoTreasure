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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class TreasureGeofenceService extends Service {
    private PendingIntent geofencePendingIntent;
    private List<Geofence> geofenceList;
    private GeofencingClient geofencingClient;
    private final static int FOREGROUND_NOTIFICATION_ID = 1;

    public TreasureGeofenceService() {
        geofenceList = new ArrayList<Geofence>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(this, "42")
                    .setContentTitle("GeoTreasure")
                    .setContentText("Vous avez commencé à jouer")
                    .setSmallIcon(R.drawable.chest)
                    .build();
            startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    public PendingIntent getGeofencePendingIntent(Context context) {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
    }

    public void addGeofencing(GeofencingClient newGeofencingClient, Context context) {
        geofencingClient = newGeofencingClient;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent(context))
                .addOnSuccessListener((Activity) context, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {}
                })
                .addOnFailureListener((Activity) context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void removeAGeofence(Context context, int indexInTheList) {
        if(geofenceList.size() > 0) {
            Geofence fenceToRemove = geofenceList.get(indexInTheList);
            String geoFenceIdToRemove = fenceToRemove.getRequestId();
            geofenceList.remove(indexInTheList);
            List<String> geoFenceToRemove = new ArrayList<>();
            geoFenceToRemove.add(geoFenceIdToRemove);
            geofencingClient.removeGeofences(geoFenceToRemove);
        }
    }

    public void removeGeofences(Context context) {
        geofencingClient.removeGeofences(getGeofencePendingIntent(context))
                .addOnSuccessListener((Activity) context, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {}
                })
                .addOnFailureListener((Activity) context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {}
                });
    }
}
