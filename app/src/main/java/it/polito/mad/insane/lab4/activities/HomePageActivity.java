package it.polito.mad.insane.lab4.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import im.delight.android.location.SimpleLocation;
import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.HomeSpinnerAdapter;
import it.polito.mad.insane.lab4.adapters.RestaurantsRecyclerAdapter;
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;


public class HomePageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static RestaurateurJsonManager manager = null;
    static final String PREF_NAME = "myPref";
    private SharedPreferences mPrefs = null;
    private List<Restaurant> listaFiltrata;
    private Context myContext=this;




    //TODO: implementare la ricerca con DB(Michele)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        HomePageActivity.manager = RestaurateurJsonManager.getInstance(this);
        setContentView(R.layout.home_page_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        manager.resetDbApp();

        final SearchView sv = (SearchView) findViewById(R.id.searchView);
        if(sv != null) {
            sv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sv.setIconified(false);
                }
            });
        }

        ImageView iv = (ImageView) findViewById(R.id.localize_me);
        if(iv != null){
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(HomePageActivity.this, LocationActivity.class);
                    startActivity(i);
                }
            });

        }


        AppCompatButton applyButton =(AppCompatButton) findViewById(R.id.applyOrdering);
        if(applyButton != null) {
            applyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Spinner dSpinner = (Spinner) findViewById(R.id.orderSpinner);
                    if (dSpinner.getSelectedItemPosition() == 0) {
                        Toast.makeText(v.getContext(), "Seleziona un ordinamento", Toast.LENGTH_SHORT).show();
                    } else {
                        setUpRestaurantsRecycler(manager.getOrderedRestaurants(dSpinner.getSelectedItem().toString(), listaFiltrata));
                    }
                }
            });
        }

        // set up ordering spinner
        setUpSpinner();

        // clear filter
        this.mPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (mPrefs!=null) {
            SharedPreferences.Editor editor = this.mPrefs.edit();
            editor.clear();
            editor.apply();
        }

        // set up clean Recycler
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference restaurantsRef = database.getReference("/restaurants");

        restaurantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // get data form Firebase
                HashMap<String,Restaurant> r = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Restaurant>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });
                if(r!=null) {
                    // set recycler
                    listaFiltrata=new ArrayList<Restaurant>(r.values());
                    manager.listaFiltrata=listaFiltrata;
                    setUpRestaurantsRecycler(listaFiltrata);
                    Firebase.setAndroidContext(myContext);
                    GeoFire geoFire = new GeoFire(new Firebase("https://lab4-insane.firebaseio.com/locations"));
                    manager.fillRestaurantLocations(geoFire,listaFiltrata);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Fix Portrait Mode
        if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


        /**********************DRAWER****************************/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        /**************************************************/


        //localization

        startLocalization();


    }

    // TODO: cosa fare se il gps non è attivo? come si fanno a ordinare i risultati dei ristoranti per distanza? (Michele)
    private void startLocalization()
    {
        // construct a new instance of SimpleLocation
        manager.simpleLocation = new SimpleLocation(this);

        // if we can't access the location yet
        if (!manager.simpleLocation.hasLocationEnabled()) {
            // ask the user to enable location access
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.askgps)
                    .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SimpleLocation.openSettings(manager.myContext);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel_dialog_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            Dialog dialog = builder.create();
            dialog.show();

            manager.simpleLocation.setListener(new SimpleLocation.Listener() {

                @Override
                public void onPositionChanged() {
                    Toast.makeText(manager.myContext,"Lat: " + manager.simpleLocation.getLatitude() + " long: " + manager.simpleLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                }
            });
            // make the device update its location
            manager.simpleLocation.beginUpdates();
        }
    }


    @Override
    public void onBackPressed() {
        /**********************DRAWER***************************/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        /*************************************************/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

//        if (loggato)
//            getMenuInflater().inflate(R.menu.home_user_menu, menu);
//        else
            getMenuInflater().inflate(R.menu.home_page_menu, menu);
        return true;
    }


    @Override
    protected void onPause() {
        // stop location updates (saves battery)
        //manager.simpleLocation.endUpdates();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        this.mPrefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.activity_filtro) {
            Intent i = new Intent(this, it.polito.mad.insane.lab4.activities.FilterActivity.class);
            startActivity(i);
        }

        if(id == R.id.activity_login){
            Intent i = new Intent(this, it.polito.mad.insane.lab4.activities.LoginActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpRestaurantsRecycler(List<Restaurant> restaurants)
    {
        RecyclerView rV = (RecyclerView) findViewById(R.id.RestaurateurRecyclerView);
        RestaurantsRecyclerAdapter adapter = new RestaurantsRecyclerAdapter(this, restaurants);
        rV.setAdapter(adapter);

        // set Layout Manager
        if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
        {
            // 10 inches
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                // 3 columns
                GridLayoutManager mGridLayoutManager = new GridLayoutManager(this,3);
                rV.setLayoutManager(mGridLayoutManager);
            }else
            {
                // 5 column
                GridLayoutManager mGridLayoutManager = new GridLayoutManager(this,5);
                rV.setLayoutManager(mGridLayoutManager);
            }

        } else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
        {
            // 7 inches
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                // 4 column
                GridLayoutManager mGridLayoutManager = new GridLayoutManager(this,4);
                rV.setLayoutManager(mGridLayoutManager);

            }else
            {
                // 3 column
                GridLayoutManager mGridLayoutManager = new GridLayoutManager(this,3);
                rV.setLayoutManager(mGridLayoutManager);
            }
        }else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL)
        {
            // normal screen
            // 2 columns
            GridLayoutManager mGridLayoutManager = new GridLayoutManager(this,2);
            rV.setLayoutManager(mGridLayoutManager);
        }else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            // small screen
            // 1 columns
            LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
            mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
            rV.setLayoutManager(mLinearLayoutManagerVertical);
        }

        // Set animation
        //RecyclerView.ItemAnimator ia = new SlideInOutLeftItemAnimator(rV);  // try animator //FIX-ME: doesn't work, pazienza
        DefaultItemAnimator ia = new DefaultItemAnimator();
        rV.setItemAnimator(ia);
    }

    private void setUpSpinner()
    {
        final Spinner dSpinner = (Spinner) findViewById(R.id.orderSpinner);
        List<String> orderings = new ArrayList<>();
        Resources res = getResources();
        String[] dStrings = res.getStringArray(R.array.order_array);
        Collections.addAll(orderings, dStrings);
        HomeSpinnerAdapter dAdapter = new HomeSpinnerAdapter(HomePageActivity.this, R.layout.support_simple_spinner_dropdown_item,orderings, res);
        dAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        dSpinner.setAdapter(dAdapter);

        dSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if(position==1|| position==2){
                //  setUpRestaurantsRecycler(manager.getOrderedRestaurants(dSpinner.getSelectedItem().toString()));
                //}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing
            }
        });

        //dSpinner.setSelection(2);

    }

    /********************DRAWER*****************************/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//            Toast.makeText(HomePageActivity.this, "Hai cliccato su stocazzo", Toast.LENGTH_SHORT).show();
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        }
        switch (id)
        {
            case R.id.home_activity:
                if(!getClass().equals(HomePageActivity.class))
                {
                    Intent i = new Intent(this, HomePageActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.activity_reservations:
                if(!getClass().equals(MyReservationsUserActivity.class)) {
                    Intent i = new Intent(this, MyReservationsUserActivity.class);
                    startActivity(i);
                }
                break;
        }
//        if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /*************************************************/
}
