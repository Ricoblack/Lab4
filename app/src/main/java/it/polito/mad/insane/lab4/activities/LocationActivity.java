package it.polito.mad.insane.lab4.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import im.delight.android.location.SimpleLocation;


import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback,OnInfoWindowClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.map);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Fix Portrait Mode
        if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }



    @Override
    public void onMapReady(final GoogleMap googleMap) {
        final RestaurateurJsonManager manager = RestaurateurJsonManager.getInstance(this);

        //if no connection, return
        if(manager.listaFiltrata==null){
            Toast.makeText(this,getResources().getText(R.string.waitforloading),Toast.LENGTH_SHORT).show();
            return;
        }

        for(Restaurant rest : manager.listaFiltrata){
            //if still hasn't finish loading, retry
            if(rest.location==null) {
                Toast.makeText(this,getResources().getText(R.string.waitforloading),Toast.LENGTH_SHORT).show();
                break;
            }

            googleMap.addMarker(new MarkerOptions().position(new LatLng(rest.location.getLatitude(), rest.location.getLongitude())).title(rest.getInfo().getRestaurantName()));
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(manager.myContext,getResources().getText(R.string.localization_permission),Toast.LENGTH_SHORT).show();
        }

        if(manager.simpleLocation.getLatitude()!=0 && manager.simpleLocation.getLongitude()!=0){
            LatLng latLng = new LatLng(manager.simpleLocation.getLatitude(), manager.simpleLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            googleMap.animateCamera(cameraUpdate);
        }
        else {
            manager.simpleLocation.setListener(new SimpleLocation.Listener() {
                @Override
                public void onPositionChanged() {
                    LatLng latLng = new LatLng(manager.simpleLocation.getLatitude(), manager.simpleLocation.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                    googleMap.animateCamera(cameraUpdate);
                }
            });
        }

        googleMap.setOnInfoWindowClickListener(this);


    }

    @Override
    public void onInfoWindowClick(Marker marker) {
            RestaurateurJsonManager manager=RestaurateurJsonManager.getInstance(this);
            Restaurant r=manager.getRestaurantByName(marker.getTitle());
        if(r!=null){
            Intent i = new Intent(this, RestaurantProfileActivity.class);
            i.putExtra("ID",r.getID());
            i.putExtra("Name", marker.getTitle());
            startActivity(i);
        }
    }
}
