package it.polito.mad.insane.lab4.activities;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class MyReservationsUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        // finish the RestaurantProfile activity if is not finished
        if(RestaurantProfile.RestaurantProfileActivity != null)
            RestaurantProfile.RestaurantProfileActivity.finish();
        */
        setContentView(R.layout.my_reservations_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final RecyclerView rv = (RecyclerView) findViewById(R.id.reservation_recycler_view);
        if(rv != null)
        {
            RestaurateurJsonManager manager = RestaurateurJsonManager.getInstance(this);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("/bookings/users/-KJQAkQrf8Ucme-NflCc");

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String,Booking> bookings = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Booking>>() {
                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }
                    });
                    setUpView(new ArrayList<Booking>(bookings.values()),rv);
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
