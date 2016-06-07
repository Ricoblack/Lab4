package it.polito.mad.insane.lab4.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.DishesRecyclerAdapter;
import it.polito.mad.insane.lab4.data.Booking;
import it.polito.mad.insane.lab4.data.Dish;

/**
 * Created by Federico on 02/06/2016.
 */
public class DailyMenu extends AppCompatActivity {

    private DishesRecyclerAdapter adapter = null;
    private ArrayList<Dish> dishes;
    static final String PREF_LOGIN = "loginPref";
    private SharedPreferences mPrefs = null;
    private static String rid;
    //static private RestaurateurJsonManager manager = null;

    /**
     * Standard Methods
     **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //DailyMenu.manager = RestaurateurJsonManager.getInstance(this);

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
        updateDishes();

        // set Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_dish);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // open activity EditDish
                    //TODO inserire edit dish sull'onclick (Federico)
//                    Intent i = new Intent(view.getContext(),EditDish.class);
////                    i.putExtra("Adapter",DailyMenu.this.adapter);
//                    view.getContext().startActivity(i);

                }
            });
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
        if (this.adapter != null)
            this.adapter.notifyDataSetChanged();
        updateDishes();
    }

    /**
     * Our Methods
     */
    private void setupDishesRecyclerView() {
        // set Adapter
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.MenuRecyclerView);
        DailyMenu.this.adapter = new DishesRecyclerAdapter(this, dishes, rid);
        if (recyclerView != null) {
            recyclerView.setAdapter(DailyMenu.this.adapter);

            // set Layout Manager
            if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
                // 10 inches
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    // 2 columns
                    GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 2);
                    recyclerView.setLayoutManager(mGridLayoutManager);
                } else {
                    // 3 columns
                    GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 3);
                    recyclerView.setLayoutManager(mGridLayoutManager);
                }

            } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
                // 7 inches
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    // 2 columns
                    GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 2);
                    recyclerView.setLayoutManager(mGridLayoutManager);

                } else {
                    // 1 column
                    LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
                    mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(mLinearLayoutManagerVertical);
                }
            } else {
                // small and normal screen
                // 1 columns
                LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
                mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(mLinearLayoutManagerVertical);
            }


            // set Animator
            recyclerView.setItemAnimator(new DefaultItemAnimator()); // default animations
        }
    }

    public void updateDishes(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/restaurants/"+rid+"/dishMap");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String,Dish> r = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Dish>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });

                dishes = new ArrayList<>(r.values());
                setupDishesRecyclerView();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
