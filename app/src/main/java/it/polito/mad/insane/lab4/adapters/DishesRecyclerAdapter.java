package it.polito.mad.insane.lab4.adapters;

/**
 * Created by miche on 31/05/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.activities.EditDishActivity;
import it.polito.mad.insane.lab4.activities.RestaurantProfileActivity;
import it.polito.mad.insane.lab4.data.Dish;

public class DishesRecyclerAdapter extends RecyclerView.Adapter<DishesRecyclerAdapter.DishesViewHolder>
{
    private final Context context;
    private List<Dish> mData; // actual data to be displayed
    private int[] popupsVisibility; //per evitare problemi con le posizioni delle view, memorizzo qui il flag del popup, se visibile o meno
//    private int[] selectedQuantities; //array che contiene le quantita' selezionate di ogni piatto del menu'
    private LayoutInflater mInflater;
    private  int reservationQty; // quantita' totale di item presenti nella prenotazione in esame
    private  double reservationPrice; //prezzo totale degli item presenti nella prenotazione in esame
    private HashMap<Dish, Integer> quantitiesMap; //mappa che contiene le quantita' selezionate di ogni piatto del menu'
    private String ridAdapter;
    private int currentActivity;


    public DishesRecyclerAdapter(Context context, List<Dish> data, String rid, int currentActivity)
    {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mData = data;
        this.currentActivity = currentActivity;

        popupsVisibility = new int[data.size()];
        Arrays.fill(popupsVisibility, View.GONE); // all'inizio i popup sono tutti invisibili
//        selectedQuantities = new int[data.size()];
//        Arrays.fill(selectedQuantities, 0);
        quantitiesMap = new HashMap<>();
        reservationQty = 0;
        reservationPrice = 0;
        ridAdapter = rid;

    }

    /**
     * Method called when a ViewHolder Object is created
     * The returned object contains a reference to a view representing the bare structure of the item
     * @param parent
     * @param viewType
     */
    @Override
    public DishesViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        // create the view starting from XML layout file
        View view = this.mInflater.inflate(R.layout.dish_cardview, parent, false);
        if(ridAdapter != null){
            view.findViewById(R.id.expand_arrow).setVisibility(View.GONE);
        }
        DishesViewHolder result =  new DishesViewHolder(view);
        return result;
    }

    /**
     * Method called after onCreateViewHolder() and it fetches data from the model and properly sets view accordingly
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final DishesViewHolder holder, int position)
    {
        Dish currentObj = this.mData.get(position);
        holder.setData(currentObj, position);
    }

    @Override
    public int getItemCount()
    {
        return this.mData.size();
    }

    public List<Dish> getmData(){
        return mData;
    }

    public int getReservationQty(){
        return reservationQty;
    }

    public double getReservationPrice(){
        return reservationPrice;
    }

//    public int[] getSelectedQuantities(){
//        return selectedQuantities;
//    }

    public HashMap<Dish, Integer> getQuantitiesMap(){
        return quantitiesMap;
    }

    public class DishesViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView dishPhoto;
        private TextView dishID;
        private TextView dishName;
        private TextView dishDesc;
        private TextView dishPrice;
        private TextView dishAvailability;
        private Button minusButton;
        private Button plusButton;
        private TextView selectedQuantity;
        private TextView selectedPrice;
        private LinearLayout selectionLayout;
        private LinearLayout mainLayout;
        private View separator;
        private ImageView expandArrow;
//        private CheckBox selection;

        public DishesViewHolder(View itemView)
        {
            super(itemView);
            this.dishPhoto = (ImageView) itemView.findViewById(R.id.dish_photo);
            this.dishID = (TextView) itemView.findViewById(R.id.dish_ID);
            this.dishName = (TextView) itemView.findViewById(R.id.dish_name);
            this.dishDesc =  (TextView) itemView.findViewById(R.id.dish_description);
            this.dishPrice = (TextView) itemView.findViewById(R.id.dish_price);
            this.dishAvailability = (TextView) itemView.findViewById(R.id.dish_availability);
            this.minusButton = (Button) itemView.findViewById(R.id.dish_minus_button);
            this.plusButton = (Button) itemView.findViewById(R.id.dish_plus_button);
            this.selectedQuantity = (TextView) itemView.findViewById(R.id.dish_selected_quantity);
            this.selectedPrice = (TextView) itemView.findViewById(R.id.dish_selected_price);
            this.selectionLayout = (LinearLayout) itemView.findViewById(R.id.add_dish_popup) ;
            this.mainLayout = (LinearLayout) itemView.findViewById(R.id.cardView_main_layout);
            this.separator = itemView.findViewById(R.id.cardView_separator);
            this.expandArrow = (ImageView) itemView.findViewById(R.id.expand_arrow);
            if(currentActivity == 1) //DailyMenuActivity
                this.expandArrow.setVisibility(View.GONE);
        }

        public void setData(final Dish current, int position )
        {
            final int pos = position;
            DecimalFormat df = new DecimalFormat("0.00");
            this.dishID.setText(current.getID());
            this.dishName.setText(current.getName());
            this.dishDesc.setText(current.getDescription());
            this.dishPrice.setText(MessageFormat.format("{0}€", String.valueOf(df.format(current.getPrice()))));
            if(current.getAvailabilityQty() == 0) {
                this.dishAvailability.setVisibility(View.VISIBLE);
                this.expandArrow.setVisibility(View.GONE);
                popupsVisibility[position] = View.GONE;             // mostrare solo quando non e' disponibile
            }

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://lab4-insane.appspot.com/restaurants/" + ridAdapter +
                    "/dishes/" + current.getID() + "/dish.jpg");

            String path = storageRef.getPath();

            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri uri) {
                    // Got the download URL for 'restaurants/myRestaurant/cover.jpg'
                    // Pass it to Glide to download, show in ImageView and caching
//                    Glide.with(context)
//                            .load(uri.toString())
//                            .placeholder(R.drawable.dish_default_green_5)
//                            .centerCrop()
//                            .error(R.drawable.wa_background)
//                            .into(dishPhoto);


                    DownloadImageTask dit = new DownloadImageTask();
                    dit.execute(uri, dishPhoto);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    //TODO gestire errore
//                    Toast.makeText(manager.myContext,"Glide: " + exception.toString(),Toast.LENGTH_SHORT).show();
                }
            });

            if(currentActivity != 1)
            {
                this.selectionLayout.setVisibility(popupsVisibility[position]); //layout del popup
                this.separator.setVisibility(popupsVisibility[position]); //layout della linea separatrice
            }
            if(quantitiesMap.get(current) != null){
                this.selectedQuantity.setText(String.valueOf(quantitiesMap.get(current)));
                df = new DecimalFormat("0.00");
                this.selectedPrice.setText(MessageFormat.format("{0}€",
                        String.valueOf(df.format(quantitiesMap.get(current) * current.getPrice()))));
            }
            else{
                this.selectedQuantity.setText(String.valueOf(0));
                this.selectedPrice.setText(MessageFormat.format("{0}€",
                        String.valueOf(df.format(0))));
            }

            if(currentActivity == 0)
            {
                // RestaurantProfileActivity

                this.mainLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupsVisibility[pos] == View.GONE && current.getAvailabilityQty() != 0) { // al click se il popup è invisibile
                            // e il prodotto e' disponibile lo faccio apparire...
                            selectionLayout.setVisibility(View.VISIBLE);
                            separator.setVisibility(View.VISIBLE);
                            expandArrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_up_black_24dp));
                            popupsVisibility[pos] = View.VISIBLE;
                        } else { //... se e' visibile lo nascondo
                            selectionLayout.setVisibility(View.GONE);
                            separator.setVisibility(View.GONE);
                            expandArrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_down_black_24dp));
                            popupsVisibility[pos] = View.GONE;
                        }
                    }
                });

                this.minusButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int quantity = 0;
                        if (quantitiesMap.containsKey(current)) {
                            quantity = quantitiesMap.get(current);

//                        selectedQuantities[pos]--;
                            quantity--;
                            if (quantity != 0)
                                quantitiesMap.put(current, quantity);
                            else
                                quantitiesMap.remove(current);

                            selectedQuantity.setText(String.valueOf(quantity));
                            DecimalFormat df = new DecimalFormat("0.00");
                            selectedPrice.setText(MessageFormat.format("{0}€",
                                    String.valueOf(df.format(quantity * current.getPrice()))));

                            reservationPrice -= current.getPrice(); //decremento il prezzo totale della prenotazione
                            reservationQty--; //decremento la quantita' di item della prenotazione

                            TextView tv = (TextView) ((RestaurantProfileActivity) context).findViewById(R.id.show_reservation_button);
                            if (tv != null) {
                                if (reservationQty != 0)
                                    tv.setText(String.format(MessageFormat.format("%d {0} - %s€", v.getResources().getString(R.string.itemsFormat)), reservationQty, reservationPrice));
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
                        if (quantitiesMap.containsKey(current)) {
                            quantity = quantitiesMap.get(current);
                        } else
                            quantitiesMap.put(current, 0);

                        if (quantity < current.getAvailabilityQty()) {
//                        selectedQuantities[pos]++;
                            quantity++;
                            quantitiesMap.put(current, quantity);

                            selectedQuantity.setText(String.valueOf(quantity));
                            DecimalFormat df = new DecimalFormat("0.00");
                            selectedPrice.setText(MessageFormat.format("{0}€",
                                    String.valueOf(df.format((quantity) * current.getPrice()))));

                            reservationPrice += current.getPrice(); //incremento il prezzo totale della prenotazione
                            reservationQty++; //incremento la quantita' di item della prenotazione


                            TextView tv = (TextView) ((RestaurantProfileActivity) context).findViewById(R.id.show_reservation_button);
                            if (tv != null) {
                                if (reservationQty != 0)
                                    tv.setText(String.format("%d " + v.getResources().getString(R.string.itemsFormat) + " - %s€", reservationQty, reservationPrice));
                                else
                                    tv.setText(R.string.empty_cart);
                            }
                        }
                    }
                });
            }else
            {
                // DailyMenuActivity
                this.mainLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent i = new Intent(v.getContext(),EditDishActivity.class);
                        i.putExtra("dish",current);
                        v.getContext().startActivity(i);
                    }
                });

            }
        }
    }

    public class DownloadImageTask extends AsyncTask<Object, Void, Bitmap> {

        private ImageView photo;

        @Override
        protected Bitmap doInBackground(Object... params) {

            this.photo = (ImageView) params[1];
            Looper.prepare();
            Bitmap bitmap;
            try {
                bitmap = Glide.
                        with(context).
                        load(params[0].toString()).
                        asBitmap().
                        into(1920,1080).
                        get();
            } catch (final ExecutionException e) {
                return null;
            } catch (final InterruptedException e) {
                return null;
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            this.photo.setImageBitmap(bitmap);
        }
    }
}

