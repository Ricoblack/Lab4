package it.polito.mad.insane.lab4.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
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
import it.polito.mad.insane.lab4.adapters.DishArrayAdapter;
import it.polito.mad.insane.lab4.data.Booking;
import it.polito.mad.insane.lab4.data.Dish;


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

        setTitle(currentBooking.getRestaurantName());

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
        final DatabaseReference dishesRef = database.getReference("/restaurants/" + currentBooking.getRestaurantId() + "/dishMap" );

        dishesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Dish> dishesMap = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Dish>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });
                if(dishesMap != null){
                    HashMap<Dish, Integer> filteredDishesMap = new HashMap<Dish, Integer>();
                    for(Dish d : dishesMap.values()){
                        if(currentBooking.getDishesIdMap().containsKey(d.getID()))
                            filteredDishesMap.put(d, currentBooking.getDishesIdMap().get(d.getID()));
                    }

                    DishArrayAdapter adapter = new DishArrayAdapter(context, R.layout.dish_listview_item, filteredDishesMap, 2);

                    ListView mylist = (ListView) findViewById(R.id.reservation_dish_list);
                    if (mylist != null) {
                        mylist.setAdapter(adapter);
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
