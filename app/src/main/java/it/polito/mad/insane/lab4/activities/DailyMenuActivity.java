package it.polito.mad.insane.lab4.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
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
import it.polito.mad.insane.lab4.adapters.DailyOfferRecyclerAdapter;
import it.polito.mad.insane.lab4.adapters.DishesRecyclerAdapter;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.data.Dish;

/**
 * Created by Federico on 02/06/2016.
 */
public class DailyMenuActivity extends AppCompatActivity {

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

    private ViewPager mViewPager;
    private static DishesRecyclerAdapter dishesAdapter = null;
    private static DailyOfferRecyclerAdapter offersAdapter = null;
    private ArrayList<Dish> dishes;
    private static HashMap<String,DailyOffer> dailyOffersLocalCache = new HashMap<>(); // questa è la copia locale dei dati scaricati mano a mano dal DB e dalla quale si genera offersList
    private static ArrayList<DailyOffer> offersList; // Questa è la lista che viene passata all'adapter sulla quale bisogna agire per modificare l'adapter
    static final String PREF_LOGIN = "loginPref";
    private SharedPreferences mPrefs = null;
    private static String rid; // restaurant id
    //static private RestaurateurJsonManager manager = null;

    /**
     * Standard Methods
     **/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        dailyOffersLocalCache.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //DailyMenuActivity.manager = RestaurateurJsonManager.getInstance(this);

        setContentView(R.layout.daily_menu_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // show back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        if (mPrefs != null) {
            rid = this.mPrefs.getString("rid", null);
        }

        // initialize Recycler View
//        updateDishes();

        // TODO: Le cardview dei dishes quando si clicca non devono venire fuori + e - ma deve rimandare alla edit dishes (Michele)

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
                    Intent i = new Intent(view.getContext(),EditOfferActivity.class);
                    view.getContext().startActivity(i);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Dish> r = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Dish>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });
                if (r!=null)
                {
                    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.MenuRecyclerView);
                    if (recyclerView != null)
                    {
                        if(dishesAdapter == null)
                            dishesAdapter = new DishesRecyclerAdapter(DailyMenuActivity.this, new ArrayList<Dish>(r.values()), rid);
                        recyclerView.setAdapter(dishesAdapter);
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
                    offersList = new ArrayList<DailyOffer>(dailyOffersLocalCache.values());
                    offersAdapter = new DailyOfferRecyclerAdapter(DailyMenuActivity.this, offersList, rid, 1);
                    if(recyclerView != null) {
                        recyclerView.setAdapter(offersAdapter);
                        offersAdapter.notifyDataSetChanged();
                    }
                }
                else
                {
                    //TODO: far uscire un messaggio che indica che non ci sono recensioni disponibili (Michele)
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

//            manager = RestaurateurJsonManager.getInstance(getActivity());

            // remove the cart textview
            TextView cartText = (TextView) rootView.findViewById(R.id.show_reservation_button);
            cartText.setVisibility(View.GONE);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("/restaurants/" + rid + "/dishMap");

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, Dish> r = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Dish>>() {
                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }
                    });
                    if (r!=null)
                        setupDishesRecyclerView(rootView, new ArrayList<>(r.values()));

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
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.MenuRecyclerView);
            if (recyclerView != null) {
                if(dishesAdapter == null)
                    dishesAdapter = new DishesRecyclerAdapter(getActivity(), dishes, null);
                recyclerView.setAdapter(dishesAdapter);
            }

            if (recyclerView != null)
            {
                recyclerView.setAdapter(dishesAdapter);

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
                        DailyMenuActivity.dailyOffersLocalCache.putAll(rTemp);
                        offersList = new ArrayList<>(dailyOffersLocalCache.values());
                        setupDailyOfferRecyclerView(rootView, offersList);
                    }

                    else
                    {
                        //TODO: far uscire un messaggio che indica che non ci sono recensioni disponibili (Michele)
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
        for( DailyOffer d : offersList)
            if(d.getID().equals(offer.getID()))
                offersList.remove(d);
        offersAdapter.notifyDataSetChanged();
    }
}
