package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.activities.NotificationsActivity;
import it.polito.mad.insane.lab4.activities.RestaurantProfileActivity;
import it.polito.mad.insane.lab4.data.DailyOfferSimple;
import it.polito.mad.insane.lab4.data.Dish;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

/**
 * Created by Federico on 10/06/2016.
 */
public class NotifyArrayAdapter extends ArrayAdapter<Dish> {

    private Context context;
    private int layoutResourceId;
    private List<Object> data;
    private int numNotifications;

    public NotifyArrayAdapter(Context context, int resource, List<Object> data) {
        //costruttore
        super(context, resource);
        this.context = context;
        this.layoutResourceId = resource;
        this.data = data;
        this.numNotifications=data.size()-1;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final NotifyHolder holder;

        final LayoutInflater inflater;
        inflater = ((NotificationsActivity) context).getLayoutInflater();
        row = inflater.inflate(layoutResourceId, parent, false);

        holder = new NotifyHolder();
        holder.restaurantName = (TextView) row.findViewById(R.id.notify_item_restaurant_name);
        holder.description = (TextView) row.findViewById(R.id.notify_item_description);
        holder.notificationID = (TextView) row.findViewById(R.id.notify_item_notify_id);
        holder.cardview = (CardView) row.findViewById(R.id.notify_card_view);

        holder.restaurantName.setText(((DailyOfferSimple) data.get(numNotifications-position)).getRestaurantName());
        holder.description.setText(((DailyOfferSimple) data.get(numNotifications-position)).getDescription());

        //setta in grassetto corsivo quelle ancora non lette
        if(((DailyOfferSimple) data.get(numNotifications-position)).isRead()==false){
            holder.restaurantName.setTypeface(holder.restaurantName.getTypeface(), Typeface.BOLD_ITALIC);
            holder.description.setTypeface(holder.description.getTypeface(),Typeface.BOLD_ITALIC);
        }

        holder.notificationID.setText(((DailyOfferSimple) data.get(numNotifications-position)).getID());

        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //go to the restaurant
                Intent resultIntent = new Intent(context, RestaurantProfileActivity.class);
                resultIntent.putExtra("ID", ((DailyOfferSimple) data.get(numNotifications-position)).getRestaurantId());
                resultIntent.putExtra("Name",((DailyOfferSimple) data.get(numNotifications-position)).getRestaurantName());

                RestaurateurJsonManager manager = RestaurateurJsonManager.getInstance(context);


                //non lo elimino più, notifico che è stata letta
                  //manager.removeDailyOffer(((DailyOfferSimple) data.get(position)).getID());
                  manager.setDailyOfferRead(((DailyOfferSimple) data.get(numNotifications-position)).getID());
                  //data.remove(position);
                  //notifyDataSetChanged();





                getContext().startActivity(resultIntent);

                ((NotificationsActivity) context).finish();

            }
        });

        return row;
    }

    @Override
    public int getCount() {
        return this.data.size();

    }

    static class NotifyHolder
    {
        private TextView restaurantName;
        private TextView notificationID;
        private TextView description;
        private CardView cardview;
    }

}

