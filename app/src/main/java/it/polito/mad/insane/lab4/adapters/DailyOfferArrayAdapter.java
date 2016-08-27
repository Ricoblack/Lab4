package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.activities.DisplayReservationActivity;
import it.polito.mad.insane.lab4.activities.EditOfferActivity;
import it.polito.mad.insane.lab4.activities.MakeReservationActivity;
import it.polito.mad.insane.lab4.activities.RestaurantProfileActivity;
import it.polito.mad.insane.lab4.activities.ViewBookingActivity;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.data.Dish;

/**
 * Created by Renato on 27/08/2016.
 */
public class DailyOfferArrayAdapter extends ArrayAdapter<DailyOffer>{

    private int currentActivity;
    private Context context;
    private int layoutResourceId;
    private List<DailyOffer> offers;
    private HashMap<DailyOffer, Integer> quantitiesMap;

    public DailyOfferArrayAdapter(Context context, int resource, HashMap<DailyOffer, Integer> quantitiesMap,
                            int currentActivity) {
        super(context, resource, new ArrayList<>(quantitiesMap.keySet()));
        this.offers = new ArrayList<>(quantitiesMap.keySet());
        this.context = context;
        this.layoutResourceId = resource;
        this.quantitiesMap = quantitiesMap;
        this.currentActivity = currentActivity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final DailyOfferHolder holder;

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

            holder = new DailyOfferHolder();
            holder.name = (TextView) row.findViewById(R.id.summary_daily_offer_name);
            holder.quantity = (TextView) row.findViewById(R.id.summary_daily_offer_quantity);
            holder.price = (TextView) row.findViewById(R.id.summary_daily_offer_total_price);
//            if(currentActivity == 3)
//            {
//                holder.minusButton = (ImageView) row.findViewById(R.id.dish_minus_button);
//                holder.minusButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v)
//                    {
//                        int dishQuantity = Integer.parseInt(holder.quantity.getText().toString());
//                        if(dishQuantity > 0)
//                        {
//                            dishQuantity --;
//                            holder.quantity.setText(String.valueOf(dishQuantity));
//                            quantitiesMap.put(holder.currentDish, dishQuantity);
//                            DecimalFormat df = new DecimalFormat("0.00");
//                            holder.totalPrice.setText(MessageFormat.format("{0}€", String.valueOf(df.format(holder.currentDish.getPrice() * quantitiesMap.get(holder.currentDish)))));
//                        }
//
//                    }
//                });
//                holder.plusButton = (ImageView) row.findViewById(R.id.dish_plus_button);
//                holder.plusButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v)
//                    {
//                        int dishQuantity = Integer.parseInt(holder.quantity.getText().toString());
//                        dishQuantity ++;
//                        holder.quantity.setText(String.valueOf(dishQuantity));
//                        quantitiesMap.put(holder.currentDish, dishQuantity);
//                        DecimalFormat df = new DecimalFormat("0.00");
//                        holder.totalPrice.setText(MessageFormat.format("{0}€", String.valueOf(df.format(holder.currentDish.getPrice() * quantitiesMap.get(holder.currentDish)))));
//                    }
//                });
//            }

            row.setTag(holder);
        } else {
            holder = (DailyOfferHolder) row.getTag();
        }

        holder.currentOffer = offers.get(position);
        holder.name.setText(holder.currentOffer.getName());
        if(currentActivity == 3)
            holder.quantity.setText(String.valueOf(quantitiesMap.get(holder.currentOffer)));
        else
            holder.quantity.setText(MessageFormat.format("{0}x", String.valueOf(quantitiesMap.get(holder.currentOffer))));

        DecimalFormat df = new DecimalFormat("0.00");
        holder.price.setText(MessageFormat.format("{0}€", String.valueOf(df.format(holder.currentOffer.getPrice() *
                quantitiesMap.get(holder.currentOffer)))));
        if(currentActivity == 1)
            holder.price.setVisibility(View.GONE);

        return row;
    }

    static class DailyOfferHolder
    {
        private TextView quantity;
        private TextView name;
        private ListView dailyOfferDishes; // TODO riempire questa lista
        private TextView price;
        private DailyOffer currentOffer;
    }

    public HashMap<DailyOffer,Integer> getQuantitiesMap()
    {
        return this.quantitiesMap;
    }

}
