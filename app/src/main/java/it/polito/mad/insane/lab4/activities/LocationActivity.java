package it.polito.mad.insane.lab4.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        RestaurateurJsonManager manager = RestaurateurJsonManager.getInstance(this);

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


    }
}
