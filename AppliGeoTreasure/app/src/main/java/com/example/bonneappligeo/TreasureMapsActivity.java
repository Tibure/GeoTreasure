package com.example.bonneappligeo;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class TreasureMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private static final double RANGE_TREASURE_SPAWN_MIN_DISTANCE = 0.07;
    private static final int NUMBER_OF_TREASURE = 4;
    private static final double TREASURE_COLLECT_DISTANCE = 0.0015;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker mCurrentLocation;
    LocationRequest locationRequest;
    private LocationCallback locationCallback;
    LatLng myLocation;
    List<Marker> treasureLocations;
    boolean gameStarting = true;
    FirebaseFirestore db;
    private int treasuresFound = 0;
    private UserScore userScore = new UserScore();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_treasure_maps);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db = FirebaseFirestore.getInstance();

        Date startDate = new Date();
        userScore.setStartDate(startDate);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        setListener();
        setCallback();
    }

    private void setCallback() {
        locationCallback = new LocationCallback(){

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

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void setListener() {

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
                    if (location != null){
                        updateGPSValues(location);
                    }
                    else
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

        if(gameStarting){
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
            double randomValueLat = rangeMinLat + ( rangeMaxLat - rangeMinLat) * random.nextDouble();
            double randomValueLon = rangeMinLon + ( rangeMaxLon - rangeMinLon) * random.nextDouble();
            LatLng newTreasureMarker = new LatLng(randomValueLat, randomValueLon);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(newTreasureMarker)
                    .title("Lat: " + String.valueOf(newTreasureMarker.latitude) + "| Long: " + String.valueOf(newTreasureMarker.longitude))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.chest))
            );
            if (treasureLocations != null) {
                treasureLocations.add(marker);
            } else {
                treasureLocations = new ArrayList<Marker>();
            }
        }


        showNotificationTreasureIsNear();
    }

    private void showNotificationTreasureIsNear() {
        // verifier le format pour l'icon, elle ne s'affiche pas bien
        Notifier notifier = new Notifier(this);
        notifier.notify("Attention moussaillon !", "Un trésor est tout près", R.drawable.chest);
    }

    private void removeTreasureIfCollected(Location playerLocation) {

        for (Marker treasureLocation : treasureLocations)
        {
            // Utiliser Pythagore pour calculer la distance entre le joueur et chaque coffre.
            double latitudeDistance = treasureLocation.getPosition().latitude - playerLocation.getLatitude();
            double longitudeDistance = treasureLocation.getPosition().longitude - playerLocation.getLongitude();
            double distance =  Math.sqrt((latitudeDistance * latitudeDistance) + (longitudeDistance * longitudeDistance));
            if (distance <= TREASURE_COLLECT_DISTANCE) {
                Toast.makeText(getApplicationContext(), String.valueOf(distance) + " | " + String.valueOf(TREASURE_COLLECT_DISTANCE), Toast.LENGTH_LONG).show();
                treasureLocation.remove();
                treasuresFound++;
            }
        }
        //TODO: Check if game is done et demander nom d'utilisateur

        setTitle("Trésors collectionés : " +String.valueOf(treasuresFound));
    }
    private void gameEnded(){
        userScore.setScore(treasuresFound);
        userScore.setEndDate(new Date());
        userScore.setUsername("Emilio");

        //TODO: mettre dans une interface
        //*     Aller le chercher pour l'inscrire dans une nouvelle activité.
        db.collection("UserScore")
                .add(userScore)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Score sauvegardé", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Erreur, score non sauvegardé", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}



