package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.activities.DisplayReservation;
import it.polito.mad.insane.lab4.activities.MakeReservationActivity;
import it.polito.mad.insane.lab4.activities.RestaurantProfileActivity;
import it.polito.mad.insane.lab4.data.Dish;

/**
 * Created by Renato on 03/06/2016.
 */
public class DishArrayAdapter extends ArrayAdapter<Dish>{
    private int currentActivity;
    private Context context;
    private int layoutResourceId;
    private List<Dish> dishes;
    private HashMap<Dish, Integer> quantitiesMap;

    public DishArrayAdapter(Context context, int resource, HashMap<Dish, Integer> quantitiesMap,
                            int currentActivity) {
        super(context, resource, new ArrayList<>(quantitiesMap.keySet()));
        this.dishes = new ArrayList<>(quantitiesMap.keySet());
        this.context = context;
        this.layoutResourceId = resource;
        this.quantitiesMap = quantitiesMap;
        this.currentActivity = currentActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DishHolder holder;

        if (row == null) {
            LayoutInflater inflater = null;
            switch(currentActivity){
                case 0:
                    inflater = ((MakeReservationActivity) context).getLayoutInflater();
                    break;
                case 1:
                    inflater = ((RestaurantProfileActivity) context).getLayoutInflater();
                    break;
                case 2:
                    inflater = ((DisplayReservation) context).getLayoutInflater();
                    break;
            }

            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DishHolder();
            holder.name = (TextView) row.findViewById(R.id.summary_dish_name);
            holder.quantity = (TextView) row.findViewById(R.id.summary_dish_quantity);
            holder.totalPrice = (TextView) row.findViewById(R.id.summary_dish_total_price);

            row.setTag(holder);
        } else {
            holder = (DishHolder) row.getTag();
        }

        Dish dish = dishes.get(position);
        holder.name.setText(dish.getName());
        holder.quantity.setText(MessageFormat.format("{0}x", String.valueOf(quantitiesMap.get(dish))));
        DecimalFormat df = new DecimalFormat("0.00");
        holder.totalPrice.setText(MessageFormat.format("{0}€", String.valueOf(df.format(dish.getPrice() * quantitiesMap.get(dish)))));
        if(currentActivity == 1)
            holder.totalPrice.setVisibility(View.GONE);

        return row;
    }

    static class DishHolder
    {
        private TextView quantity;
        private TextView name;
        private TextView totalPrice;
    }
}
