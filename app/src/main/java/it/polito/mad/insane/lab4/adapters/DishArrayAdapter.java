package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.activities.DisplayReservationActivity;
import it.polito.mad.insane.lab4.activities.EditOfferActivity;
import it.polito.mad.insane.lab4.activities.MakeReservationActivity;
import it.polito.mad.insane.lab4.activities.RestaurantProfileActivity;
import it.polito.mad.insane.lab4.activities.ViewBookingActivity;
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
        final DishHolder holder;

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
                    inflater = ((DisplayReservationActivity) context).getLayoutInflater();
                    break;
                case 3:
                    inflater = ((EditOfferActivity) context).getLayoutInflater();
                    break;
                case 4:
                    inflater = ((ViewBookingActivity) context).getLayoutInflater();
            }

            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DishHolder();
            holder.name = (TextView) row.findViewById(R.id.summary_dish_name);
            holder.quantity = (TextView) row.findViewById(R.id.summary_dish_quantity);
            holder.totalPrice = (TextView) row.findViewById(R.id.summary_dish_total_price);
            if(currentActivity == 3)
            {
                holder.minusButton = (ImageView) row.findViewById(R.id.dish_minus_button);
                holder.minusButton.setOnClickListener(new View.OnClickListener() {
                                                          @Override
                                                          public void onClick(View v)
                                                          {
                                                              int dishQuantity = Integer.parseInt(holder.quantity.getText().toString());
                                                              if(dishQuantity > 0)
                                                              {
                                                                  dishQuantity --;
                                                                  holder.quantity.setText(String.valueOf(dishQuantity));
                                                                  quantitiesMap.put(holder.currentDish, dishQuantity);
                                                                  DecimalFormat df = new DecimalFormat("0.00");
                                                                  holder.totalPrice.setText(MessageFormat.format("{0}€", String.valueOf(df.format(holder.currentDish.getPrice() * quantitiesMap.get(holder.currentDish)))));
                                                              }

                                                          }
                                                      });
                holder.plusButton = (ImageView) row.findViewById(R.id.dish_plus_button);
                holder.plusButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v)
                                                    {
                                                        int dishQuantity = Integer.parseInt(holder.quantity.getText().toString());
                                                        dishQuantity ++;
                                                        holder.quantity.setText(String.valueOf(dishQuantity));
                                                        quantitiesMap.put(holder.currentDish, dishQuantity);
                                                        DecimalFormat df = new DecimalFormat("0.00");
                                                        holder.totalPrice.setText(MessageFormat.format("{0}€", String.valueOf(df.format(holder.currentDish.getPrice() * quantitiesMap.get(holder.currentDish)))));
                                                    }
                                                });
            }

            row.setTag(holder);
        } else {
            holder = (DishHolder) row.getTag();
        }

        holder.currentDish = dishes.get(position);
        holder.name.setText(holder.currentDish.getName());
        if(currentActivity == 3)
            holder.quantity.setText(String.valueOf(quantitiesMap.get(holder.currentDish)));
        else
            holder.quantity.setText(MessageFormat.format("{0}x", String.valueOf(quantitiesMap.get(holder.currentDish))));

        DecimalFormat df = new DecimalFormat("0.00");
        holder.totalPrice.setText(MessageFormat.format("{0}€", String.valueOf(df.format(holder.currentDish.getPrice() * quantitiesMap.get(holder.currentDish)))));
        if(currentActivity == 1)
            holder.totalPrice.setVisibility(View.GONE);

        return row;
    }

    static class DishHolder
    {
        private TextView quantity;
        private TextView name;
        private TextView totalPrice;
        private ImageView minusButton;
        private ImageView plusButton;
        private Dish currentDish;
    }

    public HashMap<Dish,Integer> getQuantitiesMap()
    {
        return this.quantitiesMap;
    }
}
