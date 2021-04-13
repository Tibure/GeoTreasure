package com.example.bonneappligeo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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
    private Marker mTargetTreasures;
    LocationRequest locationRequest;
    LatLng myLocation;
    List<Marker> treasureLocations;


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

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        updateGPS();
        setListener();
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
                    //finish(); // PEUT ÊTRE CHANGER ÇA
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }


        mMap = googleMap;
        //generateTreasure();
    }

    private void generateTreasure() {
        Intent ValuesIntent = getIntent();
        int max_distance_metres = ValuesIntent.getIntExtra("max_distance_metres", 2000);
        int TreasureCount = ValuesIntent.getIntExtra("TreasureCount", 10);
        LatLng maxBound = getMaxDistLatLng(max_distance_metres);

        LatLng sydney = new LatLng(-33.852, 151.211);
        LatLng saintGeorges = new LatLng(45.3467755, -72.2796581);
        mMap.addMarker(new MarkerOptions()
                .position(saintGeorges)
                .title("Test saint-georges")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.chest))
        );
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.chest))
        );

    }


    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
        // String strLocation;
        // strLocation = "Lat: " + String.valueOf(location.getLatitude()) + "| Long: " + String.valueOf(location.getLongitude());
        // Toast.makeText(getApplicationContext(), strLocation, Toast.LENGTH_SHORT).show();
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (mCurrentLocation != null) {
            mCurrentLocation.remove();
        }
        mCurrentLocation = mMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pirate))
        );
        generateRandomTreasures(NUMBER_OF_TREASURE, location);

        removeTreasureIfCollected(location);

/*        double rangeMinLat = location.getLatitude() - RANGE_MIN_DISTANCE;
        double rangeMaxLat = location.getLatitude() + RANGE_MIN_DISTANCE;
        double rangeMinLon = location.getLongitude() - RANGE_MIN_DISTANCE;
        double rangeMaxLon = location.getLongitude() + RANGE_MIN_DISTANCE;
        Random random = new Random();
        //Toast.makeText(getApplicationContext(), strLocation, Toast.LENGTH_SHORT).show();
        for (int i = 0; i < 1; i++) {
            double randomValueLat = rangeMinLat + ( rangeMaxLat - rangeMinLat) * random.nextDouble();
            double randomValueLon = rangeMinLon + ( rangeMaxLon - rangeMinLon) * random.nextDouble();
           // generateNumberOfTreasures(randomValueLat, randomValueLon);
            LatLng newMarker = new LatLng(randomValueLat, randomValueLon);
            mMap.addMarker(new MarkerOptions()
                    .position(newMarker)
                    .title("Test 1" )
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.chest))
            );
            LatLng newMarker2 = new LatLng(randomValueLat + 0.001, randomValueLon + 0.001);

            mMap.addMarker(new MarkerOptions()
                    .position(newMarker)
                    .title("Test 2" )
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.chest))
            );


        }*/

        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera((CameraUpdateFactory.zoomTo(15)));

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

        // spawn proche du joueur
/*        rangeMinLat = playerLocation.getLatitude() - 0.001;
        rangeMaxLat = playerLocation.getLatitude() + 0.001;
        rangeMinLon = playerLocation.getLongitude() - 0.001;
        rangeMaxLon = playerLocation.getLongitude() + 0.001;
        random = new Random();
        for (int counter = 0; counter < 1; counter++) {
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
        }*/
    }

    private void removeTreasureIfCollected(Location playerLocation) {
        for (Marker treasureLocation : treasureLocations)
        {
            // Use Pythagore to calculate the distance between the player and each chest
            double latitudeDistance = treasureLocation.getPosition().latitude - playerLocation.getLatitude();
            double longitudeDistance = treasureLocation.getPosition().longitude - playerLocation.getLongitude();
            double distance =  Math.sqrt((latitudeDistance * latitudeDistance) + (longitudeDistance * longitudeDistance));
            if (distance <= TREASURE_COLLECT_DISTANCE) {
                Toast.makeText(getApplicationContext(), String.valueOf(distance) + " | " + String.valueOf(TREASURE_COLLECT_DISTANCE), Toast.LENGTH_LONG).show();
                treasureLocation.remove();
            }
        }
    }

    /*    private void generateNumberOfTreasures(double valueLat, double valueLon) {
        LatLng newMarker = new LatLng(valueLat, valueLon);

        mMap.addMarker(new MarkerOptions()
                .position(newMarker)
                .title("Test " + valueLat + " " + valueLon)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.chest))
        );
    }*/
    
   private LatLng getMaxDistLatLng(int metres){
        LatLng BoundMaxDist;
        int earth_rad = 6371000;
        if (myLocation != null){
           BoundMaxDist = new LatLng(myLocation.latitude  + (metres / earth_rad) * (180 / Math.PI), myLocation.longitude + (metres / earth_rad) * (180 / Math.PI) / Math.cos(myLocation.latitude * Math.PI/180));
        }else{
            BoundMaxDist = new LatLng(90,90);
        }

       return BoundMaxDist;
    }
}



