package it.polito.mad.insane.lab4.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.ReservationsRecyclerAdapter;
import it.polito.mad.insane.lab4.data.Booking;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class MyReservationsUserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    static final String PREF_LOGIN = "loginPref";
    private static String uid ;
    private SharedPreferences mPrefs = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(RestaurantProfileActivity.RestaurantProfileActivity != null)
            RestaurantProfileActivity.RestaurantProfileActivity.finish();
        /*
        // finish the RestaurantProfile activity if is not finished
        if(RestaurantProfile.RestaurantProfileActivity != null)
            RestaurantProfile.RestaurantProfileActivity.finish();
        */
        setContentView(R.layout.my_reservations_user_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        if (mPrefs != null) {
            uid = this.mPrefs.getString("uid", null);
        }

        final RecyclerView rv = (RecyclerView) findViewById(R.id.reservation_recycler_view);
        if(rv != null)
        {
            RestaurateurJsonManager manager = RestaurateurJsonManager.getInstance(this);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("/bookings/users/"+uid);

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String,Booking> bookings = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Booking>>() {
                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }
                    });
                    if(bookings != null) {
                        //FIXME non ho capito perchè la progress bar utilizzata in più posti in questo caso non appare(Federico)
                        findViewById(R.id.loadingPanel1).setVisibility(View.GONE);
                        setUpView(new ArrayList<Booking>(bookings.values()), rv);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

        this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_drawer);
        TextView title_drawer = (TextView) headerView.findViewById(R.id.title_drawer);
        if(mPrefs != null)
        {
            title_drawer.setText(mPrefs.getString("uName", null));
        }

        navigationView.setNavigationItemSelectedListener(this);
        /**************************************************/
    }

    /********************DRAWER*****************************/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.home_drawer_item:
                if(!getClass().equals(HomePageActivity.class))
                {
                    Intent i = new Intent(this, HomePageActivity.class);
                    startActivity(i);
                    finish();
                }
                break;
            case R.id.reservation_drawer_item:
                if(!getClass().equals(MyReservationsUserActivity.class)) {
                    Intent i = new Intent(this, MyReservationsUserActivity.class);
                    startActivity(i);
                    finish();
                }
                break;
            case R.id.logout_drawer:
                this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
                if (mPrefs!=null) {
                    uid = null;
                    SharedPreferences.Editor editor = this.mPrefs.edit();
                    editor.clear();
                    editor.apply();
                }
                Intent i = new Intent(this, HomePageActivity.class);
                startActivity(i);
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpView(List<Booking> bookingList, RecyclerView rv){

        if(!bookingList.isEmpty())
        {
            TextView reservationMessage = (TextView) findViewById(R.id.no_reservation_message);
            reservationMessage.setVisibility(View.GONE);
            ReservationsRecyclerAdapter adapter = new ReservationsRecyclerAdapter(this, bookingList);
            rv.setAdapter(adapter);

            if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            {
                // 10 inches
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    // 2 columns
                    GridLayoutManager mGridLayoutManager = new GridLayoutManager(this,2);
                    rv.setLayoutManager(mGridLayoutManager);
                }else
                {
                    // 3 column
                    GridLayoutManager mGridLayoutManager = new GridLayoutManager(this,3);
                    rv.setLayoutManager(mGridLayoutManager);
                }

            } else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
            {
                // 7 inches
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                {
                    // 2 column
                    GridLayoutManager mGridLayoutManager = new GridLayoutManager(this,2);
                    rv.setLayoutManager(mGridLayoutManager);

                }else
                {
                    // 1 column
                    LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
                    mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                    rv.setLayoutManager(mLinearLayoutManagerVertical);

                }
            }else {
                //small and normal
                // 1 column
                LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
                mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                rv.setLayoutManager(mLinearLayoutManagerVertical);
            }

            rv.setItemAnimator(new DefaultItemAnimator());
        }

    }
}
