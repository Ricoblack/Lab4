package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.data.Dish;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

/**
 * Created by miche on 03/06/2016.
 */
public class DailyOfferRecyclerAdapter extends RecyclerView.Adapter<DailyOfferRecyclerAdapter.DailyOfferHolder>
{

    private final Context context;
    private LayoutInflater mInflater;
    private List<DailyOffer> mData; // actual data to be displayed
    private int[] popupsVisibility;
    private String restaurantId;

    public DailyOfferRecyclerAdapter(Context context, List < DailyOffer > data, String restaurantID)
    {
        this.context = context;
        this.mData = data;
        this.mInflater = LayoutInflater.from(context);
        this.restaurantId = restaurantID;

        popupsVisibility = new int[data.size()];
        Arrays.fill(popupsVisibility, View.GONE); // all'inizio i popup sono tutti invisibili
    }

    @Override
    public DailyOfferHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = this.mInflater.inflate(R.layout.daily_offer_cardview, parent, false);
        DailyOfferHolder holder = new DailyOfferHolder(view); // create the holder
        return holder;
    }

    @Override
    public void onBindViewHolder(DailyOfferHolder holder, int position) {
        DailyOffer currentObj = this.mData.get(position);
        holder.setData(currentObj, position, holder.cardView);
    }

    @Override
    public int getItemCount() {
        return this.mData.size();
    }


    public class DailyOfferHolder extends RecyclerView.ViewHolder
    {
        private View cardView;
        private RestaurateurJsonManager manager;
//        private int position;
        private DailyOffer currentDailyOffer;
        private TextView dailyOfferName;
        private TextView dailyOfferID;
        private TextView dailyOfferPrice;
        private TextView dailyOfferDescription;
        private LinearLayout popupLayout;
        private ImageView expandArrow;
        private ListView dishListView;

        public DailyOfferHolder(View itemView) {
            super(itemView);
            this.manager = RestaurateurJsonManager.getInstance(null);
            this.dailyOfferName = (TextView) itemView.findViewById(R.id.daily_offer_name);
            this.dailyOfferID = (TextView) itemView.findViewById(R.id.daily_offer_ID);
            this.dailyOfferPrice = (TextView) itemView.findViewById(R.id.daily_offer_price);
            this.dailyOfferDescription = (TextView) itemView.findViewById(R.id.daily_offer_description);
            this.cardView = itemView;
            this.popupLayout = (LinearLayout) itemView.findViewById(R.id.daily_offer_popup_layout);
            this.expandArrow = (ImageView) itemView.findViewById(R.id.expand_arrow);
            this.dishListView = (ListView) itemView.findViewById(R.id.daily_offer_listview);
        }

        public void setData(final DailyOffer current, final int position, View view)
        {
//            this.position = position;
            this.currentDailyOffer = current;
            RestaurateurJsonManager manager = RestaurateurJsonManager.getInstance(context);

            this.dailyOfferID.setText(current.getID());
            this.dailyOfferName.setText(current.getName());
            this.dailyOfferDescription.setText(current.getDescription());


            DecimalFormat df = new DecimalFormat("0.00");
            this.dailyOfferPrice.setText(MessageFormat.format("{0}€", String.valueOf( df.format(current.getPrice()))));

            this.popupLayout.setVisibility(popupsVisibility[position]); //layout del popup

            // TODO sistemare la grafica delle cardView del fragment dailyOffer: non si vedono tutti gli item della ListView ma
            // TODO solo uno (x Federico, da Renato con affetto XD)
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference dishesRef = database.getReference("/restaurants/" + restaurantId + "/dishMap" );

            dishesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, Dish> dishesMap = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Dish>>() {
                        @Override
                        protected Object clone() throws CloneNotSupportedException {
                            return super.clone();
                        }
                    });
                    if(dishesMap != null){
                        HashMap<Dish, Integer> filteredDishesMap = new HashMap<Dish, Integer>();
                        for(Dish d : dishesMap.values()){
                            if(current.getDishesIdMap().containsKey(d.getID()))
                                filteredDishesMap.put(d, current.getDishesIdMap().get(d.getID()));
                        }

                        DishArrayAdapter adapter = new DishArrayAdapter(context, R.layout.dish_listview_item, filteredDishesMap, 1);
                        dishListView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
//


            this.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(popupsVisibility[position] == View.GONE) { // al click se il popup è invisibile
                        // e il prodotto e' disponibile lo faccio apparire...
                        popupLayout.setVisibility(View.VISIBLE);
                        expandArrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_up_black_24dp));
                        popupsVisibility[position] = View.VISIBLE;
                    }
                    else { //... se e' visibile lo nascondo
                        popupLayout.setVisibility(View.GONE);
                        expandArrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_down_black_24dp));
                        popupsVisibility[position] = View.GONE;
                    }
                }
            });
        }
    }
}
