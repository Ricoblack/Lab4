package it.polito.mad.insane.lab4.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import it.polito.mad.insane.lab4.R;

public class MyReviewsRestaurantActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    public static Activity MyReviewsRestaurantActivity = null;
    static final String PREF_LOGIN = "loginPref";
    private SharedPreferences mPrefs = null;
    private static String rid;
    private NavigationView navigationView;

    @Override
    public void finish()
    {
        super.finish();
        MyReviewsRestaurantActivity = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyReviewsRestaurantActivity = this;
        setContentView(R.layout.my_reviews_restaurant_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // check login
        if(rid == null)
        {
            this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
            if (mPrefs != null) {
                rid = this.mPrefs.getString("rid", null);
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
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
        /*******************************************************/
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
