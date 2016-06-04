package it.polito.mad.insane.lab4.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.MessageFormat;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.data.Booking;


public class DisplayReservation extends AppCompatActivity {

    private Booking currentBooking;
    private Button date;
    private Button time;
    private TextView totalPrice;
    private TextView note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_reservation_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.currentBooking = (Booking) getIntent().getSerializableExtra("Booking");
        setTitle(getIntent().getStringExtra("nameRestaurant"));

        date = (Button) findViewById(R.id.reservation_date);
        time = (Button) findViewById(R.id.reservation_hour);
        totalPrice = (TextView) findViewById(R.id.reservation_total_price);
        note = (TextView) findViewById(R.id.reservation_additional_notes);

        /*
        Calendar calendar = currentBooking.getDate_time();
        time.setText(MessageFormat.format("{0}:{1}", pad(calendar.get(Calendar.HOUR_OF_DAY)),
                pad(calendar.get(Calendar.MINUTE))));
        date.setText(MessageFormat.format("{0}/{1}/{2}", pad(calendar.get(Calendar.DAY_OF_MONTH)),
                pad(calendar.get(Calendar.MONTH) + 1), pad(calendar.get(Calendar.YEAR))));
        */

        //FIXME scommentare e gestire questa riga ora che l'adapter e' cambiato (Renato)
//        DishArrayAdapter adapter = new DishArrayAdapter(this, R.layout.dish_listview_item, currentBooking.getDishes(), (ArrayList<Integer>)currentBooking.getQuantities(), 2);
//
//        ListView mylist = (ListView) findViewById(R.id.reservation_dish_list);
//        if (mylist != null) {
//            mylist.setAdapter(adapter);
//        }

        DecimalFormat df = new DecimalFormat("0.00");
        totalPrice.setText(MessageFormat.format("{0}€", String.valueOf(df.format(currentBooking.getTotalPrice()))));

        if(currentBooking.getNotes() != null){
            note.setText(currentBooking.getNotes());
        }
    }

    private String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

}
