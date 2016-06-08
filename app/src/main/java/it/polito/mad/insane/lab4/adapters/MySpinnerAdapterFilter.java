package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.polito.mad.insane.lab4.R;

/**
 * Created by Federico on 08/06/2016.
 */
public class MySpinnerAdapterFilter extends ArrayAdapter<String>
{
    Context contextSpinner;
    List<String> choices = new ArrayList<>();
    Resources resSpinnerAdapter;


    public MySpinnerAdapterFilter(Context context, int resource, List objects, Resources res)
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
//        View view = super.getDropDownView(position, convertView, parent);
//        TextView tv = (TextView) view;
//        if(position == 0){
//            // Set the hint text color gray
//            tv.setTextColor(Color.GRAY);
//        }
//        else
//            tv.setTextColor(Color.BLACK);
//
//        return view;

        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int pos, View cnvtView, ViewGroup prnt) {

        //return getCustomView(pos, cnvtView, prnt);
        if(pos == 0) {

            LayoutInflater inflater = LayoutInflater.from(this.contextSpinner);
            View mySpinner = inflater.inflate(R.layout.custom_spinner, prnt,
                    false);

            ImageView image = (ImageView) mySpinner.findViewById(R.id.spinner_image);
            image.setVisibility(View.INVISIBLE);

            TextView main_text = (TextView) mySpinner
                    .findViewById(R.id.name_option);
            main_text.setText(choices.get(pos));

            return mySpinner;
        }else{
            LayoutInflater inflater = LayoutInflater.from(this.contextSpinner);
            View mySpinner = inflater.inflate(R.layout.custom_spinner, prnt,
                    false);

            TextView main_text = (TextView) mySpinner.findViewById(R.id.name_option);
            main_text.setText(choices.get(pos));

            return mySpinner;
        }
    }
    public View getCustomView(int position, View convertView,
                              ViewGroup parent) {


        if(position == 0) {

            LayoutInflater inflater = LayoutInflater.from(this.contextSpinner);
            View mySpinner = inflater.inflate(R.layout.custom_spinner, parent,
                    false);

            ImageView image = (ImageView) mySpinner.findViewById(R.id.spinner_image);
            image.setVisibility(View.INVISIBLE);

            TextView main_text = (TextView) mySpinner
                    .findViewById(R.id.name_option);
            main_text.setText(choices.get(position));
            main_text.setTextColor(resSpinnerAdapter.getColor(R.color.colorPrimary));


            return mySpinner;
        }else{
            LayoutInflater inflater = LayoutInflater.from(this.contextSpinner);
            View mySpinner = inflater.inflate(R.layout.custom_spinner, parent,
                    false);

            ImageView image = (ImageView) mySpinner.findViewById(R.id.spinner_image);
            image.setVisibility(View.INVISIBLE);

            TextView main_text = (TextView) mySpinner.findViewById(R.id.name_option);
            main_text.setText(choices.get(position));

            return mySpinner;
        }


    }
}

