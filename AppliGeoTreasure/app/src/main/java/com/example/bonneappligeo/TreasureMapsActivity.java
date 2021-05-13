package com.example.bonneappligeo;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.example.bonneappligeo.scoreManager.ScoreFactory;
import com.example.bonneappligeo.scoreManager.ScoreService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class TreasureMapsActivity extends AppCompatActivity implements OnMapReadyCallback, LifecycleObserver {
    final Context context = this;
    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private static final double RANGE_TREASURE_SPAWN_MIN_DISTANCE = 0.05;
    private static final int NUMBER_OF_TREASURE = 4;
    private static final double TREASURE_COLLECT_DISTANCE = 0.0015;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker mCurrentLocation;
    LocationRequest locationRequest;
    private LocationCallback locationCallback;
    LatLng myLocation;
    private List<Marker> treasureLocations;
    boolean gameStarting = true;
    ScoreService scoreService;
    private int treasuresFound = 0;
    private UserScore userScore = new UserScore();
    private TreasureGeofenceService treasureGeofenceService;
    Button btn_surrender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        treasureGeofenceService = new TreasureGeofenceService();
        startService();

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_treasure_maps);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        scoreService = ScoreFactory.getInstance();

        Date startDate = new Date();
        userScore.setStartDate(startDate);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        setListener();
        setCallback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        treasureGeofenceService.removeGeofences(this);
        stopService(new Intent(this, TreasureGeofenceService.class));
    }

    private void startService() {
        Intent intent = new Intent(this, TreasureGeofenceService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
    }

    private void setCallback() {
        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // save the Location
                updateGPSValues(locationResult.getLastLocation());
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            updateGPS();
        }

    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void setListener() {
        btn_surrender = findViewById(R.id.btn_treasureMaps_surrender);
        btn_surrender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_surrender.animate().alpha(1f).setDuration(5000);
                btn_surrender.animate().alpha(1f).translationYBy(-700).setDuration(2000);
                btn_surrender.animate().alpha(1f).translationXBy(-350).setDuration(2000);
                confirmSurrender();
            }
        });
    }

    private void confirmSurrender() {
        btn_surrender = findViewById(R.id.btn_treasureMaps_surrender);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_surrender_validation);
        dialog.setTitle("Abandonner");
        Button btn_confirm, btn_cancel;
        btn_confirm = dialog.findViewById(R.id.btn_surrenderValidation_confirm);
        btn_cancel = dialog.findViewById(R.id.btn_surrenderValidation_cancel);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                btn_surrender.animate().alpha(1f).translationYBy(+700).setDuration(2000);
                btn_surrender.animate().alpha(1f).translationXBy(+350).setDuration(2000);

            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                btn_surrender.animate().alpha(1f).translationYBy(+700).setDuration(2000);
                btn_surrender.animate().alpha(1f).translationXBy(+350).setDuration(2000);
            }
        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "Cette application a besoin des permissions GPS", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        startLocationUpdates();
        mMap = googleMap;
    }

    private void updateGPS() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        updateGPSValues(location);
                    } else
                        Toast.makeText(getApplicationContext(), "Impossible d'obtenir la localisation GPS ", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
    }

    private void updateGPSValues(@NonNull Location location) {
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (mCurrentLocation != null) {
            mCurrentLocation.remove();
        }
        mCurrentLocation = mMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pirate))
        );

        if (gameStarting) {
            generateRandomTreasures(NUMBER_OF_TREASURE, location);
            gameStarting = false;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            mMap.animateCamera((CameraUpdateFactory.zoomTo(15)));
        }
        removeTreasureIfCollected(location);
    }

    private void generateRandomTreasures(int numberOfTreasures, Location playerLocation) {
        double rangeMinLat = playerLocation.getLatitude() - RANGE_TREASURE_SPAWN_MIN_DISTANCE;
        double rangeMaxLat = playerLocation.getLatitude() + RANGE_TREASURE_SPAWN_MIN_DISTANCE;
        double rangeMinLon = playerLocation.getLongitude() - RANGE_TREASURE_SPAWN_MIN_DISTANCE;
        double rangeMaxLon = playerLocation.getLongitude() + RANGE_TREASURE_SPAWN_MIN_DISTANCE;
        Random random = new Random();
        for (int counter = 0; counter < numberOfTreasures; counter++) {
            double randomValueLat = rangeMinLat + (rangeMaxLat - rangeMinLat) * random.nextDouble();
            double randomValueLon = rangeMinLon + (rangeMaxLon - rangeMinLon) * random.nextDouble();
            LatLng newTreasureMarker = new LatLng(randomValueLat, randomValueLon);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(newTreasureMarker)
                    .title(String.valueOf(counter))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.chest))
            );

            if (treasureLocations == null)
                treasureLocations = new ArrayList<Marker>();
            treasureLocations.add(marker);

            treasureGeofenceService.addGeofencingTreasure(randomValueLat, randomValueLon, counter);
        }
        treasureGeofenceService.addGeofencing(LocationServices.getGeofencingClient(this), this);
    }

    private void removeTreasureIfCollected(Location playerLocation) {
        Iterator<Marker> treasureIterator = treasureLocations.iterator();
        while(treasureIterator.hasNext())
        {
            Marker treasureMarker = treasureIterator.next();

            Location teasureLocation = new Location(LocationManager.GPS_PROVIDER);
            teasureLocation.setLatitude(treasureMarker.getPosition().latitude);
            teasureLocation.setLongitude(treasureMarker.getPosition().longitude);
            double distance = getDistanceBetweenTwoPoints(teasureLocation, playerLocation);
            if (distance <= TREASURE_COLLECT_DISTANCE) {
                Toast.makeText(getApplicationContext(), "Bravo !  Vous avez rammasé un coffre !", Toast.LENGTH_LONG).show();

                // get the element index
                int elementIndex = 0;
                for (int counter = 0; counter < treasureLocations.size(); counter++) {
                    if (treasureLocations.get(counter) == treasureMarker) {
                        elementIndex = counter;
                    }
                }

                treasureMarker.remove();
                treasureIterator.remove();
                treasureGeofenceService.removeAGeofence(this, elementIndex);
                treasuresFound++;
                final MediaPlayer mp = MediaPlayer.create(this, R.raw.gold_coins_chest);
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
            }
        }

        if (treasureLocations.size() == 0) {
            stopLocationUpdates();
            gameEnded();
        }
        //TODO: Check if game is done et demander nom d'utilisateur

        setTitle("Trésors collectionés : " + String.valueOf(treasuresFound));
    }

    private double getDistanceBetweenTwoPoints(Location firstLocation, Location secondLocation) {
        // Utiliser Pythagore pour calculer la distance entre le joueur et chaque coffre.
        double latitudeDistance = firstLocation.getLatitude() - secondLocation.getLatitude();
        double longitudeDistance = firstLocation.getLongitude() - secondLocation.getLongitude();
        return Math.sqrt((latitudeDistance * latitudeDistance) + (longitudeDistance * longitudeDistance));
    }

    private void gameEnded() {
        userScore.setTreasuresFound(treasuresFound);
        userScore.setEndDate(new Date());
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_userscore);
        dialog.setTitle("Jeu fini");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        TextView textViewTime, textViewScore;
        textViewTime = dialog.findViewById(R.id.textView_addUserScore_time);
        textViewScore = dialog.findViewById(R.id.textView_addUserScore_treasuresFound);
        Log.e("allo",String.valueOf(userScore.getTimeDiff()));
        textViewTime.setText("Temps: " + String.valueOf(userScore.getTimeDiff()) + "s");
        textViewScore.setText("Trésors trouvés: " + String.valueOf(userScore.getTreasuresFound()));
        Button addUserScoreButton = dialog.findViewById(R.id.btn_addUserScore_add);

        addUserScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText_username = dialog.findViewById(R.id.editText_addUserScore_username);
                userScore.setUsername(editText_username.getText().toString());
                scoreService.createScore(userScore);
                dialog.dismiss();
            }
        });

        dialog.show();

        treasureGeofenceService.removeGeofences(this);
    }


}



