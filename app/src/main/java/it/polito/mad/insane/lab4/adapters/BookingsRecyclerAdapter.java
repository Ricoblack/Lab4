package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.activities.ViewBookingActivity;
import it.polito.mad.insane.lab4.data.Booking;

/**
 * Created by Federico on 02/06/2016.
 */
public class BookingsRecyclerAdapter extends RecyclerView.Adapter<BookingsRecyclerAdapter.BookingViewHolder>
{
    private List<Booking> mData; // actual data to be displayed
    private LayoutInflater mInflater;
    private Context myContext;

    public BookingsRecyclerAdapter(Context context, List<Booking> data)
    {
        this.mData = data;
        this.mInflater = LayoutInflater.from(context);
        this.myContext=context;
    }

    @Override
    public BookingsRecyclerAdapter.BookingViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.booking_cardview, parent, false);
        BookingViewHolder holder = new BookingViewHolder(view,myContext);
        return  holder;
    }

    @Override
    public void onBindViewHolder(BookingsRecyclerAdapter.BookingViewHolder holder, int position)
    {
        Booking currentObj = mData.get(position);
        holder.setData(currentObj,position);
    }

    @Override
    public int getItemCount()
    {
        return this.mData.size();
    }

    public List<Booking> getMData(){
        return this.mData;
    }

    /* Our Holder Class */
    class BookingViewHolder extends RecyclerView.ViewHolder
    {
        private Context myContext;
        private View cardView;
        private TextView bookingID;
        private TextView bookingTime;
        private TextView bookingDishNum;
        //private TextView bookingNote;
        private int position;
        private Booking currentBooking;
        private TextView imageView;

        private android.view.View.OnClickListener cardViewListener = new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                //Toast.makeText(v.getContext(),"Cliccato sulla cardView", Toast.LENGTH_LONG).show();
                Intent i = new Intent(v.getContext(), ViewBookingActivity.class);
                i.putExtra("Booking", BookingViewHolder.this.currentBooking);
                v.getContext().startActivity(i);
            }
        };

        private android.view.View.OnClickListener imageViewListener = new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.dialog_dialog_delete);
                // Add the buttons
                builder.setPositiveButton(R.string.ok_delete_dialog, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        //final DatabaseReference myRefadapter = database.getReference("/bookings/");
                        DatabaseReference myRefadapter = database.getReference("/bookings/users/"+currentBooking.getUserId()+"/"+currentBooking.getID());

                        //setto il valor "evaso" della booking a true e la memorizzo nel db, non eliminandola
                        currentBooking.setEvaso(true);

                        myRefadapter.setValue(currentBooking, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            }
                        });

                        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
                        //final DatabaseReference myRefadapter = database.getReference("/bookings/");
                        DatabaseReference myRefadapter2 = database.getReference("/bookings/restaurants/"+currentBooking.getRestaurantId()+"/"+currentBooking.getID());

                        myRefadapter2.setValue(currentBooking, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            }
                        });

                        mData.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, mData.size());
                    }
                });
                builder.setNegativeButton(R.string.cancel_delete_dialog, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                    }
                });

                // Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();

//               RestaurateurJsonManager manager = RestaurateurJsonManager.getInstance(myContext);
//                for(Booking  b: manager.getBookings()){
//                    if(b.getID().equals(currentBooking.getID())){
//                        Toast.makeText(v.getContext(),v.getResources().getString(R.string.confirm_delete_booking)+" #"+currentBooking.getID(), Toast.LENGTH_LONG).show();
//                        manager.getBookings().remove(b);
//                        mData.remove(b);
//                        notifyItemRemoved(position);
//                        notifyItemRangeRemoved(position, getItemCount());
//                        break;
//                    }
//                }
//                manager.saveDbApp();

            }
        };


        public BookingViewHolder(View itemView, Context myContext)
        {
            super(itemView);
            this.myContext=myContext;
            this.cardView = itemView;
            this.imageView = (TextView) itemView.findViewById(R.id.delete_booking_button);
            //data of the booking class
            this.bookingID = (TextView) itemView.findViewById(R.id.home_restaurateur_username_cardview);
            this.bookingTime = (TextView) itemView.findViewById(R.id.home_restaurateur_hour_cardview);
            this.bookingDishNum = (TextView) itemView.findViewById(R.id.num_booking);

            // set the onClickListener to the View
            this.cardView.setOnClickListener(cardViewListener);

            //set the onClickListner to the ImgView
            this.imageView.setOnClickListener(imageViewListener);

        }

        public void setData(Booking current, int position)
        {
            this.bookingID.setText(current.getUserName());

            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            String splitted[] = current.getDateTime().split(" ");
            this.bookingTime.setText(splitted[1]);
            int qty = 0;
            if (current.getDishesIdMap() != null)
                qty += current.getDishesIdMap().size();
            if (current.getDailyOffersIdMap() != null)
                qty += current.getDailyOffersIdMap().size();
            this.bookingDishNum.setText(String.valueOf(qty));

            this.position = position;
            this.currentBooking = current;

        }


        public int getPos() {
            return position;
        }

        public void setPos(int position) {
            this.position = position;
        }

        public Booking getCurrentBooking() {
            return currentBooking;
        }

        public void setCurrentBooking(Booking currentBooking) {
            this.currentBooking = currentBooking;
        }
    }
}
