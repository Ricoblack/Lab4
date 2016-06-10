package it.polito.mad.insane.lab4.activities;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.MySpinnerAdapterFilter;

public class FilterActivity extends AppCompatActivity
{

    static final String PREF_NAME = "myPrefFilter";
    private SharedPreferences mPrefs = null;

    // TODO: i filtri andrebbero caricati dinamicamente in base ai valori contenuti nel DB (Michele)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //set up spinners
        setUpSpinners(true);

        //set up on click listeners of the buttons
        AppCompatButton reset= (AppCompatButton) findViewById(R.id.resetButton);
        AppCompatButton apply=(AppCompatButton) findViewById(R.id.applyButton);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetPressed(v);
            }
        });
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onApplyPressed(v);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Fix Portrait Mode
        if( (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL ||
                (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void onResetPressed(View v){
        this.setUpSpinners(false);

    }

    public void onApplyPressed(View v){
        //read values of spinners, launches home activity with extras, and finally finishes

        Spinner dSpinner = (Spinner) findViewById(R.id.distance_filter);
        Spinner pSpinner = (Spinner) findViewById(R.id.price_filter);
        Spinner tSpinner = (Spinner) findViewById(R.id.type_restaurant_filter);
        Spinner tiSpinner = (Spinner) findViewById(R.id.lunch_time_filter);

        String distanceValue="";
        String priceValue="";
        String typeValue="";
        String timeValue="";

        if(dSpinner.getSelectedItemPosition()!=0) distanceValue=dSpinner.getSelectedItem().toString();
        if(pSpinner.getSelectedItemPosition()!=0) priceValue=pSpinner.getSelectedItem().toString();
        if(tSpinner.getSelectedItemPosition()!=0) typeValue=tSpinner.getSelectedItem().toString();
        if(tiSpinner.getSelectedItemPosition()!=0) timeValue=tiSpinner.getSelectedItem().toString();

        this.mPrefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = this.mPrefs.edit();
        editor.putString("distanceValue",distanceValue);
        editor.putString("priceValue",priceValue);
        editor.putString("typeValue",typeValue);
        editor.putString("timeValue",timeValue);
        editor.putInt("distancePos", dSpinner.getSelectedItemPosition());
        editor.putInt("pricePos",pSpinner.getSelectedItemPosition());
        editor.putInt("typePos",tSpinner.getSelectedItemPosition());
        editor.putInt("timePos",tiSpinner.getSelectedItemPosition());
        editor.putString("haveToFilter","yes");
        editor.apply();

        /*Intent i = new Intent(this,HomeConsumer.class);
        i.putExtra("distanceValue",distanceValue);
        i.putExtra("priceValue",priceValue);
        i.putExtra("typeValue",typeValue);
        i.putExtra("timeValue",timeValue);
        startActivity(i);*/

        finish();

    }

    private void setUpSpinners(boolean loadOldValue) {

        final Spinner dSpinner = (Spinner) findViewById(R.id.distance_filter);
        List<String> distances = new ArrayList<>();
        Resources res = getResources();
        String[] dStrings = res.getStringArray(R.array.distance_array);
        Collections.addAll(distances, dStrings);
        MySpinnerAdapterFilter dAdapter = new MySpinnerAdapterFilter(FilterActivity.this, R.layout.support_simple_spinner_dropdown_item,
                distances,res);
        dAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        dSpinner.setAdapter(dAdapter);


        final Spinner pSpinner = (Spinner) findViewById(R.id.price_filter);
        List<String> prices = new ArrayList<>();
        Resources res2 = getResources();
        String[] pStrings = res2.getStringArray(R.array.price_array);
        Collections.addAll(prices, pStrings);
        MySpinnerAdapterFilter pAdapter = new MySpinnerAdapterFilter(FilterActivity.this, R.layout.support_simple_spinner_dropdown_item,
                prices,res2);
        pAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        pSpinner.setAdapter(pAdapter);

        final Spinner tSpinner = (Spinner) findViewById(R.id.type_restaurant_filter);
        List<String> types = new ArrayList<>();
        Resources res3 = getResources();
        String[] tStrings = res3.getStringArray(R.array.type_array);
        Collections.addAll(types, tStrings);
        MySpinnerAdapterFilter tAdapter = new MySpinnerAdapterFilter(FilterActivity.this, R.layout.support_simple_spinner_dropdown_item,
                types,res3);
        tAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        tSpinner.setAdapter(tAdapter);

        final Spinner tiSpinner = (Spinner) findViewById(R.id.lunch_time_filter);
        List<String> times = new ArrayList<>();
        Resources res4 = getResources();
        String[] tiStrings = res4.getStringArray(R.array.time_array);
        Collections.addAll(times, tiStrings);
        MySpinnerAdapterFilter tiAdapter = new MySpinnerAdapterFilter(FilterActivity.this, R.layout.support_simple_spinner_dropdown_item,
                times,res4);
        tiAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        tiSpinner.setAdapter(tiAdapter);

        if(loadOldValue)
        {
            this.mPrefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
            if (mPrefs!=null){
                //set spinners with previous values
                dSpinner.setSelection(mPrefs.getInt("distancePos",0));
                tSpinner.setSelection(mPrefs.getInt("typePos",0));
                pSpinner.setSelection(mPrefs.getInt("pricePos",0));
                tiSpinner.setSelection(mPrefs.getInt("timePos",0));

            }
        }else
        {
            this.mPrefs = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
            if (mPrefs!=null) {
                SharedPreferences.Editor editor = this.mPrefs.edit();
                editor.clear();
                editor.commit();
            }

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
