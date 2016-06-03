package it.polito.mad.insane.lab4.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
import it.polito.mad.insane.lab4.data.Dish;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class MakeReservationActivity extends AppCompatActivity {

    private static Calendar reservationDate = null;
    private static RestaurateurJsonManager manager = null;
    private static String restaurantId;
    private static String additionalNotes = "";
    private static double totalPrice = 0;
    private static int[] quantities;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_reservation_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        restaurantId = bundle.getString("ID");
        HashMap<Dish, Integer> selectedQuantities = (HashMap<Dish, Integer>) bundle.getSerializable("selectedQuantities");

        final List<Dish> dishesToDisplay = new ArrayList<>(selectedQuantities.keySet());

        totalPrice = 0;
        for(Dish d:dishesToDisplay)
            totalPrice += d.getPrice() * selectedQuantities.get(d);

        TextView tv = (TextView) findViewById(R.id.reservation_total_price);
        DecimalFormat df = new DecimalFormat("0.00");
        if (tv != null) {
            tv.setText(MessageFormat.format("{0}€", String.valueOf(df.format(totalPrice))));
        }

        DishArrayAdapter adapter = new DishArrayAdapter(this, R.layout.dish_listview_item, new ArrayList<>(selectedQuantities.keySet()),
                selectedQuantities, 0);

        ListView mylist = (ListView) findViewById(R.id.reservation_dish_list);
        if (mylist != null) {
            mylist.setAdapter(adapter);
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

    private void saveReservation(List<Dish> dishesToDisplay, List<Integer> quantitiesToDisplay) {
        Booking b = new Booking();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        b.setDateTime(sdf.format(reservationDate.getTime()));
        b.setDishes(dishesToDisplay);
        b.setQuantities(quantitiesToDisplay);

        //TODO implementare meccanismo id prenotazioni
        //FIXME dare un id sensato
//        b.setID(String.valueOf(manager.getNextReservationID()));
        b.setID("bookRenato");
        b.setRestaurantID(restaurantId);
        b.setTotalPrice(totalPrice);
        EditText et = (EditText) findViewById(R.id.reservation_additional_notes);
        if(et != null){
            additionalNotes = et.getText().toString();
            b.setNote(additionalNotes);
        }

//        for(int i = 0; i < manager.getRestaurant(restaurantId).getDishes().size(); i++){
//            int quantity = manager.getRestaurant(restaurantId).getDishes().get(i).getAvailability_qty();
////            int newQuantity = quantity - quantities[i];
//            manager.getRestaurant(restaurantId).getDishes().get(i).setAvailability_qty(quantity - quantities[i]);
//            manager.saveDbApp();
//        }
//
//        manager.getBookings().add(b);
//        manager.saveDbApp();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/bookings");

        myRef.setValue(b, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Toast.makeText(MakeReservationActivity.this, "Prenotazione effettuata", Toast.LENGTH_LONG).show();
            }
        });

        finish(); // finish() the current activity
        Intent intent = new Intent(MakeReservationActivity.this, MyReservationsUserActivity.class);
        startActivity(intent); // start the new activity

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
