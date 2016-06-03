package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

/**
 * Created by miche on 03/06/2016.
 */
public class DailyOfferRecyclerAdapter extends RecyclerView.Adapter<DailyOfferRecyclerAdapter.DailyOfferHolder>
{

    private final Context context;
    private LayoutInflater mInflater;
    private List<DailyOffer> mData; // actual data to be displayed

    public DailyOfferRecyclerAdapter(Context context, List < DailyOffer > data)
    {
        this.context = context;
        this.mData = data;
        this.mInflater = LayoutInflater.from(context);
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
        private int position;
        private DailyOffer currentDailyOffer;
        private TextView dailyOfferName;
        private TextView dailyOfferID;
        private TextView dailyOfferPrice;
        private TextView dailyOfferDescription;

        private View.OnClickListener cardViewListener = new View.OnClickListener()
        {

            @Override
            public void onClick(View v) //TODO: implementare che i dettagli della offerta siano visualizzati per espansione (michele)
            {
                Toast.makeText(context,"Hai premuto sulla cardview della daily offer con ID: "+currentDailyOffer.getID(), Toast.LENGTH_SHORT).show();
//                Intent i = new Intent(v.getContext(), .class);
//                i.putExtra("DailyOffer", DailyOfferHolder.this.currentDailyOffer);
//                //i.putExtra("nameRestaurant", nameRist);
//                v.getContext().startActivity(i);
            }
        };
        public DailyOfferHolder(View itemView) {
            super(itemView);
            this.manager = RestaurateurJsonManager.getInstance(null);
            this.dailyOfferName = (TextView) itemView.findViewById(R.id.daily_offer_name);
            this.dailyOfferID = (TextView) itemView.findViewById(R.id.daily_offer_ID);
            this.dailyOfferPrice = (TextView) itemView.findViewById(R.id.daily_offer_price);
            this.dailyOfferDescription = (TextView) itemView.findViewById(R.id.daily_offer_description);
            this.cardView = itemView;
            this.cardView.setOnClickListener(cardViewListener);

        }

        public void setData(DailyOffer current, int position, View view)
        {
            this.position = position;
            this.currentDailyOffer = current;
            RestaurateurJsonManager manager = RestaurateurJsonManager.getInstance(context);

            this.dailyOfferID.setText(current.getID());
            this.dailyOfferName.setText(current.getName());
            this.dailyOfferDescription.setText(current.getDescription());
            DecimalFormat df = new DecimalFormat("0.00");
            this.dailyOfferPrice.setText(MessageFormat.format("{0}€", String.valueOf( df.format(current.getPrice()))));
        }

    }
}
