package it.polito.mad.insane.lab4.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.bumptech.glide.Glide;
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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.DailyOfferRecyclerAdapter;
import it.polito.mad.insane.lab4.adapters.DishesRecyclerAdapter;
import it.polito.mad.insane.lab4.adapters.ReviewsRecyclerAdapter;
import it.polito.mad.insane.lab4.data.Cart;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.data.Dish;
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.data.RestaurantInfo;
import it.polito.mad.insane.lab4.data.Review;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;


//T_ODO cambiare logica di aggiunta prenotazione (Renato da fare insieme a Federico)

public class RestaurantProfileActivity extends AppCompatActivity {

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

    static final String PREF_LOGIN = "loginPref";
    private ViewPager mViewPager;
    private static FloatingActionButton menuFab;
    private static FloatingActionButton reviewsFab;
    private static RestaurateurJsonManager manager = null;
    private static String restaurantId;
    private static String restaurantName;
    private static DishesRecyclerAdapter dishesAdapter = null;
    private static HashMap<String, Review> reviewsMap = new HashMap<>();
    private static Restaurant restaurant;
    private static TextView noOffersTextView;
    private static TextView noDishesTextView;
    private static int reviewsNumber;
    private static Cart cart = null;

    public static Activity RestaurantProfileActivity = null; // attribute used to finish() the current activity from another activity

    @Override
    public void finish()
    {
        super.finish();
        RestaurantProfileActivity = null;
        clearStaticVariables();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.RestaurantProfileActivity = this;

        setContentView(R.layout.restaurant_profile_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        manager = RestaurateurJsonManager.getInstance(this);
        if(getIntent().getStringExtra("ID") != null && getIntent().getStringExtra("Name") != null)
        {
            restaurantId = getIntent().getStringExtra("ID");
            restaurantName = getIntent().getStringExtra("Name");
            setTitle(restaurantName);
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReferenceFromUrl("gs://lab4-insane.appspot.com/restaurants/" + restaurantId +
                "/cover.jpg");
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                // Pass it to Picasso to download, show in ImageView and caching

                ImageView img = (ImageView)findViewById(R.id.coverPhoto);

                Glide.with(RestaurantProfileActivity.this)
                        .load(uri.toString())
                        .placeholder(R.drawable.default_img_rest_1)
                        .into(img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

//        NestedScrollView scrollView = (NestedScrollView) findViewById (R.id.scrollView);
//        scrollView.setFillViewport (true);

        if(cart != null)
            editShowButton(cart.getReservationQty(), cart.getReservationPrice());

        // set button
        menuFab = (FloatingActionButton) findViewById(R.id.fab_cart);
        menuFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
                String userID = null;
                if (mPrefs != null) {
                    userID = mPrefs.getString("uid", null);
                }

                if(userID == null){
                    Toast.makeText(it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.this,
                            getResources().getString(R.string.logged_in_booking_alert), Toast.LENGTH_LONG).show();
                }
                else if(cart == null || cart.getReservationQty() == 0) {
                    Toast.makeText(it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.this,
                            getResources().getString(R.string.cart_empty_alert), Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intent = new Intent(it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.this, MakeReservationActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("cart", cart);
                    bundle.putString("ID", restaurantId);
                    bundle.putString("restName", restaurantName);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

            }
        });

        reviewsFab = (FloatingActionButton) findViewById(R.id.fab_add_review);
        reviewsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
                String userID = null;
                if (mPrefs != null) {
                    userID = mPrefs.getString("uid", null);
                }

                if(userID == null){
                    Toast.makeText(it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.this,
                            getResources().getString(R.string.logged_in_review_alert), Toast.LENGTH_LONG).show();
                }
                else{
                    Intent intent = new Intent(it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.this, AddReviewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ID", restaurantId);
                    bundle.putSerializable("scoresMap", (Serializable) restaurant.getAvgScores());
                    bundle.putDouble("finalScore", restaurant.getAvgFinalScore());
                    bundle.putInt("reviewsNumber", reviewsNumber);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        // Set up the ViewPager with the sections adapter.
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
                    case 0: // DAILY OFFER fragment
                        menuFab.setVisibility(View.VISIBLE);
                        if (cart != null)
                            editShowButton(cart.getReservationQty(), cart.getReservationPrice());
                        break;
                    case 1: // MENU fragment
                        menuFab.setVisibility(View.VISIBLE);
                        if (cart != null)
                            editShowButton(cart.getReservationQty(), cart.getReservationPrice());
                        break;
                    case 3: //REVIEWS fragment
                        reviewsFab.setVisibility(View.VISIBLE);
                        break;
                    default:
                        menuFab.setVisibility(View.GONE);
                        reviewsFab.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.restaurant_tabs);
        // Attach the view pager to the tab strip
        if (tabsStrip != null) {
            tabsStrip.setViewPager(mViewPager);
            tabsStrip.setBackgroundColor(ContextCompat.getColor(it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.this, R.color.colorPrimary));
            tabsStrip.setTextColor(ContextCompat.getColor(it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.this, R.color.white));
            tabsStrip.setIndicatorColor(ContextCompat.getColor(it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.this, R.color.white));
        }
//        if(dishesAdapter != null)
//            editShowButton(dishesAdapter.getReservationQty(), dishesAdapter.getReservationPrice());

        // Fix Portrait Mode
        if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dishesAdapter = null;
//        reservationList = null;
        restaurantId = null;
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(cart != null)
            editShowButton(cart.getReservationQty(), cart.getReservationPrice());
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        // get restaurant in order to get his avg final score
        final DatabaseReference restaurantRef = database.getReference("restaurants/" + restaurantId);
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                restaurant = dataSnapshot.getValue(Restaurant.class);

                if (restaurant != null) {
                    reviewsNumber = restaurant.getReviewsNumber();

                    if (restaurant.getAvgFinalScore() == -1) {
                        TextView tv = (TextView) findViewById(R.id.review_fragment_no_reviews);
                        if (tv != null) {
                            tv.setVisibility(View.VISIBLE);
                        }
                        CardView cv = (CardView) findViewById(R.id.review_total_score_cardview);
                        if (cv != null) {
                            cv.setVisibility(View.GONE);
                        }
                        RecyclerView rv = (RecyclerView) findViewById(R.id.reviews_recycler_view);
                        if (rv != null) {
                            rv.setVisibility(View.GONE);
                        }
                    }
                    else {
                        TextView tv = (TextView) findViewById(R.id.restaurant_final_score);
                        DecimalFormat df = new DecimalFormat("0.0");
                        if (tv != null) {
                            tv.setText(df.format(restaurant.getAvgFinalScore()));
                        }

                        tv = (TextView) findViewById(R.id.score_1);
                        df = new DecimalFormat("0.0");
                        if (tv != null) {
                            tv.setText(df.format(restaurant.getAvgScores().get(getString(R.string.first_score))));
                        }

                        tv = (TextView) findViewById(R.id.score_2);
                        df = new DecimalFormat("0.0");
                        if (tv != null) {
                            tv.setText(df.format(restaurant.getAvgScores().get(getString(R.string.second_score))));
                        }

                        tv = (TextView) findViewById(R.id.score_3);
                        df = new DecimalFormat("0.0");
                        if (tv != null) {
                            tv.setText(df.format(restaurant.getAvgScores().get(getString(R.string.third_score))));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final DatabaseReference reviewsRef = database.getReference("/reviews/restaurants/"+restaurantId);

        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Review> data = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Review>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });
                if(data != null)
                {
                    reviewsMap.putAll(data);
//                        ArrayList<Review> reviewsList = new ArrayList<Review>(data.values());
                    RecyclerView rv = (RecyclerView) findViewById(R.id.reviews_recycler_view);
                    if (rv != null) {
                        ReviewsRecyclerAdapter adapter = new ReviewsRecyclerAdapter(RestaurantProfileActivity.this,
                                new ArrayList<>(reviewsMap.values()));
                        rv.setAdapter(adapter);
                    }
                    TextView tv = (TextView) findViewById(R.id.reviews_number);
                    if (tv != null) {
                        tv.setText(String.format(getResources().getString(R.string.reviewsFormat), data.size()));
                    }
//                    reviewsNumber = data.size();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void editShowButton(int quantity, double price) {

        TextView tv = (TextView) findViewById(R.id.daily_offer_cart);
        if (tv != null) {
            if (quantity != 0) {
                DecimalFormat df = new DecimalFormat("0.00");
                tv.setText(String.format("%d " + getResources().getString(R.string.itemsFormat) + " - %s€", quantity, df.format(price)));
            }
            else
                tv.setText(R.string.empty_cart);
        }

        tv = (TextView) findViewById(R.id.show_reservation_button);
        if (tv != null) {
            if (quantity != 0)
                tv.setText(String.format("%d "+getResources().getString(R.string.itemsFormat)+" - %s€", quantity, price));
            else
                tv.setText(R.string.empty_cart);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                MakeReservationActivity.clearStaticVariables();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void clearStaticVariables()
    {
        it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.dishesAdapter = null;
        it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.cart = null;
//        it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.reservationList = null;
        it.polito.mad.insane.lab4.activities.RestaurantProfileActivity.restaurantId = null;
        reviewsMap.clear();
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        final int PAGE_COUNT = 4;

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position); // da qui ho tolto il +1
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
                case 2:
                    return getString(R.string.title_tab_info);
                case 3:
                    return getString(R.string.title_tab_reviews);
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
        public static PlaceholderFragment newInstance(int sectionNumber) {
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
                case 0:
                    rootView = dailyOfferLayout(inflater,container);
                    return rootView;
                case 1:
                    rootView = menuLayout(inflater, container);
                    return rootView;
                case 2:
                    rootView = infoLayout(inflater, container);
                    return rootView;
                case 3:
                    rootView = reviewsLayout(inflater, container);
                    return rootView;
                default:
                    return null;
            }
        }

        private View dailyOfferLayout(LayoutInflater inflater, ViewGroup container)
        {
            final View rootView = inflater.inflate(R.layout.restaurant_daily_offer_fragment, container, false);
            noOffersTextView = (TextView) rootView.findViewById(R.id.offer_fragment_no_offers);

            // take istance of the manager
            manager = RestaurateurJsonManager.getInstance(getActivity());


            // take data from Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("/restaurants/" + restaurantId + "/dailyOfferMap");

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // method invoked when data are availables
                    HashMap<String,DailyOffer> r = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, DailyOffer>>() {
                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }
                    });

                    rootView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);

                    // set up recycler view
                    if(r != null) {

                        setupDailyOfferRecyclerView(rootView, new ArrayList<>(r.values()));
                    }
                    else
                    {

                        if (noOffersTextView != null) {
                            noOffersTextView.setVisibility(View.VISIBLE);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if(cart != null)
                editShowButton(cart.getReservationQty(), cart.getReservationPrice(), rootView);
            return rootView;
        }
        private void setupDailyOfferRecyclerView(View rootView, List<DailyOffer> offers)
        {
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.daily_offers_recycler_view);
            if (cart == null)
                cart = new Cart();
            RecyclerView.Adapter dailyOffersAdapter = new DailyOfferRecyclerAdapter(getActivity(), offers, restaurantId, 0, cart);

            if(recyclerView != null)
            {
                recyclerView.setAdapter(dailyOffersAdapter);
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

        private View infoLayout(LayoutInflater inflater, ViewGroup container) {
            View rootView = inflater.inflate(R.layout.restaurant_info_fragment, container, false);
//            TextView tv = (TextView) getActivity().findViewById(R.id.chart_selection);
//            tv.setVisibility(View.GONE);
            loadProfileData(rootView);

            return rootView;
        }

        private void loadProfileData(final View rootView) {
            manager = RestaurateurJsonManager.getInstance(getActivity());

            // set up clean Recycler
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("/restaurants/" + restaurantId + "/info");

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    RestaurantInfo profile = dataSnapshot.getValue(RestaurantInfo.class);
                    if(profile!=null) {
                        TextView tv;

                        tv = (TextView) rootView.findViewById(R.id.editName);
                        if (tv != null) {
                            if (profile.getRestaurantName() != null)
                                tv.setText(profile.getRestaurantName());
                        }
                        tv = (TextView) rootView.findViewById(R.id.editAddress);
                        if (tv != null) {
                            if (profile.getAddress() != null)
                                tv.setText(profile.getAddress());
                        }
                        tv = (TextView) rootView.findViewById(R.id.editDescription);
                        if (tv != null) {
                            if (profile.getDescription() != null)
                                tv.setText(profile.getDescription());
                        }
                        tv = (TextView) rootView.findViewById(R.id.editTimeNotes);
                        if (tv != null) {
                            if (profile.getTimeInfo() != null)
                                tv.setText(profile.getTimeInfo());
                        }
                        tv = (TextView) rootView.findViewById(R.id.editPayment);
                        if (tv != null) {
                            if (profile.getPaymentMethod() != null)
                                tv.setText(profile.getPaymentMethod());
                        }
                        tv = (TextView) rootView.findViewById(R.id.editServices);
                        if (tv != null) {
                            if (profile.getAdditionalServices() != null)
                                tv.setText(profile.getAdditionalServices());
                        }
                        tv = (TextView) rootView.findViewById(R.id.university);
                        if (tv != null) {
                            if (profile.getNearbyUniversity() != null)
                                tv.setText(profile.getNearbyUniversity());
                        }
                        tv = (TextView) rootView.findViewById(R.id.cuisineType);
                        if (tv != null) {
                            if (profile.getCuisineType() != null)
                                tv.setText(profile.getCuisineType());
                        }

                        tv = (TextView) rootView.findViewById(R.id.openingHour_title);
                        if (tv != null) {
                            String date = profile.getOpeningHour();
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            Calendar cal = Calendar.getInstance();
                            try {
                                cal.setTime(sdf.parse(date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return;
                            }
                            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                            int minute = cal.get(Calendar.MINUTE);
                            tv.setText(String.format("%s:%s", pad(hourOfDay), pad(minute)));
                        }
                        tv = (TextView) rootView.findViewById(R.id.closingHour_title);
                        if (tv != null) {
                            String date = profile.getClosingHour();
                            Calendar cal = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            try {
                                cal.setTime(sdf.parse(date));
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return;
                            }
                            int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                            int minute = cal.get(Calendar.MINUTE);
                            tv.setText(String.format("%s:%s", pad(hourOfDay), pad(minute)));
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        private View menuLayout(LayoutInflater inflater, ViewGroup container)
        {
            final View rootView = inflater.inflate(R.layout.restaurant_menu_fragment, container, false);
//            TextView tv = (TextView) getActivity().findViewById(R.id.chart_selection);
//            tv.setVisibility(View.VISIBLE);
            noDishesTextView = (TextView) rootView.findViewById(R.id.dish_fragment_no_dishes);
            // take the list of dishes from manager
            manager = RestaurateurJsonManager.getInstance(getActivity());

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("/restaurants/" + restaurantId + "/dishMap");

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, Dish> r = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Dish>>() {
                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }
                    });

                    rootView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    if (r!=null)
                        setupDishesRecyclerView(rootView, new ArrayList<>(r.values()));
                    else
                    {
                        if (noDishesTextView != null) {
                            noDishesTextView.setVisibility(View.VISIBLE);
                        }
                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

           // Restaurant restaurant = manager.getRestaurant(restaurantId);

            // set up dishesRecyclerView
            //final RecyclerView rv = setupDishesRecyclerView(rootView, restaurant.getDishes());

            if (cart != null)
                editShowButton(cart.getReservationQty(), cart.getReservationPrice(), rootView);

            return rootView;
        }

        private View reviewsLayout(LayoutInflater inflater, ViewGroup container) {
            final View rootView = inflater.inflate(R.layout.restaurant_reviews_fragment, container, false);

//            TextView tv = (TextView) getActivity().findViewById(R.id.chart_selection);
//            tv.setVisibility(View.GONE);

            manager = RestaurateurJsonManager.getInstance(getActivity());

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            // get restaurant in order to get his avg final score
            final DatabaseReference restaurantRef = database.getReference("restaurants/" + restaurantId);
            restaurantRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    restaurant = dataSnapshot.getValue(Restaurant.class);


                    rootView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);

                    if(restaurant != null) {
                        if (restaurant.getAvgFinalScore() == -1){
                            TextView tv = (TextView) rootView.findViewById(R.id.review_fragment_no_reviews);
                            tv.setVisibility(View.VISIBLE);
                            CardView cv = (CardView) rootView.findViewById(R.id.review_total_score_cardview);
                            cv.setVisibility(View.GONE);
                            RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.reviews_recycler_view);
                            rv.setVisibility(View.GONE);
                        }
                        else {
                            TextView tv = (TextView) rootView.findViewById(R.id.restaurant_final_score);
                            DecimalFormat df = new DecimalFormat("0.0");
                            tv.setText(df.format(restaurant.getAvgFinalScore()));

                            tv = (TextView) rootView.findViewById(R.id.score_1);
                            df = new DecimalFormat("0.0");
                            tv.setText(df.format(restaurant.getAvgScores().get(getString(R.string.first_score))));

                            tv = (TextView) rootView.findViewById(R.id.score_2);
                            df = new DecimalFormat("0.0");
                            tv.setText(df.format(restaurant.getAvgScores().get(getString(R.string.second_score))));

                            tv = (TextView) rootView.findViewById(R.id.score_3);
                            df = new DecimalFormat("0.0");
                            tv.setText(df.format(restaurant.getAvgScores().get(getString(R.string.third_score))));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            final DatabaseReference reviewsRef = database.getReference("/reviews/restaurants/"+restaurantId);

            reviewsRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, Review> data = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Review>>() {
                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }
                    });
                    if(data != null)
                    {
                        reviewsMap.putAll(data);
//                        ArrayList<Review> reviewsList = new ArrayList<Review>(data.values());
                        setupReviewsRecyclerView(rootView, new ArrayList<>(reviewsMap.values()));
                        TextView tv = (TextView) rootView.findViewById(R.id.reviews_number);
                        tv.setText(String.format(getResources().getString(R.string.reviewsFormat), data.size()));
                        reviewsNumber = data.size();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return rootView;
        }

        private void setupReviewsRecyclerView(View rootView, List<Review> reviews)
        {
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.reviews_recycler_view);
            RecyclerView.Adapter reviewsAdapter = new ReviewsRecyclerAdapter(getActivity(), reviews);
            if(recyclerView != null){
                recyclerView.setAdapter(reviewsAdapter);
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

        private RecyclerView setupDishesRecyclerView(View rootView, List<Dish> dishes)
        {
            // set Adapter
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.MenuRecyclerView);
            if (recyclerView != null) {
                if(dishesAdapter == null) {
                    if (cart == null)
                        cart = new Cart();
                    dishesAdapter = new DishesRecyclerAdapter(getActivity(), dishes, restaurantId, 0, cart);
                }
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


        public static void editShowButton(int quantity, double price, View rootView) {
//        if (tv == null)
            TextView tv = (TextView) rootView.findViewById(R.id.show_reservation_button);
            if (tv != null) {
                if(quantity != 0) {
                    DecimalFormat df = new DecimalFormat("0.00");
                    tv.setText(String.format("%d " + rootView.getResources().getString(R.string.itemsFormat) + " - %s€", quantity,
                            df.format(price)));
                }
                else
                    tv.setText(R.string.empty_cart);

            }
        }

        private String pad(int c) {
            if (c >= 10)
                return String.valueOf(c);
            else
                return "0" + String.valueOf(c);
        }
    }
}
