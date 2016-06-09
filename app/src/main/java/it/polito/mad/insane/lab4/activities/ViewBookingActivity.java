package it.polito.mad.insane.lab4.activities;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.data.Booking;

public class ViewBookingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_booking_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO - per Federico/Michele - implementare evasione(cancellazione) booking (Renato)
                }
            });
        }

        setTitle(getString(R.string.reservation_details));

        Booking currentBooking = (Booking) getIntent().getSerializableExtra("Booking");

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
            tv.setText(splitDate[0]);
        tv = (TextView) findViewById(R.id.view_booking_date);
        if(tv != null)
            tv.setText(splitDate[1]);

        //TODO riempire la ListView


        // Fix Portrait Mode
        if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

}
