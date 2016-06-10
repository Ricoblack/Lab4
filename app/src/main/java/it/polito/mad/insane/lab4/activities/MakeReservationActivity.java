package it.polito.mad.insane.lab4.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.DishArrayAdapter;
import it.polito.mad.insane.lab4.data.Booking;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.data.Dish;
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class MakeReservationActivity extends AppCompatActivity {

    static final String PREF_LOGIN = "loginPref";
    private static Calendar reservationDate = null;
    private static RestaurateurJsonManager manager = null;
    private static String restaurantId;
    private static String restaurantName;
    private static String additionalNotes = "";
    private static double totalPrice = 0;
    private static double totalDiscount;
    private static int totalDishesQty;

    // TODO X FEDE: mettere la listview dei piatti fissa e la pagina scrollabile in modo che scrolli l'activity e non la listview (Michele)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_reservation_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        restaurantId = bundle.getString("ID");
        restaurantName = bundle.getString("restName");
        final HashMap<Dish, Integer> selectedQuantities = (HashMap<Dish, Integer>) bundle.getSerializable("selectedQuantities");

        if (selectedQuantities != null) {
            totalDishesQty = selectedQuantities.size();
            List<Dish> dishesToDisplay = new ArrayList<>(selectedQuantities.keySet());
            totalPrice = 0;
            for(Dish d:dishesToDisplay)
                totalPrice += d.getPrice() * selectedQuantities.get(d);
        }

        // get Daily offers from firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference offersRefs = database.getReference("/restaurants/"+restaurantId+"/dailyOfferMap");
        offersRefs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String,DailyOffer> dailyOfferHashMap = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, DailyOffer>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });

                if(dailyOfferHashMap!=null)
                {
                    totalDiscount = 0;

                    // check if there are some daily offers in  the reservation
                    ArrayList<DailyOffer> dailyOffers = new ArrayList<DailyOffer>(dailyOfferHashMap.values());

                    // sort offers based on decreasing discount
                    Collections.sort(dailyOffers, new Comparator<DailyOffer>() {
                        @Override
                        public int compare(DailyOffer lhs, DailyOffer rhs) {
                            return Double.compare(rhs.getDiscount(), lhs.getDiscount());
                        }
                    });
                    Log.d("prima:",String.valueOf(dailyOffers.get(0).getDiscount()));
                    Log.d("seconda:",String.valueOf(dailyOffers.get(1).getDiscount()));

                    // take a copy of the dishes in the reservation
                    ArrayList<DailyOffer> applyedOffers = new ArrayList<DailyOffer>();
                    HashMap<Dish, Integer> copyDishesMap = new HashMap<Dish, Integer>(selectedQuantities);
                    ArrayList<Dish> dishesReservationTemp = new ArrayList<Dish>(copyDishesMap.keySet());
                    for (DailyOffer tempOffer : dailyOffers)
                    {
                        // for each daily offer
                        ArrayList<String> idDishesOffer = new ArrayList<String>(tempOffer.getDishesIdMap().keySet());
//                        ArrayList<Dish> dishesReservation = new ArrayList<Dish>(selectedQuantities.keySet());
                        int numberDishes = tempOffer.getDishesIdMap().size();
                        int repeater = Integer.MAX_VALUE;
                        // for each dish in the offer, check if there is in the reservation
                        for (String tempDishID : idDishesOffer)
                        {
                            // check in the list of reserved dishes if there is tempDishID
//                            for (Dish dishReservation : dishesReservation)
                            for(Iterator<Dish> it = dishesReservationTemp.iterator(); it.hasNext();)
                            {
                                Dish dish = it.next();
//                                if (dishReservation.getID().equals(tempDishID))
                                if(dish.getID().equals(tempDishID))
                                {
                                    // there is the dish in the offer. Check the quantity
                                    int result = copyDishesMap.get(dish) / tempOffer.getDishesIdMap().get(tempDishID);
//                                    int result = selectedQuantities.get(dishReservation) / tempOffer.getDishesIdMap().get(tempDishID);
                                    if (result > 0)
                                    {
                                        numberDishes--;
                                        if(result < repeater)
                                            repeater = result;
                                    }

                                    break;
                                }
                            }
                        }
                        if (numberDishes == 0)
                        {
                            // the offer is matched
                            totalDiscount += repeater * tempOffer.getDiscount();
                            // remove the dishes interested in this offer from the copyMap
                            for(Iterator<Dish> it = dishesReservationTemp.iterator(); it.hasNext();)
                            {
                                Dish d = it.next();
                                if(tempOffer.getDishesIdMap().containsKey(d.getID())) {
                                    int currentQty = copyDishesMap.get(d);
                                    currentQty -= repeater * tempOffer.getDishesIdMap().get(d.getID());
                                    copyDishesMap.put(d, currentQty);
                                    if(!applyedOffers.contains(tempOffer))
                                        applyedOffers.add(tempOffer); // TODO: in applyedOffers c'è la lista delle offerte che sono state applicate per calcolare lo sconto, si possono mostrare nell'activity volendo (Michele)

                                }
                            }
                        }
                    }

                    for(DailyOffer d : applyedOffers)
                        Log.d("offerta applicata:", d.getName()+" - "+d.getDescription());

                    if(totalDiscount > 0)
                    {
                        LinearLayout ll = (LinearLayout) findViewById(R.id.discount_layout);
                        if (ll != null) {
                            ll.setVisibility(View.VISIBLE);
                        }

                        TextView tv = (TextView) findViewById(R.id.discount_value);
                        if (tv != null) {
                            DecimalFormat df = new DecimalFormat("0.00");
                            tv.setText(MessageFormat.format("{0}€", df.format(totalDiscount)));
                        }

                    }

                }

                // operazioni che devono essere performate anche in caso non ci siano dailyOffer
                TextView tv = (TextView) findViewById(R.id.reservation_total_price);
                DecimalFormat df = new DecimalFormat("0.00");
                if (tv != null) {
                    tv.setText(MessageFormat.format("{0}€", String.valueOf(df.format(totalPrice))));
                }

                DishArrayAdapter adapter = new DishArrayAdapter(MakeReservationActivity.this, R.layout.dish_listview_item, selectedQuantities, 0);

                ListView mylist = (ListView) findViewById(R.id.reservation_dish_list);
                if (mylist != null) {
                    mylist.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Button hour= (Button) findViewById(R.id.reservation_hour);
                    Button date=(Button) findViewById(R.id.reservation_date);

                    if(hour.getText().toString().toLowerCase().equals("select") || date.getText().toString().toLowerCase().equals("select")){
                        Toast.makeText(MakeReservationActivity.this, getString(R.string.specify_date_time), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(reservationDate == null){
                        Toast.makeText(MakeReservationActivity.this, getString(R.string.specify_date_time), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(!manager.reservationRespectsTimeContraints(reservationDate, restaurantId)){
                        Toast.makeText(MakeReservationActivity.this, getString(R.string.respect_time_contraints), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(MakeReservationActivity.this);
                    builder.setTitle(MakeReservationActivity.this.getResources().getString(R.string.alert_title_booking))
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    saveReservation(selectedQuantities);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });

                    Dialog dialog = builder.create();
                    dialog.show();
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

        manager = RestaurateurJsonManager.getInstance(this);
    }

    @Override
    public void finish()
    {
        super.finish();
        clearStaticVariables();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void clearStaticVariables()
    {
        MakeReservationActivity.reservationDate = null;
        MakeReservationActivity.additionalNotes = "";
        MakeReservationActivity.totalPrice = 0;
    }

    public void saveReservation(final HashMap<Dish, Integer> selectedQuantities) {
        final Booking b = new Booking();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        b.setDateTime(sdf.format(reservationDate.getTime()));

        HashMap<String, Integer> map = new HashMap<>();
        for(Dish d: selectedQuantities.keySet()) {
            map.put(d.getID(), selectedQuantities.get(d));
            b.setTotalPrice(b.getTotalPrice() + d.getPrice() * selectedQuantities.get(d));
            b.setTotalDishesQty(b.getTotalDishesQty() + selectedQuantities.get(d));
        }
        b.setDishesIdMap(map);

        EditText et = (EditText) findViewById(R.id.reservation_additional_notes);
        if(et != null){
            additionalNotes = et.getText().toString();
            b.setNotes(additionalNotes);
        }
        b.setRestaurantId(restaurantId);
        b.setRestaurantName(restaurantName);
        b.setTotalDiscount(totalDiscount);
        b.setTotalDishesQty(totalDishesQty);
        b.setTotalPrice(totalPrice);

        SharedPreferences mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        if (mPrefs != null) {
            b.setUserId(mPrefs.getString("uid", null));
            b.setUserName(mPrefs.getString("uName",null));
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference restaurantRef = database.getReference("/restaurants/" + restaurantId);
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Restaurant restaurant = dataSnapshot.getValue(Restaurant.class);

                final DatabaseReference addBookingRef = database.getReference("/bookings");
                DatabaseReference restaurantRef = addBookingRef.child("restaurants").child(restaurantId);
                DatabaseReference pushRef = restaurantRef.push();
                String key = pushRef.getKey();
                b.setID(key);
                pushRef.setValue(b);

                DatabaseReference userRef = addBookingRef.child("users").child(b.getUserId()).child(key);
                userRef.setValue(b);

                HashMap<String, Dish> updateMap = (HashMap<String, Dish>) restaurant.getDishMap();
                for (Map.Entry<Dish, Integer> selectedDishEntry : selectedQuantities.entrySet()) {
                    for (Map.Entry<String, Dish> menuDishEntry : updateMap.entrySet()) {
                        if (selectedDishEntry.getKey().getID().equals(menuDishEntry.getKey())){
                            int updateQuantity = menuDishEntry.getValue().getAvailabilityQty() - selectedDishEntry.getValue();
                            menuDishEntry.getValue().setAvailabilityQty(updateQuantity);
                            updateMap.put(menuDishEntry.getKey(), menuDishEntry.getValue());
                        }
                    }
                }

                DatabaseReference dishMapRef = database.getReference("/restaurants/" + restaurantId + "/dishMap");
                dishMapRef.setValue(updateMap);

                Toast.makeText(getApplicationContext(), "Reservation done successfully",
                        Toast.LENGTH_SHORT).show();
                clearStaticVariables();
                finish(); // finish() the current activity
                Intent intent = new Intent(MakeReservationActivity.this, MyReservationsUserActivity.class);
                startActivity(intent); // start the new activity
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(reservationDate != null) {
            Button button = (Button) findViewById(R.id.reservation_hour);
            if (button != null) {
                button.setText(new StringBuilder().append(pad(reservationDate.get(Calendar.HOUR_OF_DAY)))
                        .append(":").append(pad(reservationDate.get(Calendar.MINUTE))));
            }

            button = (Button) findViewById(R.id.reservation_date);
            if (button != null) {
                button.setText(new StringBuilder().append(pad(reservationDate.get(Calendar.DAY_OF_MONTH))).
                        append("/").append(pad(reservationDate.get(Calendar.MONTH) + 1))
                        .append("/").append(reservationDate.get(Calendar.YEAR)));
            }
        }
        if(!additionalNotes.equals("")) {
            EditText et = (EditText) findViewById(R.id.reservation_additional_notes);
            if (et != null) {
                et.setText(additionalNotes);
            }
        }

    }

    public void showTimePickerDialog(View view) {

        DialogFragment timeFragment = new TimePickerFragment();
        timeFragment.show(getSupportFragmentManager(), "reservationTimePicker");
    }

    public void showDatePickerDialog(View view) {
        DialogFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getSupportFragmentManager(), "reservationDatePicker");
    }

    public void setHour(int hourOfDay, int minute){

        if(reservationDate == null)
            reservationDate = Calendar.getInstance();
        reservationDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        reservationDate.set(Calendar.MINUTE, minute);
        Button button = (Button) findViewById(R.id.reservation_hour);
        if (button != null) {
            button.setText(new StringBuilder().append(pad(hourOfDay))
                    .append(":").append(pad(minute)));
        }

    }

    private void setDate(int year, int month, int day) {
        if (reservationDate == null)
            reservationDate = Calendar.getInstance();
        reservationDate.set(Calendar.YEAR, year);
        reservationDate.set(Calendar.MONTH, month);
        reservationDate.set(Calendar.DAY_OF_MONTH, day);
        Button button = (Button) findViewById(R.id.reservation_date);
        if (button != null) {
            button.setText(new StringBuilder().append(pad(day)).append("/").append(pad(month + 1)).append("/").append(year));
        }
    }

    private String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity())){
            };
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            ((MakeReservationActivity) getActivity()).setHour(hourOfDay, minute);
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            ((MakeReservationActivity)getActivity()).setDate(year,month,day);
        }
    }
}
