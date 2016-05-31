package it.polito.mad.insane.lab4.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
        quantities = bundle.getIntArray("selectedQuantities");
        restaurantId = bundle.getString("ID");

        manager = RestaurateurJsonManager.getInstance(this);
        List<Dish> dishes = manager.getRestaurant(restaurantId).getDishes();

        setTitle(manager.getRestaurant(restaurantId).getProfile().getRestaurantName());

        final List<Dish> dishesToDisplay = new ArrayList<>();
        final ArrayList<Integer> quantitiesToDisplay = new ArrayList<>();
        totalPrice = 0;
        for(int i=0; i < dishes.size(); i++){
            if(quantities[i] != 0){
                dishesToDisplay.add(dishes.get(i));
                quantitiesToDisplay.add(quantities[i]);
                totalPrice += dishes.get(i).getPrice() * quantities[i];
            }
        }
        TextView tv = (TextView) findViewById(R.id.reservation_total_price);
        DecimalFormat df = new DecimalFormat("0.00");
        if (tv != null) {
            tv.setText(MessageFormat.format("{0}€", String.valueOf(df.format(totalPrice))));
        }

//        tv = (TextView) findViewById(R.id.reservation_restaurant_name);
//        if (tv != null) {
//            tv.setText(manager.getRestaurant(restaurantId).getProfile().getRestaurantName());
//        }

        DishArrayAdapter adapter = new DishArrayAdapter(this, R.layout.dish_cardview, dishesToDisplay, quantitiesToDisplay,0);

        ListView mylist = (ListView) findViewById(R.id.reservation_dish_list);
        if (mylist != null) {
            mylist.setAdapter(adapter);
        }

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
                    if(manager.reservationRespectsTimeContraints(reservationDate,restaurantId)==false){
                        Toast.makeText(MakeReservationActivity.this, getString(R.string.respect_time_contraints), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(MakeReservationActivity.this);
                    builder.setTitle(MakeReservationActivity.this.getResources().getString(R.string.alert_title))
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    saveReservation(dishesToDisplay,quantitiesToDisplay);
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

        //T-ODO far fare al manager la creazione della booking del db, così ce lo troviamo pronto per l'online
        Booking b = new Booking();
        b.setDate_time(reservationDate);
        b.setDishes(dishesToDisplay);
        b.setQuantities(quantitiesToDisplay);

        b.setID(String.valueOf(manager.getNextReservationID()));
        b.setRestaurantID(restaurantId);
        b.setTotalPrice(totalPrice);
        EditText et = (EditText) findViewById(R.id.reservation_additional_notes);
        if(et != null){
            additionalNotes = et.getText().toString();
            b.setNote(additionalNotes);
        }

        // decrease the available quantity for the reserved dishes
        for(int i = 0; i < manager.getRestaurant(restaurantId).getDishes().size(); i++){
            int quantity = manager.getRestaurant(restaurantId).getDishes().get(i).getAvailability_qty();
//            int newQuantity = quantity - quantities[i];
            manager.getRestaurant(restaurantId).getDishes().get(i).setAvailability_qty(quantity - quantities[i]);
            manager.saveDbApp();
        }

        manager.getBookings().add(b);
        manager.saveDbApp();

        finish(); // finish() the current activity
        Intent intent = new Intent(MakeReservationActivity.this, MyReservationsActivity.class);
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
