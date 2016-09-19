package it.polito.mad.insane.lab4.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.activities.DisplayReservationActivity;
import it.polito.mad.insane.lab4.data.Booking;
import it.polito.mad.insane.lab4.data.Dish;
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;


/**
 * Created by Renato on 10/05/2016.
 */
public class ReservationsRecyclerAdapter extends RecyclerView.Adapter<ReservationsRecyclerAdapter.BookingsViewHolder>{

    private final Context context;
    private List<Booking> mData; //actual data to be displayed
    private LayoutInflater mInflater;
    private DatabaseReference myRef;
    private ValueEventListener listener;


    public ReservationsRecyclerAdapter(Context context, List<Booking> data, ValueEventListener listener, DatabaseReference myRef){
        this.context = context;
        this.mData = data;
        this.mInflater = LayoutInflater.from(context);
        this.myRef = myRef;
        this.listener = listener;
    }

    @Override
    public BookingsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = this.mInflater.inflate(R.layout.reservation_cardview, parent, false);
        BookingsViewHolder holder = new BookingsViewHolder(view); // create the holder
        return holder;
    }

    @Override
    public void onBindViewHolder(BookingsViewHolder holder, int position) {
        Booking currentObj = this.mData.get(position);
        holder.setData(currentObj, position, holder.view);
    }

    @Override
    public int getItemCount() {
        return this.mData.size();
    }

    public class BookingsViewHolder extends RecyclerView.ViewHolder {

        private TextView evaso;
        private ImageView restaurantPhoto; //T-ODO implementare selezione immagini
        private TextView restaurantName;
        private TextView ID;
        private TextView date;
        private TextView hour;
        private TextView nItems;
        private TextView totalPrice;
        private View view;
        private ImageView trash;
        private RestaurateurJsonManager manager;
        private int position;
        private Booking currentBooking;
        private View cardView;
        private TextView evadedText;
        private LinearLayout cardviewLayout;

        private View.OnClickListener cardViewListener = new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                //Toast.makeText(v.getContext(),"Cliccato sulla cardView", Toast.LENGTH_LONG).show();
                Intent i = new Intent(v.getContext(),DisplayReservationActivity.class);
                i.putExtra("Booking", BookingsViewHolder.this.currentBooking);
                v.getContext().startActivity(i);
            }
        };

        public BookingsViewHolder(View itemView) {
            super(itemView);
            this.manager = RestaurateurJsonManager.getInstance(null);
            this.restaurantName = (TextView) itemView.findViewById(R.id.reservation_cardview_restaurant_name);
            this.ID = (TextView) itemView.findViewById(R.id.reservation_cardview_ID);
            this.date = (TextView) itemView.findViewById(R.id.reservation_cardview_date);
            this.hour = (TextView) itemView.findViewById(R.id.reservation_cardview_hour);
            this.nItems = (TextView) itemView.findViewById(R.id.reservation_cardview_nItems);
            this.totalPrice = (TextView) itemView.findViewById(R.id.reservation_cardview_price);
            this.trash = (ImageView) itemView.findViewById(R.id.delete_reservation);
            this.view = itemView;
            this.cardView = itemView;
            this.restaurantPhoto=(ImageView) itemView.findViewById(R.id.image_restaurant);
            this.evaso=(TextView) itemView.findViewById(R.id.reservation_cardview_evaso);
            this.evadedText = (TextView) itemView.findViewById(R.id.evaded_text);
            this.cardviewLayout = (LinearLayout) itemView.findViewById(R.id.reservation_cardview_layout);


            this.cardView.setOnClickListener(cardViewListener);
            this.trash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //check if user can delete current booking --> lo user può eliminarla entro 2 ore, sennò può solo eliminarla dopo
                    //il ristoratore
                    Date bookingDate;
                    Calendar bookingCal;
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    try {
                        bookingDate=sdf.parse(currentBooking.getDateTime());
                        bookingCal=Calendar.getInstance();
                        bookingCal.setTime(bookingDate);

                    } catch (ParseException e) {
                        Toast.makeText(manager.myContext,"Errore di conversione date, prenota nuovamente",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Calendar cal = Calendar.getInstance(); // creates calendar of now
                    cal.setTime(bookingDate); // sets calendar time/date
                    cal.add(Calendar.HOUR, -2); //minus two hours

                    Calendar calNow=Calendar.getInstance();
                    calNow.setTime(new Date());

                    if( calNow.before(cal) ) { // if( calNow.before(cal) || calNow.after(bookingCal) ) {
                        //Can delete reservation, continue
                    }
                    else {
                        Toast.makeText(manager.myContext,context.getResources().getText(R.string.impossible_delete_booking),Toast.LENGTH_LONG).show();
                        return;

                    }


                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getResources().getString(R.string.delete_reservation_alert_title))
                            .setPositiveButton(v.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    //elimino l'oggetto booking dalle mappe users e restaurants

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    //final DatabaseReference myRefadapter = database.getReference("/bookings/");
                                    DatabaseReference myRefadapter = database.getReference("/bookings/users/" +
                                            currentBooking.getUserId() + "/" + currentBooking.getID());
                                    myRefadapter.setValue(null, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        }
                                    });

                                    DatabaseReference myRefadapter2 = database.getReference("/bookings/restaurants/" +
                                            currentBooking.getRestaurantId()+ "/" + currentBooking.getID());
                                    myRefadapter2.setValue(null, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        }
                                    });


                                    //aggiorno le quantita' disponibili di piatti e dailyOffer della prenotazione

                                    final DatabaseReference restaurantRef = database.getReference("/restaurants/" +
                                            currentBooking.getRestaurantId());
                                    restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Restaurant restaurant = dataSnapshot.getValue(Restaurant.class);
                                            if(restaurant != null) {
                                                if (currentBooking.getDishesIdMap() != null) {
                                                    for (String bookingDishId : currentBooking.getDishesIdMap().keySet()) {
                                                        int qty = restaurant.getDishMap().get(bookingDishId).getAvailabilityQty();
                                                        qty += currentBooking.getDishesIdMap().get(bookingDishId);
                                                        restaurant.getDishMap().get(bookingDishId).setAvailabilityQty(qty);
                                                    }
                                                }

                                                if (currentBooking.getDailyOffersIdMap() != null) {
                                                    for (String bookingOfferId : currentBooking.getDailyOffersIdMap().keySet()) {
                                                        int qty = restaurant.getDailyOfferMap().get(bookingOfferId).getAvailableQuantity();
                                                        qty += currentBooking.getDailyOffersIdMap().get(bookingOfferId);
                                                        restaurant.getDailyOfferMap().get(bookingOfferId).setAvailableQuantity(qty);
                                                    }
                                                }

                                                restaurantRef.setValue(restaurant);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

//                                    myRef.setValue(null, new DatabaseReference.CompletionListener() {
//                                        @Override
//                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                            mData.remove(position);
//                                            notifyItemRemoved(position);
//                                            notifyItemRangeChanged(position, mData.size());
//                                            Toast.makeText(context,"eliminato",Toast.LENGTH_LONG).show();
//                                        }
//                                    });

                                    /*********************************************************/
//                                    myRefadapter.runTransaction(new Transaction.Handler() {
//
//                                        @Override
//                                        public Transaction.Result doTransaction(MutableData mutableData) {
//                                            DatabaseReference bookingRefUser = myRefadapter.child("users").child(currentBooking.getUserId()).child(currentBooking.getID());
//                                            bookingRefUser.setValue(null);
//
//
//                                            return Transaction.success(mutableData);
//                                        }
//
//                                        @Override
//                                        public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
////                                            if (!committed){
////                                            mData.remove(position);
////                                              notifyItemRemoved(position);
////                                            notifyItemRangeChanged(position, mData.size());
////                                            Toast.makeText(context,"eliminato",Toast.LENGTH_LONG).show();
////                                            }
////                                            else
////                                                Toast.makeText(context, R.string.error_delete_offer, Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//
//                                    myRefadapter.runTransaction(new Transaction.Handler() {
//
//                                        @Override
//                                        public Transaction.Result doTransaction(MutableData mutableData) {
//
//
//                                            DatabaseReference bookingRefRest = myRefadapter.child("restaurants").child(currentBooking.getRestaurantId()).child(currentBooking.getID());
//                                            bookingRefRest.setValue(null);
//
//                                            return Transaction.success(mutableData);
//                                        }
//
//                                        @Override
//                                        public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
////                                            if (!committed){
////                                            mData.remove(position);
////                                              notifyItemRemoved(position);
////                                            notifyItemRangeChanged(position, mData.size());
////                                            Toast.makeText(context,"eliminato",Toast.LENGTH_LONG).show();
////                                            }
////                                            else
////                                                Toast.makeText(context, R.string.error_delete_offer, Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
                                    /**********************************************************/

                                    mData.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, mData.size());
                                    Toast.makeText(context,"eliminato",Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton(v.getResources().getString(R.string.cancel_dialog_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });


                    Dialog dialog = builder.create();
                    dialog.show();
                }
            });

        }

        public void setData(Booking current, int position, View view) {

            this.position = position;

            restaurantName.setText(current.getRestaurantName());

            ID.setText(current.getUserName());

            nItems.setText(MessageFormat.format("{0} " +view.getResources().getString(R.string.dishes), current.getTotalDishesQty()));

            DecimalFormat df = new DecimalFormat("0.00");

            totalPrice.setText(MessageFormat.format("{0}€", String.valueOf(df.format(current.getTotalPrice()))));


            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(parser.parse(current.getDateTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            hour.setText(MessageFormat.format("{0}:{1}", calendar.get(Calendar.HOUR_OF_DAY),
                    pad(calendar.get(Calendar.MINUTE))));
            date.setText(MessageFormat.format("{0}/{1}/{2}", pad(calendar.get(Calendar.DAY_OF_MONTH)),
                    pad(calendar.get(Calendar.MONTH) + 1), pad(calendar.get(Calendar.YEAR))));

            currentBooking = current;

            if(current.getEvaso())
            {
                trash.setVisibility(View.GONE);
                evadedText.setVisibility(View.VISIBLE);
                cardviewLayout.setBackgroundColor(Color.parseColor("#DDDDDD"));
            }

            // Create a storage reference from our app
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://lab4-insane.appspot.com/restaurants/" + current.getRestaurantId() +
                    "/cover.jpg");

            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'restaurants/myRestaurant/cover.jpg'
                    // Pass it to Glide to download, show in ImageView and caching
//                    Glide.with(context)
//                            .load(uri.toString())
//                            .placeholder(R.drawable.default_img_rest_1)
//                            .centerCrop()
//                            .error(R.drawable.wa_background)
//                            .into(restaurantPhoto);

                    DownloadImageTask dit = new DownloadImageTask();
                    dit.execute(uri, restaurantPhoto);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }


        private String pad(int c) {
            if (c >= 10)
                return String.valueOf(c);
            else
                return "0" + String.valueOf(c);
        }

        public void removeBooking(){
            mData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mData.size());
            Toast.makeText(context,"eliminato",Toast.LENGTH_SHORT).show();
    }


    }

    public class DownloadImageTask extends AsyncTask<Object, Void, Bitmap> {

        private ImageView photo;

        @Override
        protected Bitmap doInBackground(Object... params) {

            this.photo = (ImageView) params[1];
            Bitmap bitmap;
            try {
                bitmap = Glide.
                        with(context).
                        load(params[0].toString()).
                        asBitmap().
                        into(1920,1080). //FIXME x Michele settare dimensioni schermo invece che dimensioni fisse
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

            if(bitmap != null)
                this.photo.setImageBitmap(bitmap);
        }
    }
}
