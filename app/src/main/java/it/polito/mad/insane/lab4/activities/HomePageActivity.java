package it.polito.mad.insane.lab4.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseException;
import com.firebase.geofire.GeoFire;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import im.delight.android.location.SimpleLocation;
import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.HomeSpinnerAdapter;
import it.polito.mad.insane.lab4.adapters.RestaurantsRecyclerAdapter;

import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.managers.NotificationDailyOfferService;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;


public class HomePageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static RestaurateurJsonManager manager = null;
    static final String PREF_NAME = "myPrefFilter";
    static final String PREF_LOGIN = "loginPref";
    private SharedPreferences mPrefs = null;
    private List<Restaurant> listaFiltrata;
    private Context myContext=this;
    private static String uid ;
    private String rid;
    private DatabaseReference restaurantsRef;
    private ValueEventListener listener;
    private NavigationView navigationView;

    //TODO MIchele sistemare tutti i titoli delle activity
    //TODO testare l'app in italiano per controllare la traduzione

    public static Activity HomePageActivity = null; // attribute used to finish() the current activity from another activity

    @Override
    public void finish()
    {
        super.finish();
        HomePageActivity = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        HomePageActivity = this;

        //controllo se l'utente è loggato come ristoratore o come consumer
        this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        if (mPrefs != null) {
            uid = this.mPrefs.getString("uid", null);
            if(uid == null){
                rid = this.mPrefs.getString("rid", null);
                if(rid != null){
                    Intent ir = new Intent(HomePageActivity.this, HomeRestaurateurActivity.class);
                    startActivity(ir);
                    finish();
                }
            }
            else {
                //loggato come user, attivo service
                if(isServiceStarted()==false) {
                    //Toast.makeText(this,"service started",Toast.LENGTH_SHORT).show();
                    Intent mServiceIntent = new Intent(this, NotificationDailyOfferService.class);
                    startService(mServiceIntent);

                }
                else {
                    //Toast.makeText(this,"service gia attivo",Toast.LENGTH_SHORT).show();
                }
            }
        }

        manager = RestaurateurJsonManager.getInstance(this);
        setContentView(R.layout.home_page_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        // check login
//        if(uid == null)
//        {
//            this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
//            if (mPrefs != null) {
//                uid = this.mPrefs.getString("uid", null);
//            }
//        }

        // clear filter
        this.mPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (mPrefs!=null) {
            SharedPreferences.Editor editor = this.mPrefs.edit();
            editor.clear();
            editor.apply();
        }

        final SearchView sv = (SearchView) findViewById(R.id.searchView);
        if(sv != null) {
            sv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sv.setIconified(false);
                }
            });
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if(manager.listaFiltrata==null){
                        Toast.makeText(myContext,getResources().getText(R.string.waitforloading),Toast.LENGTH_SHORT).show();
                    }
                    else{
                        //if no just white spaces
                        setUpRestaurantsRecycler(manager.getFilteredRestaurants(query),false);

                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    //if just white spaces
                    if(newText.equals("")) setUpRestaurantsRecycler(manager.listaFiltrata,false);
                    return true;
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
                        Toast.makeText(v.getContext(), myContext.getResources().getText(R.string.selectOrdering), Toast.LENGTH_SHORT).show();
                    }
                    else if(dSpinner.getSelectedItemPosition()==4){
                            setUpRestaurantsRecycler(listaFiltrata,false);
                        }
                     else {
                        setUpRestaurantsRecycler(manager.getOrderedRestaurants(dSpinner.getSelectedItem().toString(), listaFiltrata),true);
                    }
                }
            });
        }

        // set up ordering spinner
        setUpSpinner();



        // set up clean Recycler
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        restaurantsRef = database.getReference("/restaurants");

        listener=new ValueEventListener() {
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
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    listaFiltrata=new ArrayList<Restaurant>(r.values());
                    manager.listaFiltrata=listaFiltrata;
                    setUpRestaurantsRecycler(listaFiltrata,false);
                    Firebase.setAndroidContext(myContext);
                    GeoFire geoFire = new GeoFire(new Firebase("https://lab4-insane.firebaseio.com/locations"));
                    manager.fillRestaurantLocations(geoFire,listaFiltrata);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        restaurantsRef.addListenerForSingleValueEvent(listener);

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
        //controllo se l'utente è loggato come ristoratore o come consumer
        this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);

        if(uid != null){

            //user logged
            View headerView = navigationView.inflateHeaderView(R.layout.nav_header_drawer);
            TextView title_drawer = (TextView) headerView.findViewById(R.id.title_drawer);

            if(mPrefs != null) {
                title_drawer.setText(mPrefs.getString("uName", null));
            }
            Menu menu = navigationView.getMenu();
            MenuItem log = menu.findItem(R.id.logout_drawer);
            log.setTitle(getResources().getString(R.string.logout));

        }else if(rid != null){

            //restaurateur logged
            View headerView = navigationView.inflateHeaderView(R.layout.nav_header_drawer);
            TextView title_drawer = (TextView) headerView.findViewById(R.id.title_drawer);
            if(mPrefs != null) {
                title_drawer.setText(mPrefs.getString("rUser", null));
            }
            Menu menu = navigationView.getMenu();
            MenuItem log = menu.findItem(R.id.logout_drawer);
            log.setTitle(getResources().getString(R.string.logout));

        }else{
            //no log
            View headerView = navigationView.inflateHeaderView(R.layout.nav_header_drawer);
            TextView title_drawer = (TextView) headerView.findViewById(R.id.title_drawer);
            title_drawer.setText(getResources().getString(R.string.noLog));

            Menu menu = navigationView.getMenu();
            MenuItem log = menu.findItem(R.id.logout_drawer);
            log.setTitle(getResources().getString(R.string.login));

            menu.findItem(R.id.reservation_drawer_item).setVisible(false);

        }

        navigationView.setNavigationItemSelectedListener(this);
        /**************************************************/


        //localization
        startLocalization();




        closeOtherActivities();

    }
    private void closeOtherActivities()
    {
        // close other activities
        if(MyReservationsUserActivity.MyReservationsUserActivity != null)
            MyReservationsUserActivity.MyReservationsUserActivity.finish();

    }

    private boolean isServiceStarted() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NotificationDailyOfferService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


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
                            SimpleLocation.openSettings(myContext);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel_dialog_button), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

            Dialog dialog = builder.create();
            dialog.show();


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

        if (uid != null)
            getMenuInflater().inflate(R.menu.home_user_menu, menu);
        else
            getMenuInflater().inflate(R.menu.home_page_menu, menu);
        return true;
    }


    @Override
    protected void onPause() {
        // stop location updates (saves battery)
        //manager.simpleLocation.endUpdates();
        restaurantsRef.removeEventListener(listener);
        super.onPause();
    }




    @Override
    protected void onResume()
    {
        super.onResume();

        navigationView.getMenu().findItem(R.id.home_drawer_item).setChecked(true);

        // check login
        this.mPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        if(this.mPrefs != null)
        {
            //If coming from filter activity...
            if(mPrefs.getString("haveToFilter","no").equals("yes")) {
                listaFiltrata = manager.getAdvancedFilteredRestaurants(this.mPrefs.getString("distanceValue", ""), this.mPrefs
                        .getString("priceValue", ""), this.mPrefs.getString("typeValue", ""), this.mPrefs.getString("timeValue", ""));

                setUpRestaurantsRecycler(listaFiltrata,false);
            }

        }else
        {
            //No filtering needed
            setUpRestaurantsRecycler(manager.listaFiltrata,false);
        }
        // make the device update its location
        manager.simpleLocation.beginUpdates();

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

        if(id == R.id.notifications){
            Intent i = new Intent(this, it.polito.mad.insane.lab4.activities.NotificationsActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpRestaurantsRecycler(List<Restaurant> restaurants, boolean singleColumn)
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

        //If coming from filter, indipendently from display, place in single column
        if(singleColumn){
            // 1 columns
            LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
            mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
            rV.setLayoutManager(mLinearLayoutManagerVertical);
        }

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
                /*
                if(position==1|| position==2){
                  setUpRestaurantsRecycler(manager.getOrderedRestaurants(dSpinner.getSelectedItem().toString(),manager.listaFiltrata));
                }
                */
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing
            }
        });

        // Initialize spinner to the "DEFAULT" value
        dSpinner.setSelection(4);

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
                    if(HomePageActivity != null)
                        HomePageActivity.finish();

                    Intent i = new Intent(this, HomePageActivity.class);
                    startActivity(i);
                }
                break;

            case R.id.reservation_drawer_item:

                 if (!getClass().equals(MyReservationsUserActivity.class))
                 {
                     // finish the HomePageActivity if is not finished
                     if(MyReservationsUserActivity.MyReservationsUserActivity != null)
                         MyReservationsUserActivity.MyReservationsUserActivity.finish();

                        Intent i = new Intent(this, MyReservationsUserActivity.class);
                        startActivity(i);
                 }
                break;

            case R.id.logout_drawer:
                if(uid == null){

                    //user no logged, insert login
                    Intent i = new Intent(this, it.polito.mad.insane.lab4.activities.LoginActivity.class);
                    startActivity(i);

                }else {
                    //LOGOUT

                    this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
                    if (mPrefs != null) {

                        uid = null;
                        SharedPreferences.Editor editor = this.mPrefs.edit();
                        editor.clear();
                        editor.apply();


                        //Toast.makeText(this,"Service stopped",Toast.LENGTH_SHORT);
                    }

                    //stop service and clear notifications
                    stopService(new Intent(this, NotificationDailyOfferService.class));
                    NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(001);
                    notificationManager.cancel(002);

                    Intent i = new Intent(this, HomePageActivity.class);
                    startActivity(i);
                    finish();
                }
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /*************************************************/

    @Override
    protected void onDestroy() {
        restaurantsRef.removeEventListener(listener);
        super.onDestroy();
    }


}



