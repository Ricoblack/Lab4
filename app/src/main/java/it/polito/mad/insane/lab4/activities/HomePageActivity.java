package it.polito.mad.insane.lab4.activities;

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
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

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

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.HomeSpinnerAdapter;
import it.polito.mad.insane.lab4.adapters.RestaurantsRecyclerAdapter;
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.data.Review;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class HomePageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static RestaurateurJsonManager manager = null;
    static final String PREF_NAME = "myPref";private SharedPreferences mPrefs = null;
    private List<Restaurant> listaFiltrata;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HomePageActivity.manager = RestaurateurJsonManager.getInstance(this);
        setContentView(R.layout.home_page_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        manager.resetDbApp();

        final SearchView sv = (SearchView) findViewById(R.id.searchView);


        if(sv != null) {
            sv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sv.setIconified(false);
                }
            });
        }

        AppCompatButton applyButton=(AppCompatButton) findViewById(R.id.applyOrdering);
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

        //set up ordering spinner
        setUpSpinner();

        //'clear filter
        this.mPrefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        if (mPrefs!=null) {
            SharedPreferences.Editor editor = this.mPrefs.edit();
            editor.clear();
            editor.apply();
        }

        // set up clean Recycler
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/restaurants");


        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String,Restaurant> r=dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Restaurant>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });
                setUpRestaurantsRecycler(new ArrayList<Restaurant>(r.values()));
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
        getMenuInflater().inflate(R.menu.home_page_menu, menu);
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        this.mPrefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.activity_filtro) {
            Intent i = new Intent(this, it.polito.mad.insane.lab4.activities.FilterActivity.class);
            startActivity(i);
        }
        if(id == R.id.activity_reservations){
            Intent i = new Intent(this, it.polito.mad.insane.lab4.activities.MyReservationsActivity.class);
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
        //TODO nel caso in cui l'app venga utilizzati su cell con schermo piccolo utilizzare il linearLayout, nel caso di schermi grandi(10 pollici non 7) utilizzare griglia a 3
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Toast.makeText(HomePageActivity.this, "Hai cliccato su stocazzo", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /*************************************************/
}
