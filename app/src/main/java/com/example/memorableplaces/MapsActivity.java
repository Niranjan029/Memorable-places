package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.memorableplaces.databinding.ActivityMapsBinding ;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap ;
    private ActivityMapsBinding binding ;
    Intent intent ;
    LocationManager locationManager ;
    LocationListener locationListener ;


    public void centerMapOnLocation(Location location, String title) {
        if (location != null) {
            LatLng userlocation = new LatLng(location.getLatitude(), location.getLongitude());
           // mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userlocation).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation,6));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
                    Location lastknownlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    centerMapOnLocation(lastknownlocation,"Your Location");
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this) ;


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
       mMap = googleMap ;
        mMap.setOnMapLongClickListener(this);
        intent = getIntent() ;
        if(intent.getIntExtra("placeNumber",0)==0) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    mMap.clear();
                    centerMapOnLocation(location, "Your Location");
                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
                Location lastknownlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastknownlocation, "Your Location");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        else
        {
            Location location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).latitude) ;
            location.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).longitude) ;
            centerMapOnLocation(location,MainActivity.places.get(intent.getIntExtra("placeNumber",0)));
        }

    }


    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault()) ;

        String addresses = "";
        try {
         List<Address> listaddresses =   geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(listaddresses!=null && listaddresses.size()>0)
            {
                if(listaddresses.get(0).getThoroughfare()!=null)
                {
                    if(listaddresses.get(0).getSubThoroughfare()!=null)
                    {
                        addresses+=listaddresses.get(0).getSubThoroughfare()+" " ;
                    }
                    addresses+=listaddresses.get(0).getThoroughfare()  ;
                }
                if(addresses.equals(""))
                {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault());
                    addresses+= simpleDateFormat.format(new Date());
                }
                Log.d("Address",listaddresses.get(0).toString()) ;
            }
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(addresses).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        MainActivity.places.add(addresses);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();
        Toast.makeText(this, "location updated", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);
        ArrayList<String> latitudes = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();
        for(LatLng cord : MainActivity.locations)
        {
            latitudes.add(Double.toString(cord.latitude));
            longitudes.add(Double.toString(cord.longitude));
        }
        try {
            sharedPreferences.edit().putString("VisitedPlaces",ObjectSerializer.serialize(MainActivity.places)).apply();
            sharedPreferences.edit().putString("Lats",ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("Lons",ObjectSerializer.serialize(longitudes)).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}