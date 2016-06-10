package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.activities.MakeReservationActivity;
import it.polito.mad.insane.lab4.activities.NotificationsActivity;
import it.polito.mad.insane.lab4.data.Dish;

/**
 * Created by Federico on 10/06/2016.
 */
public class NotifyArrayAdapter extends ArrayAdapter<Dish> {

    private Context context;
    private int layoutResourceId;
    private List<Object> data;

    public NotifyArrayAdapter(Context context, int resource, List<Object> data) {
        //costruttore
        super(context, resource);
        this.context = context;
        this.layoutResourceId = resource;
        this.data = data;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final NotifyHolder holder;

        LayoutInflater inflater;
        inflater = ((NotificationsActivity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new NotifyHolder();
        holder.restaurantName = (TextView) row.findViewById(R.id.notify_item_restaurant_name);
        holder.offerName = (TextView) row.findViewById(R.id.notify_item_offer_name);
        holder.description = (TextView) row.findViewById(R.id.notify_item_description);
        holder.notificationID = (TextView) row.findViewById(R.id.notify_item_notify_id);
        holder.cardview = (CardView) row.findViewById(R.id.notify_item_card_view);



        return row;
    }

    static class NotifyHolder
    {
        private TextView restaurantName;
        private TextView offerName;
        private TextView notificationID;
        private TextView description;
        private CardView cardview;
    }

}

