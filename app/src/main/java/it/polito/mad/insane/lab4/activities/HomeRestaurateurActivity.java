package it.polito.mad.insane.lab4.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.BookingsRecyclerAdapter;
import it.polito.mad.insane.lab4.data.Booking;

public class HomeRestaurateurActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BookingsRecyclerAdapter adapter;
    private static Calendar globalDate = Calendar.getInstance();
    private static int globalHour = -1;
    static final String PREF_LOGIN = "loginPref";
    private SharedPreferences mPrefs = null;
    private static String rid;
    private  ArrayList<Booking> bookings = new ArrayList<>();

    // FIXME: su smartphone cone android 4.1.2 non viene settato lo sfondo dei tasti nella home

    // TODO: sistemare le dimensioni delle immagini per adattarle ai vari tipi di schermo

    // TODO: mettere un colore/tema di background dell'app che non sia bianco come tutti gli altri colori di sfondo


    /** Standard methods **/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_restaurateur_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // check login
        if(rid == null)
        {
            this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
            if (mPrefs != null) {
                rid = this.mPrefs.getString("rid", null);
            }
        }

        // Fix Portrait Mode
        if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        /**********************DRAWER****************************/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_drawer);
        TextView title_drawer = (TextView) headerView.findViewById(R.id.title_drawer);
        if(mPrefs != null) {
            title_drawer.setText(mPrefs.getString("rUser", null));
        }
        navigationView.setNavigationItemSelectedListener(this);
        /**************************************************/

    }

    /********************DRAWER*****************************/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id)
        {
            case R.id.home_restaurateur_activity:
                if(!getClass().equals(HomeRestaurateurActivity.class))
                {
                    Intent i = new Intent(this, HomeRestaurateurActivity.class);
                    startActivity(i);
                    finish();
                }
                break;
            case R.id.action_daily_menu:
                // Start DailyMenuActivity activity
                Intent invokeDailyMenu = new Intent(HomeRestaurateurActivity.this, DailyMenuActivity.class);
                startActivity(invokeDailyMenu);
                break;

            case R.id.logout_restaurateur_drawer:
                if(rid == null){
                    Toast.makeText(HomeRestaurateurActivity.this, "Non sei loggato",Toast.LENGTH_SHORT).show();
                }else {
                    this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
                    if (mPrefs != null) {
                        rid = null;
                        SharedPreferences.Editor editor = this.mPrefs.edit();
                        editor.clear();
                        editor.apply();
                    }
                    Intent i = new Intent(this, HomePageActivity.class);
                    startActivity(i);
                    finish();
                }
                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /*************************************************/


    public void showDatePickerDialog(View v)
    {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialogHome(View view)
    {
        DialogFragment openingFragment = new TimePickerFragment();
        openingFragment.show(getSupportFragmentManager(), "homeTitleHourPicker");
    }

    @Override
    public void onBackPressed() {
        /**********************DRAWER***************************/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.home_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        /*************************************************/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_restaurateur_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id)
        {
            case R.id.action_edit_profile:
                //Start EditProfileActivity
//                Intent invokeEditProfile = new Intent(HomeRestaurateurActivity.this, EditProfileActivity.class);
//                startActivity(invokeEditProfile);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        //adapter.notifyDataSetChanged();
        TextView tv = (TextView) findViewById(R.id.home_title_day);
        if(tv != null)
            tv.setText(String.format("  %s  ", convertDateToString(globalDate.getTime())));
        updateBookingsDay(globalDate.get(Calendar.YEAR),globalDate.get(Calendar.MONTH),globalDate.get(Calendar.DAY_OF_MONTH));

        tv = (TextView) findViewById(R.id.home_title_hour);
        //TODO risolvere bug filtro orario (Federico)
        if (tv != null){
            if(globalHour == -1)
                tv.setText(R.string.all_hours);
            else {
                tv.setText(String.format("  %d:00  ", globalHour));
                updateBookingsHour(globalHour);
            }
        }
    }

    /** Our Methods **/
    // Layout Manager
    private void setUpRecyclerDay(int year, int month, int day)
    {
        RecyclerView rV = (RecyclerView) findViewById(R.id.BookingRecyclerView);

        adapter = new BookingsRecyclerAdapter(this, getBookingsOfDay(year,month,day));
        rV.setAdapter(adapter);

        // set Layout Manager
        if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)
        {
            // 10 inches
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            {
                // 2 columns
                GridLayoutManager mGridLayoutManager = new GridLayoutManager(this,2);
                rV.setLayoutManager(mGridLayoutManager);
            }else
            {
                // 1 column, different layout
                LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
                mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                rV.setLayoutManager(mLinearLayoutManagerVertical);
            }

        } else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE)
        {
            // 7 inches
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                // 1 column
                LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
                mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                rV.setLayoutManager(mLinearLayoutManagerVertical);

            }else
            {
                // 1 column
                LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
                mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
                rV.setLayoutManager(mLinearLayoutManagerVertical);
            }
        }else {

            // small and normal screen
            // 1 columns
            LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
            mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
            rV.setLayoutManager(mLinearLayoutManagerVertical);
        }

        // Set animation
        RecyclerView.ItemAnimator ia = new DefaultItemAnimator();
        rV.setItemAnimator(ia); // If you don't apply other animations it uses the default one

        fillGraphWithBookings(adapter);

    }

    private void setUpRecyclerHour (final int hour)
    {
        RecyclerView rV = (RecyclerView) findViewById(R.id.BookingRecyclerView);

        BookingsRecyclerAdapter adapter = new BookingsRecyclerAdapter(this, getBookingsOfHour(hour));
        rV.setAdapter(adapter);

        LinearLayoutManager mLinearLayoutManagerVertical = new LinearLayoutManager(this);
        mLinearLayoutManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        rV.setLayoutManager(mLinearLayoutManagerVertical);

        // Set animation
        RecyclerView.ItemAnimator ia = new DefaultItemAnimator();  // If you don't apply other animations it uses the default one
        rV.setItemAnimator(ia);
    }


    private List<Booking> getBookingsOfDay(int year,int month,int day)
    {
        ArrayList<Booking> dayBookings= new ArrayList<>();

        for(int i=0; i < bookings.size(); i++){
            Booking booking = bookings.get(i);
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(parser.parse(booking.getDateTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(cal.get(Calendar.YEAR)==year && cal.get(Calendar.MONTH)==month && cal.get(Calendar.DAY_OF_MONTH)==day){
                dayBookings.add(booking);
            }
        }
        Collections.sort(dayBookings);
        return dayBookings;
    }



    private List<Booking> getBookingsOfHour(int hour)
    {
        ArrayList<Booking> hourBookings = new ArrayList<>();
        ArrayList<Booking> dayBookings = (ArrayList<Booking>) getBookingsOfDay(globalDate.get(Calendar.YEAR),
                globalDate.get(Calendar.MONTH),globalDate.get(Calendar.DAY_OF_MONTH));

        for(Booking b : dayBookings){
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(parser.parse(b.getDateTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (cal.get(Calendar.HOUR_OF_DAY) >= hour)
                hourBookings.add(b);
        }
        return hourBookings;
    }

    private void updateBookingsHour(final int hour){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/bookings/restaurants/"+rid);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String,Booking> r=dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Booking>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });

                bookings = new ArrayList<>(r.values());
                setUpRecyclerHour(hour);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateBookingsDay(final int year, final int month, final int day){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/bookings/restaurants/"+rid);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String,Booking> r = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Booking>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });

                bookings = new ArrayList<>(r.values());
                setUpRecyclerDay(year,month,day);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private String convertDateToString(Date date)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(date);
    }

    private static String pad(int c)
    {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    public void refresh(View view) {
        globalHour = -1;
        TextView tv = (TextView) findViewById(R.id.home_title_hour);
        if(tv != null)
            tv.setText(R.string.all_hours);
        setUpRecyclerDay(globalDate.get(Calendar.YEAR), globalDate.get(Calendar.MONTH), globalDate.get(Calendar.DAY_OF_MONTH));
    }

    private void editGraph(BarGraphSeries<DataPoint> series)
    {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = HomeRestaurateurActivity.this.getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        final int colorAccent = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        final int colorPrimary = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        int colorPrimaryDark = typedValue.data;

        GraphView graph = (GraphView) findViewById(R.id.graph);

        if (graph != null) {
            series.setSpacing(20);
            series.setTitle(getResources().getString(R.string.graph_title));
            series.setColor(colorPrimary);

            series.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, final DataPointInterface dataPoint) {
                    Toast.makeText(HomeRestaurateurActivity.this, String.format("h %s:00 %s: %s", String.valueOf(pad((int) dataPoint.getX())),
                            " - "+getResources().getString(R.string.totalDishesNr), String.valueOf((int) dataPoint.getY())), Toast.LENGTH_SHORT).show();
//                    globalHour = (int) dataPoint.getX();
//                    TextView tv = (TextView) findViewById(R.id.home_title_hour);
//                    tv.setText(String.format("  %d:00  ", globalHour));
//                    setUpRecyclerHour(globalHour);

                }
            });

            int hour;
            graph.getViewport().setXAxisBoundsManual(true);
            if(globalHour != -1)
                hour = globalHour;
            else {
                Calendar cal = Calendar.getInstance();
                hour = cal.get(Calendar.HOUR_OF_DAY);
            }
            graph.getViewport().setMinX(hour);

            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                if(hour > 24 - 4){
                    graph.getViewport().setMinX(16);
                    graph.getViewport().setMaxX(24);
                }
                else if (hour < 4){
                    graph.getViewport().setMinX(0);
                    graph.getViewport().setMaxX(8);
                }
                else{
                    graph.getViewport().setMinX(hour - 4);
                    graph.getViewport().setMaxX(hour + 4);
                }
                graph.getGridLabelRenderer().setNumHorizontalLabels(9);
            }
            else {
                if(hour > 24 - 6){
                    graph.getViewport().setMinX(12);
                    graph.getViewport().setMaxX(24);
                }
                else if (hour < 6){
                    graph.getViewport().setMinX(0);
                    graph.getViewport().setMaxX(12);
                }
                else{
                    graph.getViewport().setMinX(hour - 6);
                    graph.getViewport().setMaxX(hour + 6);
                }
                graph.getGridLabelRenderer().setNumHorizontalLabels(12+1);
            }

            graph.getGridLabelRenderer().setPadding(20);
            graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
            graph.getGridLabelRenderer().setHorizontalLabelsVisible(true);
            graph.getGridLabelRenderer().setHorizontalAxisTitle(getString(R.string.horizontal_axis_label));
            graph.getGridLabelRenderer().setHorizontalAxisTitleColor(colorPrimary);

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(0);
            nf.setMaximumIntegerDigits(2);
            graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));
//        graph.getViewport().setScrollable(true);
//        graph.getGridLabelRenderer().setHorizontalLabelsColor(colorPrimary);
//        graph.getGridLabelRenderer().setVerticalLabelsColor(colorPrimary);
//        graph.getGridLabelRenderer().setVerticalAxisTitle("Bookings");
//        graph.getGridLabelRenderer().setHorizontalAxisTitle("Hours");

            graph.getGridLabelRenderer().setGridColor(0xFFFAFAFA); //background theme color
            graph.getGridLabelRenderer().reloadStyles();
        }

    }


    private void fillGraphWithBookings(BookingsRecyclerAdapter adapter)
    {

        //creo un vettore in cui gli indici corrispondono alle ore del giorno e i valori al numero di prenotazioni in quell'ora
        int hours[] = new int[24];

        //inizializzo il vettore con tutti zeri
        for(int i=0; i<24; i++)
            hours[i] = 0;

        //List<Booking> bookings = null;

        // aggiungo all'i-esimo posto (che corrisponde all'i-esima ora) il numero di piatti prenotati
        for(Booking b : bookings) {
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(parser.parse(b.getDateTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            hours[cal.get(Calendar.HOUR_OF_DAY)] += b.getDishesIdMap().size();
        }

        //creo un vettore di DataPoint per riempire il grafico. quest'oggetto contiene un item per ogni ora del giorno
        DataPoint[] graphBookings= new DataPoint[24];
        for(int i=0; i<hours.length; i++){
            //creo un DataPoint che ha per ascissa l'ora e per ordinata il numero di prenotazioni per ogni ora
            graphBookings[i] = new DataPoint(i, hours[i]);
        }
        //creo l'oggetto BarGraphSeries per avere un istogramma
        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(graphBookings);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        if (graph != null) {
            //aggiungo la serie al grafico in modo da visualizzarla
            graph.getSeries().clear();
            graph.addSeries(series);
        }

        editGraph(series); //modifica l'aspetto visivo del grafico
    }

    public void setDate(int year, int month, int day)
    {
        // set graph time interval
        TextView tv = (TextView) findViewById(R.id.home_title_day);
        if (tv != null) {
            tv.setText(new StringBuilder().append(pad(day)).append("/").append(pad(month + 1)).append("/").append(year));
        }
        globalDate.set(Calendar.YEAR, year);
        globalDate.set(Calendar.MONTH, month);
        globalDate.set(Calendar.DAY_OF_MONTH, day);
        globalHour = -1;
        tv = (TextView) findViewById(R.id.home_title_hour);
        if(tv != null){
            tv.setText(R.string.all_hours);
        }
        setUpRecyclerDay(globalDate.get(Calendar.YEAR),globalDate.get(Calendar.MONTH),globalDate.get(Calendar.DAY_OF_MONTH));
//        setUpRecyclerDay(year, month, day);
        //set up again recycle view
//        setUpRecyclerDay(year,month,day);
    }

    public void setTime(int hourOfDay)
    {
        globalHour = hourOfDay;
        TextView tv = (TextView) findViewById(R.id.home_title_hour);
        if (tv != null) {
            tv.setText(new StringBuilder().append(pad(hourOfDay)).append(":").append("00"));
        }
        setUpRecyclerDay(globalDate.get(Calendar.YEAR),globalDate.get(Calendar.MONTH),globalDate.get(Calendar.DAY_OF_MONTH));
        setUpRecyclerHour(hourOfDay);

    }



    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

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
            // Do something with the date chosen by the user
            ((HomeRestaurateurActivity)getActivity()).setDate(year,month,day);
        }
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity())){
            };
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            String picker = this.getTag();
            switch (picker){
//                case "openingPicker":
//                    Button button = (Button) getActivity().findViewById(R.id.openingHour);
//                    button.setText(new StringBuilder().append(pad(hourOfDay))
//                            .append(":").append(pad(minute)));
//                    break;
//                case "closingPicker":
//                    button = (Button) getActivity().findViewById(R.id.closingHour);
//                    button.setText(new StringBuilder().append(pad(hourOfDay))
//                            .append(":").append(pad(minute)));
//                    break;
                case "homeTitleHourPicker":
                    ((HomeRestaurateurActivity) getActivity()).setTime(hourOfDay);
                default:
                    break;
            }
        }

        private static String pad(int c) {
            if (c >= 10)
                return String.valueOf(c);
            else
                return "0" + String.valueOf(c);
        }
    }
}
