package it.polito.mad.insane.lab4.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.ReviewsRecyclerAdapter;
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.data.Review;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class MyReviewsRestaurantActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    public static Activity MyReviewsRestaurantActivity = null;
    static final String PREF_LOGIN = "loginPref";
    private SharedPreferences mPrefs = null;
    private static String rid;
    private NavigationView navigationView;

    private DatabaseReference restaurantRef = null;
    private DatabaseReference reviewsRef = null;

    private ValueEventListener restaurantListener = null;
    private ValueEventListener reviewsListener = null;

    @Override
    public void finish()
    {
        super.finish();
        MyReviewsRestaurantActivity = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        MyReviewsRestaurantActivity = this;
        setContentView(R.layout.my_reviews_restaurant_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.action_myReviews_restaurant);

        // check login
        if(rid == null)
        {
            this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
            if (mPrefs != null) {
                rid = this.mPrefs.getString("rid", null);
            }
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        restaurantRef = database.getReference("/restaurants/" + rid);

        restaurantListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Restaurant restaurant = dataSnapshot.getValue(Restaurant.class);
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);

                if(restaurant != null) {
                    if (restaurant.getAvgFinalScore() == -1 || restaurant.getReviewsNumber() == 0){
                        TextView tv = (TextView) findViewById(R.id.restaurateur_review_fragment_no_reviews);
                        tv.setVisibility(View.VISIBLE);
                        CardView cv = (CardView) findViewById(R.id.restaurateur_review_total_score_cardview);
                        cv.setVisibility(View.GONE);
                        RecyclerView rv = (RecyclerView) findViewById(R.id.restaurateur_reviews_recycler_view);
                        rv.setVisibility(View.GONE);
                    }
                    else {
                        TextView tv = (TextView) findViewById(R.id.restaurateur_restaurant_final_score);
                        DecimalFormat df = new DecimalFormat("0.0");
                        tv.setText(df.format(restaurant.getAvgFinalScore()));

                        tv = (TextView) findViewById(R.id.restaurateur_score_1);
                        df = new DecimalFormat("0.0");
                        tv.setText(df.format(restaurant.getAvgScores().get(getString(R.string.first_score))));

                        tv = (TextView) findViewById(R.id.restaurateur_score_2);
                        df = new DecimalFormat("0.0");
                        tv.setText(df.format(restaurant.getAvgScores().get(getString(R.string.second_score))));

                        tv = (TextView) findViewById(R.id.restaurateur_score_3);
                        df = new DecimalFormat("0.0");
                        tv.setText(df.format(restaurant.getAvgScores().get(getString(R.string.third_score))));

                        tv = (TextView) findViewById(R.id.restaurateur_reviews_number);
                        tv.setText(String.format(getResources().getString(R.string.reviewsFormat), restaurant.getReviewsNumber()));
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        restaurantRef.addValueEventListener(restaurantListener);

        reviewsRef = database.getReference("reviews/restaurants/" + rid);
        reviewsListener = new ValueEventListener() {
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
//                    RecyclerView rv = (RecyclerView) findViewById(R.id.restaurateur_reviews_recycler_view);
//                    ReviewsRecyclerAdapter adapter = new ReviewsRecyclerAdapter(MyReviewsRestaurantActivity.this,
//                            new ArrayList<>(data.values()));
//                    rv.setAdapter(adapter);
//                    rv.setItemAnimator(new DefaultItemAnimator());
                    setupReviewsRecyclerView(new ArrayList<>(data.values()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        reviewsRef.addValueEventListener(reviewsListener);

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
        /*******************************************************/

        // Fix Portrait Mode
        if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        navigationView.getMenu().findItem(R.id.my_reviews_restaurant).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.my_reviews_restaurant, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    private void setupReviewsRecyclerView(List<Review> reviews)
    {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.restaurateur_reviews_recycler_view);
        RecyclerView.Adapter reviewsAdapter = new ReviewsRecyclerAdapter(this, reviews);
        if(recyclerView != null){
            recyclerView.setAdapter(reviewsAdapter);
            // set Layout Manager
            if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            {
                // 10 inches
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    // 2 columns
                    GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 2);
                    recyclerView.setLayoutManager(mGridLayoutManager);
                }else
                {
                    // 3 columns
                    GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 3);
                    recyclerView.setLayoutManager(mGridLayoutManager);
                }

            } else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
            {
                // 7 inches
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                {
                    // 2 columns
                    GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 2);
                    recyclerView.setLayoutManager(mGridLayoutManager);

                }else
                {
                    // 1 column
                    LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
                    mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(mLinearLayoutManagerVertical);
                }
            }else {
                // small and normal screen
                // 1 columns
                LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
                mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(mLinearLayoutManagerVertical);
            }


            // set Animator
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
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
                    if(DailyMenuActivity.DailyMenuActivity != null)
                        DailyMenuActivity.DailyMenuActivity.finish();

                    // Start DailyMenuActivity activity
                    Intent invokeDailyMenu = new Intent(this, DailyMenuActivity.class);
                    startActivity(invokeDailyMenu);
                    break;
                }

            case R.id.my_reviews_restaurant:
                if(!getClass().equals(MyReviewsRestaurantActivity.class))
                {
                    // finish the MyReviewsRestaurantActivity if is not finished
                    if(MyReviewsRestaurantActivity != null)
                        MyReviewsRestaurantActivity.finish();

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
}
