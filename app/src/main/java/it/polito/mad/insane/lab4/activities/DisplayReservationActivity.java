package it.polito.mad.insane.lab4.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.DailyOfferArrayAdapter;
import it.polito.mad.insane.lab4.adapters.DishArrayAdapter;
import it.polito.mad.insane.lab4.data.Booking;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.data.Dish;
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;


public class DisplayReservationActivity extends AppCompatActivity {

    private Booking currentBooking;
    private Button date;
    private Button time;
    private TextView totalPrice;
    private TextView note;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_reservation_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.currentBooking = (Booking) getIntent().getSerializableExtra("Booking");

        context = DisplayReservationActivity.this;
        RestaurateurJsonManager manager=RestaurateurJsonManager.getInstance(this);
        final Restaurant rest=manager.getRestaurant(currentBooking.getRestaurantId());

        setTitle(currentBooking.getRestaurantName());

        ImageView mapsLink=(ImageView) findViewById(R.id.toolbar_img);
        mapsLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creates an Intent that will load navigator for that restaurant
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+rest.location.getLatitude()+","+rest.location.getLongitude());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        date = (Button) findViewById(R.id.reservation_date);

        time = (Button) findViewById(R.id.reservation_hour);

        totalPrice = (TextView) findViewById(R.id.reservation_total_price);

        note = (TextView) findViewById(R.id.reservation_additional_notes);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            calendar.setTime(sdf.parse(currentBooking.getDateTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        time.setText(MessageFormat.format("{0}:{1}", pad(calendar.get(Calendar.HOUR_OF_DAY)),
                pad(calendar.get(Calendar.MINUTE))));
        date.setText(MessageFormat.format("{0}/{1}/{2}", pad(calendar.get(Calendar.DAY_OF_MONTH)),
                pad(calendar.get(Calendar.MONTH) + 1), pad(calendar.get(Calendar.YEAR))));


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference dishesRef = database.getReference("/restaurants/" + currentBooking.getRestaurantId());

        dishesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Restaurant restaurant = dataSnapshot.getValue(Restaurant.class);
                if(restaurant != null){

                    HashMap<DailyOffer, Integer> filteredOffersMap = new HashMap<DailyOffer, Integer>();
                    for (DailyOffer d : restaurant.getDailyOfferMap().values())
                        if (currentBooking.getDailyOffersIdMap() != null)
                            if(currentBooking.getDailyOffersIdMap().containsKey(d.getID()))
                                filteredOffersMap.put(d, currentBooking.getDailyOffersIdMap().get(d.getID()));

                    DailyOfferArrayAdapter offersAdapter = new DailyOfferArrayAdapter(context, R.layout.daily_offer_listview_item,
                            filteredOffersMap, 2);
                    ListView myList = (ListView) findViewById(R.id.display_reservation_offers_list);
                    if (myList != null) {
                        myList.setAdapter(offersAdapter);
                    }

                    HashMap<Dish, Integer> filteredDishesMap = new HashMap<Dish, Integer>();
                    for(Dish d : restaurant.getDishMap().values())
                        if (currentBooking.getDishesIdMap() != null)
                            if(currentBooking.getDishesIdMap().containsKey(d.getID()))
                                filteredDishesMap.put(d, currentBooking.getDishesIdMap().get(d.getID()));

                    DishArrayAdapter dishesAdapter = new DishArrayAdapter(context, R.layout.dish_listview_item, filteredDishesMap, 2, true);
                    myList = (ListView) findViewById(R.id.display_reservation_dishes_list);
                    if (myList != null) {
                        myList.setAdapter(dishesAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DecimalFormat df = new DecimalFormat("0.00");
        totalPrice.setText(MessageFormat.format("{0}€", String.valueOf(df.format(currentBooking.getTotalPrice()))));

        if(currentBooking.getNotes() != null){
            note.setText(currentBooking.getNotes());
        }
        // Fix Portrait Mode
        if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }



}
