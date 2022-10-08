package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
  ListView listView ;
  static   ArrayList<String> places = new ArrayList<>();
  static   ArrayList<LatLng> locations = new ArrayList<>();
  static   ArrayAdapter<String> arrayAdapter ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);
        ArrayList<String> latitudes = new ArrayList<>();
        ArrayList<String> longitudes = new ArrayList<>();
        places.clear();
        latitudes.clear();
        longitudes.clear();
        try {
            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("VisitedPlaces",ObjectSerializer.serialize(new ArrayList<>())));
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Lats",ObjectSerializer.serialize(new ArrayList<>())));
           longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("Lons",ObjectSerializer.serialize(new ArrayList<>())));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if(places.size()>0 && latitudes.size()>0 && longitudes.size()>0)
        {
            if(places.size()== latitudes.size() && places.size()== longitudes.size())
            {
                for (int i=0;i<places.size();i++)
                {
                    locations.add( new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }
        }
        else
        {
            places.add("Add a new Place.." );
            locations.add(new LatLng(0,0));
        }
        listView = findViewById(R.id.listPlaces);
      arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,places);
     listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("placeNumber",i);
                startActivity(intent);
            }
        });

    }


}