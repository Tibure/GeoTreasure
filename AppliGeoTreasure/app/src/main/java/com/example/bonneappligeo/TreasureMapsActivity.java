package com.example.bonneappligeo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


public class TreasureMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker mCurrentLocation;
    private Marker mTargetTreasures;
    LocationRequest locationRequest;
    LatLng myLocation;


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
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateGPS();
                } else {
                    Toast.makeText(this, "Cette application a besoins des permissions GPS", Toast.LENGTH_SHORT).show();
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
        generateTreasure();
    }

    private void generateTreasure() {
        Intent ValuesIntent = getIntent();
        int max_distance_metres = ValuesIntent.getIntExtra("max_distance_metres", 2000);
        int TreasureCount = ValuesIntent.getIntExtra("TreasureCount", 10);
        LatLng maxBound = getMaxDistLatLng(max_distance_metres);

//        LatLng sydney = new LatLng(-33.852, 151.211);
//        LatLng saintGeorges = new LatLng(45.3467755, -72.2796581);
//        mMap.addMarker(new MarkerOptions()
//                .position(saintGeorges)
//                .title("Test saint-georges")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.chest))
//        );
//        mMap.addMarker(new MarkerOptions()
//                .position(sydney)
//                .title("Marker in Sydney")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.chest))
//        );
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
                        Toast.makeText(getApplicationContext(), "Could not get GPS location", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }
        }
    }

    private void updateGPSValues(@NonNull Location location) {
        String strLocation;
        strLocation = "Lat: " + String.valueOf(location.getLatitude()) + "| Long: " + String.valueOf(location.getLongitude());
        Toast.makeText(getApplicationContext(), strLocation, Toast.LENGTH_SHORT).show();
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (mCurrentLocation != null) {
            mCurrentLocation.remove();
        }

        mCurrentLocation = mMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.chest))
        );




        //mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        //mMap.animateCamera((CameraUpdateFactory.zoomTo(15)));

    }

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



