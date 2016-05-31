package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.polito.mad.insane.lab4.R;

/**
 * Created by Renato on 31/05/2016.
 */
public class HomeSpinnerAdapter extends ArrayAdapter<String> {
    Context contextSpinner;
    List<String> choices = new ArrayList<>();
    Resources resSpinnerAdapter;


    public HomeSpinnerAdapter(Context context, int resource, List objects, Resources res)
    {
        super(context, resource, objects);
        contextSpinner = context;
        choices = (List<String>)objects;
        this.resSpinnerAdapter = res;
    }

    @Override
    public boolean isEnabled(int position){
        return position != 0;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int pos, View cnvtView, ViewGroup prnt) {

        if(pos == 0) {

            LayoutInflater inflater = LayoutInflater.from(this.contextSpinner);
            View mySpinner = inflater.inflate(R.layout.home_order_spinner, prnt,
                    false);

            TextView main_text = (TextView) mySpinner
                    .findViewById(R.id.text_spinner_home);
            main_text.setText(choices.get(pos));

            return mySpinner;
        }else{
            LayoutInflater inflater = LayoutInflater.from(this.contextSpinner);
            View mySpinner = inflater.inflate(R.layout.home_order_spinner, prnt,
                    false);

            TextView main_text = (TextView) mySpinner.findViewById(R.id.text_spinner_home);
            main_text.setText(choices.get(pos));

            return mySpinner;
        }
    }
    public View getCustomView(int position, View convertView,
                              ViewGroup parent) {

        int x = 60;

        if(position == 0) {

            LayoutInflater inflater = LayoutInflater.from(this.contextSpinner);
            View mySpinner = inflater.inflate(R.layout.home_order_spinner, parent,
                    false);

            TextView main_text = (TextView) mySpinner
                    .findViewById(R.id.text_spinner_home);
            main_text.setText(choices.get(position));
            main_text.setTextColor(resSpinnerAdapter.getColor(R.color.colorPrimary));
            main_text.setPadding(x,x,x,x);


            return mySpinner;
        }else{
            LayoutInflater inflater = LayoutInflater.from(this.contextSpinner);
            View mySpinner = inflater.inflate(R.layout.home_order_spinner, parent,
                    false);

            TextView main_text = (TextView) mySpinner.findViewById(R.id.text_spinner_home);
            main_text.setText(choices.get(position));
            main_text.setTextColor(resSpinnerAdapter.getColor(R.color.black));
            main_text.setPadding(x,x,x,x);


            return mySpinner;
        }
    }
}
