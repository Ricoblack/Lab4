package it.polito.mad.insane.lab4.activities;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.DailyOfferArrayAdapter;
import it.polito.mad.insane.lab4.adapters.DishArrayAdapter;
import it.polito.mad.insane.lab4.data.Booking;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.data.Dish;
import it.polito.mad.insane.lab4.data.Restaurant;

public class ViewBookingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_booking_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //TODO Federico sistemare la grafica di questa activity, e' abbastanza rozza

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        if (fab != null) {
//            fab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    //T-ODO - per Federico/Michele - implementare evasione(cancellazione) booking (Renato)
//                }
//            });
//        }


        setTitle(getString(R.string.reservation_details));

        final Booking currentBooking = (Booking) getIntent().getSerializableExtra("Booking");

        TextView tv = (TextView) findViewById(R.id.view_booking_username);
        if(tv != null){
            tv.setText(currentBooking.getUserName());
        }

        tv = (TextView) findViewById(R.id.view_booking_note_text);
        if(tv != null){
            if(currentBooking.getNotes().equals(""))
                tv.setText(getString(R.string.no_notes_in_bookings));
            else
                tv.setText(currentBooking.getNotes());
        }

        String splitDate[] = currentBooking.getDateTime().split(" ");
        tv = (TextView) findViewById(R.id.view_booking_hour);
        if(tv != null)
            tv.setText(splitDate[1]);
        tv = (TextView) findViewById(R.id.view_booking_date);
        if(tv != null)
            tv.setText(splitDate[0]);

        tv = (TextView) findViewById(R.id.view_booking_items_number);
        if(tv != null)
            tv.setText(MessageFormat.format("{0} ITEMS", String.valueOf(currentBooking.getTotalDishesQty())));
        tv = (TextView) findViewById(R.id.view_booking_price);
        if(tv != null){
            DecimalFormat df = new DecimalFormat("0.00");
            tv.setText(MessageFormat.format("{0}€", String.valueOf(df.format(currentBooking.getTotalPrice()))));
        }


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference dishesRef = database.getReference("/restaurants/" + currentBooking.getRestaurantId());

        dishesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Restaurant restaurant = dataSnapshot.getValue(Restaurant.class);
                if (restaurant != null) {

                    HashMap<DailyOffer, Integer> filteredOffersMap = new HashMap<>();
                    for (DailyOffer d : restaurant.getDailyOfferMap().values())
                        if (currentBooking.getDailyOffersIdMap() != null)
                            if (currentBooking.getDailyOffersIdMap().containsKey(d.getID()))
                                filteredOffersMap.put(d, currentBooking.getDailyOffersIdMap().get(d.getID()));

                    DailyOfferArrayAdapter offersAdapter = new DailyOfferArrayAdapter(ViewBookingActivity.this,
                            R.layout.daily_offer_listview_item, filteredOffersMap, 4);
                    ListView myList = (ListView) findViewById(R.id.view_booking_offers_list_view);
                    if (myList != null) {
                        myList.setAdapter(offersAdapter);
                    }

                    HashMap<Dish, Integer> filteredDishesMap = new HashMap<Dish, Integer>();
                    for (Dish d : restaurant.getDishMap().values())
                        if (currentBooking.getDishesIdMap() != null)
                            if (currentBooking.getDishesIdMap().containsKey(d.getID()))
                                filteredDishesMap.put(d, currentBooking.getDishesIdMap().get(d.getID()));

                    DishArrayAdapter dishesAdapter = new DishArrayAdapter(ViewBookingActivity.this, R.layout.dish_listview_item,
                            filteredDishesMap, 4, true);
                    myList = (ListView) findViewById(R.id.view_booking_dishes_list_view);
                    if (myList != null) {
                        myList.setAdapter(dishesAdapter);
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Fix Portrait Mode
        if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

}
