package it.polito.mad.insane.lab4.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.DailyOfferRecyclerAdapter;
import it.polito.mad.insane.lab4.adapters.DishesRecyclerAdapter;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.data.Dish;

/**
 * Created by Federico on 02/06/2016.
 */
public class DailyMenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    public static Activity DailyMenuActivity = null; // attribute used to finish() the current activity from another activity

    private ViewPager mViewPager;
    private static DishesRecyclerAdapter dishesAdapter = null;
    private static DailyOfferRecyclerAdapter offersAdapter = null;
    private static HashMap<String, Dish> dishesLocalCache = new HashMap<>();// questa è la copia locale dei dati scaricati mano a mano dal DB e dalla quale si genera dishesList
    private static ArrayList<Dish> dishesList = new ArrayList<>(); // Questa è la lista che viene passata all'adapter sulla quale bisogna agire per modificare l'adapter
    private static HashMap<String,DailyOffer> dailyOffersLocalCache = new HashMap<>(); // questa è la copia locale dei dati scaricati mano a mano dal DB e dalla quale si genera offersList
    private static ArrayList<DailyOffer> offersList = new ArrayList<>(); // Questa è la lista che viene passata all'adapter sulla quale bisogna agire per modificare l'adapter
    static final String PREF_LOGIN = "loginPref";
    private SharedPreferences mPrefs = null;
    private static String rid; // restaurant id
    private static TextView noOffersTextView;
    private static TextView noDishesTextView;
    private NavigationView navigationView;
    //static private RestaurateurJsonManager manager = null;

    //TODO: sarebbe da sostituire la logica dei listener usando "addValueEventListener(listener)" che rende l'app piu interattiva (guarda MyReservationUserActivity) - (Michele)

    /**
     * Standard Methods
     **/

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
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        DailyMenuActivity = null;
        dailyOffersLocalCache.clear();
        dishesLocalCache.clear();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //DailyMenuActivity.manager = RestaurateurJsonManager.getInstance(this);
        DailyMenuActivity = this;
        setContentView(R.layout.daily_menu_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        if (mPrefs != null) {
            rid = this.mPrefs.getString("rid", null);
        }


        // set add_dish fab button
        final FloatingActionButton dishFab = (FloatingActionButton) findViewById(R.id.add_dish);
        if (dishFab != null) {
            dishFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // open activity EditDishActivity
                    Intent i = new Intent(view.getContext(),EditDishActivity.class);
                    view.getContext().startActivity(i);

                }
            });
        }
        // set add_offer fab button
        final FloatingActionButton offerFab = (FloatingActionButton) findViewById(R.id.add_offer);
        if (offerFab != null) {
            offerFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // open activity EditOffer
                    if(dishesList == null || dishesList.isEmpty())
                        Toast.makeText(DailyMenuActivity.this, R.string.error_no_dishes, Toast.LENGTH_LONG).show();
                    else
                    {
                        Intent i = new Intent(view.getContext(),EditOfferActivity.class);
                        view.getContext().startActivity(i);
                    }

                }
            });
        }

        // Set up the ViewPager with the sections dishesAdapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch(position)
                {
                    case 0:
                        dishFab.setVisibility(View.GONE);
                        offerFab.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        dishFab.setVisibility(View.VISIBLE);
                        offerFab.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.restaurant_dailymenu_tabs);
        // Attach the view pager to the tab strip
        if (tabsStrip != null) {
            tabsStrip.setViewPager(mViewPager);
            tabsStrip.setBackgroundColor(ContextCompat.getColor(DailyMenuActivity.this, R.color.colorPrimary));
            tabsStrip.setTextColor(ContextCompat.getColor(DailyMenuActivity.this, R.color.white));
            tabsStrip.setIndicatorColor(ContextCompat.getColor(DailyMenuActivity.this, R.color.white));
        }

        // Fix Portrait Mode
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
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
        if(mPrefs != null) {
            title_drawer.setText(mPrefs.getString("rUser", null));
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
            case R.id.home_restaurateur_activity:
                if(!getClass().equals(HomeRestaurateurActivity.class))
                {
                    // finish the HomeRestaurateurActivity if is not finished
                    if(HomeRestaurateurActivity.HomeRestaurateurActivity != null)
                        HomeRestaurateurActivity.HomeRestaurateurActivity.finish();

                    Intent i = new Intent(this, HomeRestaurateurActivity.class);
                    startActivity(i);
                }
                break;
            case R.id.action_daily_menu:
                if(!getClass().equals(DailyMenuActivity.class))
                {
                    // finish the DailyMenuActivity if is not finished
                    if(DailyMenuActivity != null)
                        DailyMenuActivity.finish();

                    // Start DailyMenuActivity activity
                    Intent invokeDailyMenu = new Intent(this, DailyMenuActivity.class);
                    startActivity(invokeDailyMenu);
                    break;
                }

            case R.id.my_reviews_restaurant:
                if(!getClass().equals(MyReviewsRestaurantActivity.class))
                {
                    // finish the MyReviewsRestaurantActivity if is not finished
                    if(MyReviewsRestaurantActivity.MyReviewsRestaurantActivity != null)
                        MyReviewsRestaurantActivity.MyReviewsRestaurantActivity.finish();

                    Intent invokeMyReviewsRestaurant = new Intent(this, MyReviewsRestaurantActivity.class);
                    startActivity(invokeMyReviewsRestaurant);
                }
                break;

            case R.id.action_edit_profile:
                if(!getClass().equals(EditProfileRestaurateurActivity.class))
                {
                    // finish the EditProfileRestaurateurActivity if is not finished
                    if(EditProfileRestaurateurActivity.EditProfileRestaurateurActivity != null)
                        EditProfileRestaurateurActivity.EditProfileRestaurateurActivity.finish();

                    //Start EditProfileActivity
                    Intent invokeEditProfile = new Intent(this, EditProfileRestaurateurActivity.class);
                    startActivity(invokeEditProfile);
                }
                break;

            case R.id.logout_restaurateur_drawer:
                if(rid == null){
                    Toast.makeText(this, R.string.not_logged,Toast.LENGTH_SHORT).show();
                }else {
                    this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
                    if (mPrefs != null) {
                        rid = null;
                        SharedPreferences.Editor editor = this.mPrefs.edit();
                        editor.clear();
                        editor.apply();
                    }
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
    protected void onResume() {
        super.onResume();

        navigationView.getMenu().findItem(R.id.action_daily_menu).setChecked(true);
        if (dishesAdapter != null)
            dishesAdapter.notifyDataSetChanged();
        if(offersAdapter != null)
            offersAdapter.notifyDataSetChanged();
        updateDishes();
        updateDailyOffers();

    }

    /**
     * Our Methods
     */
    private void updateDishes()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/restaurants/" + rid + "/dishMap");
        noDishesTextView = (TextView) findViewById(R.id.dish_fragment_no_dishes);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Dish> rTemp = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Dish>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });
                if (rTemp!=null)
                {
                    dishesLocalCache.putAll(rTemp);
                    dishesList = new ArrayList<>(dishesLocalCache.values());
                    dishesAdapter = new DishesRecyclerAdapter(DailyMenuActivity.this, dishesList, rid, 1);
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.MenuRecyclerView);
                    if (recyclerView != null)
                    {
                        recyclerView.setAdapter(dishesAdapter);
                        dishesAdapter.notifyDataSetChanged();
                    }

                }else
                {
                    if (noDishesTextView != null && dishesList.isEmpty()) // crasha
                        noDishesTextView.setVisibility(View.VISIBLE);

                    if(dishesList.isEmpty()){
                        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.MenuRecyclerView);
                        recyclerView.setVisibility(View.GONE);
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateDailyOffers()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/restaurants/" + rid + "/dailyOfferMap");
        noOffersTextView = (TextView) findViewById(R.id.offer_fragment_no_offers);

        myRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // method invoked when data are availables
                HashMap<String,DailyOffer> rTemp = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, DailyOffer>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });

                // set up recycler view
                if(rTemp!=null)
                {
                    dailyOffersLocalCache.putAll(rTemp);
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.daily_offers_recycler_view);
                    offersList = new ArrayList<>(dailyOffersLocalCache.values());
                    offersAdapter = new DailyOfferRecyclerAdapter(DailyMenuActivity.this, offersList, rid, 1);
                    if(recyclerView != null) {
                        recyclerView.setAdapter(offersAdapter);
                        offersAdapter.notifyDataSetChanged();
                    }
                }
                else
                {
                    if (noOffersTextView != null && offersList.isEmpty())
                        noOffersTextView.setVisibility(View.VISIBLE);

                    if(offersList.isEmpty()) {
                        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.daily_offers_recycler_view);
                        recyclerView.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        final int PAGE_COUNT = 2;

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position); //  da qui ho tolto il +1
        }

        @Override
        public int getCount()
        {
            // Return the number of total pages.
            return this.PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0:
                    return getString(R.string.title_tab_daily_offer);
                case 1:
                    return getString(R.string.title_tab_menu);
                default:
                    return null;
            }
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment
    {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber)
        {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = null;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 0: // Daily Offer
                    rootView = dailyOfferLayout(inflater,container);
                    return rootView;
                case 1: // Daily Menu
                    rootView = menuLayout(inflater, container);
                    return rootView;
                default:
                    return null;
            }
        }


        private View menuLayout(LayoutInflater inflater, ViewGroup container)
        {
            final View rootView = inflater.inflate(R.layout.restaurant_menu_fragment, container, false);
            noDishesTextView = (TextView) rootView.findViewById(R.id.dish_fragment_no_dishes);

//            manager = RestaurateurJsonManager.getInstance(getActivity());

            // remove the cart textview
            TextView cartText = (TextView) rootView.findViewById(R.id.show_reservation_button);
            cartText.setVisibility(View.GONE);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("/restaurants/" + rid + "/dishMap");

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, Dish> rTemp = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Dish>>() {
                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }
                    });
                    if (rTemp!=null)
                    {
                        dishesLocalCache.putAll(rTemp);
                        dishesList = new ArrayList<>(dishesLocalCache.values());
                        setupDishesRecyclerView(rootView, dishesList);
                    }else
                    {
                        if (noDishesTextView != null && dishesList.isEmpty())
                            noDishesTextView.setVisibility(View.VISIBLE);

                        if(dishesList == null)
                            dishesList = new ArrayList<>();
                        setupDishesRecyclerView(rootView, dishesList);
                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return rootView;
        }
        private RecyclerView setupDishesRecyclerView(View rootView, List<Dish> dishes)
        {

            // set Adapter
            dishesAdapter = new DishesRecyclerAdapter(getActivity(), dishes, rid, 1);
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.MenuRecyclerView);
            if (recyclerView != null)
            {
                recyclerView.setAdapter(dishesAdapter);
                dishesAdapter.notifyDataSetChanged();

                // set Layout Manager
                if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
                {
                    // 10 inches
                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    {
                        // 2 columns
                        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getActivity(), 2);
                        recyclerView.setLayoutManager(mGridLayoutManager);
                    }else
                    {
                        // 3 columns
                        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getActivity(), 3);
                        recyclerView.setLayoutManager(mGridLayoutManager);
                    }

                } else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
                {
                    // 7 inches
                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    {
                        // 2 columns
                        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getActivity(), 2);
                        recyclerView.setLayoutManager(mGridLayoutManager);

                    }else
                    {
                        // 1 column
                        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(getActivity());
                        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(mLinearLayoutManagerVertical);
                    }
                }else {
                    // small and normal screen
                    // 1 columns
                    LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(getActivity());
                    mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(mLinearLayoutManagerVertical);
                }


                // set Animator
                recyclerView.setItemAnimator(new DefaultItemAnimator()); // default animations
                return recyclerView;
            }

            return null;
        }

        private View dailyOfferLayout(LayoutInflater inflater, ViewGroup container)
        {
            final View rootView = inflater.inflate(R.layout.daily_offer_fragment, container, false);
            noOffersTextView = (TextView) rootView.findViewById(R.id.offer_fragment_no_offers);

            // take istance of the manager
//            manager = RestaurateurJsonManager.getInstance(getActivity());

            // take data from Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("/restaurants/" + rid + "/dailyOfferMap");//

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // method invoked when data are availables
                    HashMap<String,DailyOffer> rTemp = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, DailyOffer>>() {
                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }
                    });
                    // set up recycler view
                    if(rTemp!=null)
                    {
                        dailyOffersLocalCache.putAll(rTemp);
                        offersList = new ArrayList<>(dailyOffersLocalCache.values());
                        setupDailyOfferRecyclerView(rootView, offersList);
                    }

                    else
                    {
                        if (noOffersTextView != null && offersList.isEmpty())
                            noOffersTextView.setVisibility(View.VISIBLE);

                        if(offersList == null)
                            offersList = new ArrayList<>();
                        setupDailyOfferRecyclerView(rootView, offersList);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return rootView;
        }
        private void setupDailyOfferRecyclerView(View rootView, List<DailyOffer> offers)
        {
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.daily_offers_recycler_view);
            offersAdapter = new DailyOfferRecyclerAdapter(getActivity(), offers, rid, 1);

            if(recyclerView != null)
            {
                recyclerView.setAdapter(offersAdapter);
                offersAdapter.notifyDataSetChanged();

                // set Layout Manager
                if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
                {
                    // 10 inches
                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    {
                        // 2 columns
                        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getActivity(), 2);
                        recyclerView.setLayoutManager(mGridLayoutManager);
                    }else
                    {
                        // 3 columns
                        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getActivity(), 3);
                        recyclerView.setLayoutManager(mGridLayoutManager);
                    }

                } else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
                {
                    // 7 inches
                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    {
                        // 2 columns
                        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getActivity(), 2);
                        recyclerView.setLayoutManager(mGridLayoutManager);

                    }else
                    {
                        // 1 column
                        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(getActivity());
                        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                        recyclerView.setLayoutManager(mLinearLayoutManagerVertical);
                    }
                }else {
                    // small and normal screen
                    // 1 columns
                    LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(getActivity());
                    mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(mLinearLayoutManagerVertical);
                }


                // set Animator
                recyclerView.setItemAnimator(new DefaultItemAnimator());
            }
        }
    }

    public static void removeOffer(DailyOffer offer)
    {
        dailyOffersLocalCache.remove(offer.getID());
        for(Iterator<DailyOffer> it = offersList.iterator(); it.hasNext();)
        {
            DailyOffer d = it.next();
            if (d.getID().equals(offer.getID()))
                it.remove();
        }
        offersAdapter.notifyDataSetChanged();
    }

    public static void removeDish(Dish dish)
    {
        dishesLocalCache.remove(dish.getID());
        for(Iterator<Dish> it = dishesList.iterator(); it.hasNext();)
        {
            Dish d = it.next();
            if(d.getID().equals(dish.getID()))
                it.remove();
        }

        dishesAdapter.notifyDataSetChanged();
    }

    public static void notifyNewDish(Context context, Dish dish)
    {
        if(dishesList == null)
            dishesList = new ArrayList<>();
        dishesList.add(dish);

        if(dishesAdapter == null)
            dishesAdapter = new DishesRecyclerAdapter(context, dishesList, rid, 1);
        dishesAdapter.notifyDataSetChanged();

    }
    public static void notifyNewOffer(Context context, DailyOffer offer)
    {
        if(offersList == null)
            offersList = new ArrayList<>();
        offersList.add(offer);

        if(offersAdapter == null)
            offersAdapter = new DailyOfferRecyclerAdapter(context, offersList, rid, 1);
        offersAdapter.notifyDataSetChanged();
    }

}
