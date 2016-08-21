package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.FirebaseException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.util.List;

import it.polito.mad.insane.lab4.activities.RestaurantProfileActivity;
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

import it.polito.mad.insane.lab4.R;

/**
 * Created by Renato on 31/05/2016.
 */
public class RestaurantsRecyclerAdapter extends RecyclerView.Adapter<RestaurantsRecyclerAdapter.RestaurantsViewHolder> {

    private List<Restaurant> mData;
    private LayoutInflater mInflater;
    private RestaurateurJsonManager manager;

    //costruttore
    public RestaurantsRecyclerAdapter(Context context, List<Restaurant> data) {
        this.mData = data;
        this.mInflater = LayoutInflater.from(context);
        this.manager=RestaurateurJsonManager.getInstance(context);
    }

    /**
     * Method called when a ViewHolder Object is created
     * The returned object contains a reference to a view representing the bare structure of the item
     * @param parent
     * @param viewType
     */
    @Override
    public RestaurantsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create the view starting from XML layout file
        View view = this.mInflater.inflate(R.layout.home_restaurant_cardview,parent, false);

        RestaurantsViewHolder result = new RestaurantsViewHolder(view); // create the holder
        return result;
    }

    /**
     * Method called after onCreateViewHolder() and it fetches data from the model and properly sets view accordingly
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RestaurantsViewHolder holder, int position) {
        Restaurant currentObj = this.mData.get(position);
        holder.setData(currentObj, position);

    }

    @Override
    public int getItemCount() {
        return this.mData.size();
    }

    public class RestaurantsViewHolder extends RecyclerView.ViewHolder {

        private View cardView;
        private TextView title;
        private TextView street;
        private String IDrestaurant;
        private String nameRestaurant;
        private ImageView img;
        private TextView typeCausine;
        private TextView avgFinalScore;
        private TextView numReview;
        private TextView distance;
        private int position;

        private android.view.View.OnClickListener cardViewListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Toast.makeText(v.getContext(),"Cliccato sulla cardView", Toast.LENGTH_LONG).show();
                Intent i = new Intent(v.getContext(), RestaurantProfileActivity.class);
                i.putExtra("ID",IDrestaurant);
                i.putExtra("Name", nameRestaurant);
                v.getContext().startActivity(i);
            }
        };

        public RestaurantsViewHolder(View itemView) {

            super(itemView);
            this.cardView = itemView;
            this.title = (TextView) itemView.findViewById(R.id.restaurant_title);
            this.street = (TextView) itemView.findViewById(R.id.street_restaurant);
            this.img = (ImageView) itemView.findViewById(R.id.image_restaurant);
            this.typeCausine = (TextView) itemView.findViewById(R.id.type_cusine);
            this.avgFinalScore = (TextView) itemView.findViewById(R.id.review_finalscore);
            this.numReview = (TextView) itemView.findViewById(R.id.num_reviews);
            this.distance=(TextView) itemView.findViewById(R.id.distance_restaurant);

            // set the onClickListener to the View
            this.cardView.setOnClickListener(cardViewListener);
        }

        public void setData(Restaurant current, int position) {
            this.position = position;
            this.title.setText(current.getInfo().getRestaurantName());
            this.street.setText(current.getInfo().getAddress());
            this.IDrestaurant = current.getID();
            this.nameRestaurant = current.getInfo().getRestaurantName();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                this.img.setImageAlpha(180);//range 0..255
            }
            this.typeCausine.setText(current.getInfo().getCuisineType());
            DecimalFormat df = new DecimalFormat("0.0");
            if(current.getAvgFinalScore() == -1)
                this.avgFinalScore.setText("N/A");
            else
                this.avgFinalScore.setText(df.format(current.getAvgFinalScore()));
            this.numReview.setText(String.valueOf(current.getReviewsNumber()));

            //T-ODO calcolare la distanza
//            float distance = current.getLocation().distanceTo(manager.getLocation());
//            this.distance.setText(String.format("%.0f",distance)+"m");
            this.distance.setVisibility(View.GONE);

            //set restaurants image with picasso
            //set up restaurant images (DEBUG)

            // Create a storage reference from our app
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://lab4-insane.appspot.com/prova/foto_piccolo.jpg");
            // Create a reference with an initial file path and name
            //start download of image
            final long ONE_MEGABYTE = 1024 * 1024;
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed

                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Bitmap bmpimg = Bitmap.createScaledBitmap(bmp, img.getWidth(), img.getHeight(), true);
                    img.setImageBitmap(bmpimg);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    FirebaseException fbe=(FirebaseException) exception;

                }
            });
        }
    }
}
