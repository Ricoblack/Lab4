package it.polito.mad.insane.lab4.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
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
import java.util.HashMap;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.DishArrayAdapter;
import it.polito.mad.insane.lab4.data.Booking;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.data.Dish;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class MakeReservationActivity extends AppCompatActivity {

    private static Calendar reservationDate = null;
    private static RestaurateurJsonManager manager = null;
    private static String restaurantId;
    private static String additionalNotes = "";
    private static double totalPrice = 0;
    private static double totalDiscount;
    private static int totalDishesQty;
    private static int[] quantities;


    //TODO: aggiungere il controllo che verifichi se i piatti presenti presenti nel carrello sono legati da qualche offerta (daily offer) e in tal caso applichi lo sconto (Michele)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_reservation_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        restaurantId = bundle.getString("ID");
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
                // TODO: gestire il caso di offerte sovrapposte e cambiare layout in modo da visualizzare quali offerte sono state applicate (Michele)
                // TODO: oppure implementare offerte direttamente acquistabili (Michele)
                if(dailyOfferHashMap!=null)
                {
                    totalDiscount = 0;

                    // check if there are some daily offers in  the reservation
                    ArrayList<DailyOffer> dailyOffers = new ArrayList<DailyOffer>(dailyOfferHashMap.values());
                    for (DailyOffer tempOffer : dailyOffers)
                    {
                        ArrayList<String> idDishesOffer = new ArrayList<String>(tempOffer.getDishesIdMap().keySet());
                        ArrayList<Dish> dishesReservation = new ArrayList<Dish>(selectedQuantities.keySet());
                        int numberDishes = tempOffer.getDishesIdMap().size();
                        int repeater = Integer.MAX_VALUE;
                        // for each dish in the offer, check if there is in the reservation
                        for (String tempDishID : idDishesOffer) {
                            // check in the list of reserved dishes if there is tempDishID
                            for (Dish dishReservation : dishesReservation) {
                                if (dishReservation.getID().equals(tempDishID)) {
                                    // there is the dish. Check the quantity
                                    int result = selectedQuantities.get(dishReservation) / tempOffer.getDishesIdMap().get(tempDishID);
                                    if (result > 0) {
                                        numberDishes--;
                                        if(result < repeater)
                                            repeater = result;
                                    }

                                    break;
                                }
                            }
                        }

                        if (numberDishes == 0) {
                            totalDiscount += repeater * tempOffer.getDiscount();
                        }

                    }

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
                    TextView tv = (TextView) findViewById(R.id.reservation_total_price);
                    DecimalFormat df = new DecimalFormat("0.00");
                    if (tv != null) {
                        tv.setText(MessageFormat.format("{0}€", String.valueOf(df.format(totalPrice))));
                    }

                    DishArrayAdapter adapter = new DishArrayAdapter(MakeReservationActivity.this, R.layout.dish_listview_item, selectedQuantities, 3);

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

                    //Verifico che i costraint per la prenotazione siano rispettati: in orario di lavoro e tra almeno un ora
                    //TODO implementare questo metodi sul manager, preferibilmente chi li ha fatti la scorsa volta, se non sbaglio
                    //TODO Carlo e/o Michele (Renato)
//                    if(manager.reservationRespectsTimeContraints(reservationDate,restaurantId)==false){
//                        Toast.makeText(MakeReservationActivity.this, getString(R.string.respect_time_contraints), Toast.LENGTH_SHORT).show();
//                        return;
//                    }
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

    public void saveReservation(HashMap<Dish, Integer> selectedQuantities) {
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
        b.setTotalDiscount(totalDiscount);
        b.setTotalDishesQty(totalDishesQty);
        b.setTotalPrice(totalPrice);

        //TODO inserire id dello user che dovremmo avere nelle sharedPref (Renato)
//        b.setUserId(userId);


        //TODO implementare meccanismo di decremento quantita' disponibili dei piatti
//        for(int i = 0; i < manager.getRestaurant(restaurantId).getDishes().size(); i++){
//            int quantity = manager.getRestaurant(restaurantId).getDishes().get(i).getAvailabilityQty();
////            int newQuantity = quantity - quantities[i];
//            manager.getRestaurant(restaurantId).getDishes().get(i).setAvailabilityQty(quantity - quantities[i]);
//            manager.saveDbApp();
//        }
//
//        manager.getBookings().add(b);
//        manager.saveDbApp();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference addBookingsRef = database.getReference("/bookings");

        //TODO salvare la prenotazione anche nello user
        //TODO inserire id prenotazione (Renato)
//        b.setID(String.valueOf(manager.getNextReservationID()));

        addBookingsRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                DatabaseReference restaurantRef = database.getReference("/bookings/restaurants/" + restaurantId + "/");
                DatabaseReference newRef = restaurantRef.push();
                String key = newRef.getKey();
                b.setID(key);
                newRef.setValue(b);
//
//                b.setID(generatedId);
//                restaurantRef.push(b);
//                addBookingsRef.child("restaurants").child(restaurantId).child(generatedId).setValue(b);

                //TODO inserire prenotazione nella sezione users, scommentare queste righe (Renato)
//                DatabaseReference userRef = addBookingsRef.child("users").child(userId);
//                userRef.push().setValue(b);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if(committed)
                    Toast.makeText(MakeReservationActivity.this, "Reservation done", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MakeReservationActivity.this, "Reservation failed", Toast.LENGTH_SHORT).show();
            }
        });


        //TODO scommentare queste righe e gestire la cosa
//        finish(); // finish() the current activity
//        Intent intent = new Intent(MakeReservationActivity.this, MyReservationsUserActivity.class);
//        startActivity(intent); // start the new activity

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
