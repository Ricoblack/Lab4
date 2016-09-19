package it.polito.mad.insane.lab4.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.ReservationsRecyclerAdapter;
import it.polito.mad.insane.lab4.data.Booking;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.managers.NotificationDailyOfferService;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class MyReservationsUserActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    static final String PREF_LOGIN = "loginPref";
    private static String uid ;
    private SharedPreferences mPrefs = null;
    private static HashMap<String,Booking> bookingLocalCache = new HashMap<>(); // questa è la copia locale dei dati scaricati mano a mano dal DB e dalla quale si genera offersList
    private static ArrayList<Booking> bookingList; // Questa è la lista che viene passata all'adapter sulla quale bisogna agire per modificare l'adapter

    private DatabaseReference myRef = null;
    private ValueEventListener listener = null;
    private NavigationView navigationView;

    public static Activity MyReservationsUserActivity = null; // attribute used to finish() the current activity from another activity

    @Override
    public void finish()
    {
        super.finish();
        MyReservationsUserActivity = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyReservationsUserActivity = this;

        setTitle(R.string.title_activity_my_reservation);
        // finish the RestaurantProfile activity if is not finished
        if(RestaurantProfileActivity.RestaurantProfileActivity != null)
            RestaurantProfileActivity.RestaurantProfileActivity.finish();

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

            listener=new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String,Booking> bookings = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Booking>>(){});
//                    {
//                        @Override
//                        protected Object clone() throws CloneNotSupportedException {
//                            return super.clone();
//                        }
//                    });
                    if(bookings != null) {
                        findViewById(R.id.loadingPanel1).setVisibility(View.GONE);
                        setUpView(new ArrayList<Booking>(sortList(bookings.values())), rv);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            myRef = database.getReference("/bookings/users/"+uid);

            myRef.addValueEventListener(listener);
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

        navigationView = (NavigationView) findViewById(R.id.nav_view);

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
                    // finish the HomePageActivity if is not finished
                    if(HomePageActivity.HomePageActivity != null)
                        HomePageActivity.HomePageActivity.finish();

                    Intent i = new Intent(this, HomePageActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.reservation_drawer_item:
                if(!getClass().equals(MyReservationsUserActivity.class))
                {
                    // finish the HomePageActivity if is not finished
                    if(MyReservationsUserActivity != null)
                        MyReservationsUserActivity.finish();

                    Intent i = new Intent(this, MyReservationsUserActivity.class);
                    startActivity(i);
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

                //stop service and clear notifications
                stopService(new Intent(this, NotificationDailyOfferService.class));
                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(001);
                notificationManager.cancel(002);
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

    @Override
    protected void onResume() {
//        myRef.addValueEventListener(listener);
        super.onResume();
        navigationView.getMenu().findItem(R.id.reservation_drawer_item).setChecked(true);
    }

    private void setUpView(List<Booking> bookingList, RecyclerView rv){

        if(!bookingList.isEmpty())
        {
            TextView reservationMessage = (TextView) findViewById(R.id.no_reservation_message);
            reservationMessage.setVisibility(View.GONE);
            //myRef.removeEventListener(listener);
            ReservationsRecyclerAdapter adapter = new ReservationsRecyclerAdapter(this, bookingList, listener, myRef);
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

    @Override
    protected void onPause() {
        myRef.removeEventListener(listener);
        super.onPause();
    }

    /**
     * Method that sort the input list by the "evaso" boolean ad by date. Return the sorted list
     * @param collection
     * @return sorted list
     */
    private List<Booking> sortList(Collection<Booking> collection )
    {
        List<Booking> list = new ArrayList<>(collection);

        // sort by date
        Collections.sort(list, new Comparator<Booking>()
        {
                @Override
                public int compare(Booking lhs, Booking rhs)
                {
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                    try {
                        Date lhsDate = df.parse(lhs.getDateTime());
                        Date rhsDate = df.parse(rhs.getDateTime());
                        return rhsDate.compareTo(lhsDate);
                    }catch(ParseException pe)
                    {
                       pe.printStackTrace();
                        Toast.makeText(MyReservationsUserActivity.this,R.string.error_date,Toast.LENGTH_SHORT ).show();
                    }
                    return rhs.getDateTime().compareTo(lhs.getDateTime());
                }
        });


        // sort by "evaso" boolean
        Collections.sort(list, new Comparator<Booking>()
        {
            @Override
            public int compare(Booking lhs, Booking rhs) {
                return Boolean.compare(lhs.getEvaso(),rhs.getEvaso());
            }
        });
        return list;
    }
}
