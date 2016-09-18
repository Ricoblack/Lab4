package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import it.polito.mad.insane.lab4.activities.EditOfferActivity;
import it.polito.mad.insane.lab4.activities.RestaurantProfileActivity;
import it.polito.mad.insane.lab4.data.Cart;
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
    private int currentActivity;
    private Cart cart;

    public DailyOfferRecyclerAdapter(Context context, List < DailyOffer > data, String restaurantID, int currentActivity, Cart cart)
    {
        this.context = context;
        this.mData = data;
        this.mInflater = LayoutInflater.from(context);
        this.restaurantId = restaurantID;
        this.currentActivity = currentActivity;
        this.cart = cart;

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
        private Button minusButton;
        private Button plusButton;
        private TextView selectedQuantity;
        private TextView selectedPrice;
        private TextView dishAvailability;

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
            this.minusButton = (Button) itemView.findViewById(R.id.daily_offer_minus_button);
            this.plusButton = (Button) itemView.findViewById(R.id.daily_offer_plus_button);
            this.selectedQuantity = (TextView) itemView.findViewById(R.id.daily_offer_selected_quantity);
            this.selectedPrice = (TextView) itemView.findViewById(R.id.daily_offer_selected_price);
            this.dishAvailability = (TextView) itemView.findViewById(R.id.daily_offer_availability);

            if(currentActivity == 1) // DailyMenu
            {
                this.popupLayout.setVisibility(View.GONE);
                this.expandArrow.setVisibility(View.GONE);
            }
        }

        public void setData(final DailyOffer current, final int position, View view)
        {
//            this.position = position;
            this.currentDailyOffer = current;
            RestaurateurJsonManager manager = RestaurateurJsonManager.getInstance(context);

            this.dailyOfferID.setText(current.getID());
            this.dailyOfferName.setText(current.getName());
            this.dailyOfferDescription.setText(current.getDescription());

            if(current.getAvailableQuantity() == 0) {
                this.dishAvailability.setVisibility(View.VISIBLE);
                this.expandArrow.setVisibility(View.GONE);
                popupsVisibility[position] = View.GONE;             // mostrare solo quando non e' disponibile
            }


            DecimalFormat df = new DecimalFormat("0.00");
            this.dailyOfferPrice.setText(MessageFormat.format("{0}€", String.valueOf( df.format(current.getPrice()))));

            if(currentActivity != 1)
                this.popupLayout.setVisibility(popupsVisibility[position]); //layout del popup

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

                        DishArrayAdapter adapter = new DishArrayAdapter(context, R.layout.dish_listview_item, filteredDishesMap, 1, true);
                        dishListView.setAdapter(adapter);

                        // TODO se la disponibilita' del piatto e' terminata la dailyOffer non deve essere prenotabile
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if(currentActivity == 0) // RestaurantProfileActivity
            {
                this.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupsVisibility[position] == View.GONE && current.getAvailableQuantity() != 0) { // al click se il popup è invisibile
                            // e il prodotto e' disponibile lo faccio apparire...
                            popupLayout.setVisibility(View.VISIBLE);
                            expandArrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_up_black_24dp));
                            popupsVisibility[position] = View.VISIBLE;
                        } else { //... se e' visibile lo nascondo
                            popupLayout.setVisibility(View.GONE);
                            expandArrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_down_black_24dp));
                            popupsVisibility[position] = View.GONE;
                        }
                    }
                });
            } else if(currentActivity == 1 ) // DailyMenuActivity
            {
                this.cardView.setOnClickListener(new View.OnClickListener()
                {

                    @Override
                    public void onClick(View v)
                    {
                        Intent i = new Intent(v.getContext(),EditOfferActivity.class);
                        i.putExtra("offer",currentDailyOffer);
                        v.getContext().startActivity(i);
                    }
                });
            }

            this.minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantity = 0;
                    if (cart.getOffersQuantityMap().containsKey(current)) {
                        quantity = cart.getOffersQuantityMap().get(current);

//                        selectedQuantities[pos]--;
                        quantity--;
                        if (quantity != 0)
                            cart.getOffersQuantityMap().put(current, quantity);
                        else
                            cart.getOffersQuantityMap().remove(current);

                        selectedQuantity.setText(String.valueOf(quantity));
                        DecimalFormat df = new DecimalFormat("0.00");
                        selectedPrice.setText(MessageFormat.format("{0}€",
                                String.valueOf(df.format(quantity * current.getPrice()))));

                        double reservationPrice = cart.getReservationPrice();
                        reservationPrice -= current.getPrice(); //decremento il prezzo totale della prenotazione
                        cart.setReservationPrice(reservationPrice);

                        int reservationQty = cart.getReservationQty();
                        reservationQty --; //decremento la quantita' di item della prenotazione di 1 (considero la dailyOffer come un solo piatto)
                        cart.setReservationQty(reservationQty);

                        TextView tv = (TextView) ((RestaurantProfileActivity) context).findViewById(R.id.daily_offer_cart);
                        if (tv != null) {
                            if (cart.getReservationQty() != 0)
                                tv.setText(String.format(MessageFormat.format("%d {0} - %s€", v.getResources().getString(R.string.itemsFormat)), cart.getReservationQty(),
                                        df.format(cart.getReservationPrice())));
                            else
                                tv.setText(R.string.empty_cart);
                        }
                    }
                }
            });

            this.plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int quantity = 0;
                    if (cart.getOffersQuantityMap().containsKey(current)) {
                        quantity = cart.getOffersQuantityMap().get(current);
                    } else
                        cart.getOffersQuantityMap().put(current, 0);

                    if (quantity < current.getAvailableQuantity()) { // FIXME se una dailyOffer contiene piatti con disponibilita' esaurita
                                                                     // FIXME non deve essere prenotabile
//                        selectedQuantities[pos]++;
                        quantity++;
                        cart.getOffersQuantityMap().put(current, quantity);

                        selectedQuantity.setText(String.valueOf(quantity));
                        DecimalFormat df = new DecimalFormat("0.00");
                        selectedPrice.setText(MessageFormat.format("{0}€",
                                String.valueOf(df.format((quantity) * current.getPrice()))));

                        double reservationPrice = cart.getReservationPrice();
                        reservationPrice += current.getPrice(); //incremento il prezzo totale della prenotazione
                        cart.setReservationPrice(reservationPrice);

                        int reservationQty = cart.getReservationQty();
                        reservationQty++; //incremento la quantita' di item della prenotazione
                        cart.setReservationQty(reservationQty);

                        TextView tv = (TextView) ((RestaurantProfileActivity) context).findViewById(R.id.daily_offer_cart);
                        if (tv != null) {
                            if (cart.getReservationQty() != 0)
                                tv.setText(String.format("%d " + v.getResources().getString(R.string.itemsFormat) + " - %s€", reservationQty, df.format(reservationPrice)));
                            else
                                tv.setText(R.string.empty_cart);
                        }
                    }
                }
            });
        }
    }
}
