package it.polito.mad.insane.lab4.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.data.Review;

/**
 * Created by miche on 31/05/2016.
 */

public class ReviewsRecyclerAdapter extends RecyclerView.Adapter<ReviewsRecyclerAdapter.ReviewsViewHolder> {

    private List<Review> mData; //actual data to be displayed
    private LayoutInflater mInflater;
    private Context context;

    public ReviewsRecyclerAdapter(Context context, List<Review> data){
        this.mData = data;
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    /**
     * Method called when a ViewHolder Object is created
     * The returned object contains a reference to a view representing the bare structure of the item
     * @param parent
     * @param viewType
     */
    @Override
    public ReviewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create the view starting from XML layout file
        View view = this.mInflater.inflate(R.layout.review_cardview, parent, false);

        ReviewsViewHolder holder = new ReviewsViewHolder(view); // create the holder
        return holder;
    }


    /**
     * Method called after onCreateViewHolder() and it fetches data from the model and properly sets view accordingly
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ReviewsViewHolder holder, int position) {
        Review currentObj = this.mData.get(position);
        holder.setData(currentObj, position);
    }

    @Override
    public int getItemCount() {
        return this.mData.size();
    }

    public class ReviewsViewHolder extends RecyclerView.ViewHolder{

        private View cardView;
        private ImageView userPic;
        private TextView userName;
        private TextView score;
        private TextView title;
        private TextView expandableText;
        private TextView btnSeeMore;
        private TextView date;
        private int position;
        private boolean first = true;
        private boolean expandable = true;
        private ImageView expandArrow;
        private LinearLayout hiddenScoresLayout;
        private LinearLayout headerLayout;

        public ReviewsViewHolder(View itemView) {
            super(itemView);
            this.cardView = itemView;  // non dovrebbe servire
//            this.userPic = (ImageView) itemView.findViewById(R.id.user_pic);
            this.userName = (TextView) itemView.findViewById(R.id.review_user_name);
            this.score = (TextView) itemView.findViewById(R.id.review_score);
            this.title = (TextView) itemView.findViewById(R.id.review_title);
            this.expandableText = (TextView) itemView.findViewById(R.id.review_extendable_text);
            this.btnSeeMore = (TextView) itemView.findViewById(R.id.review_btn_see_more);
            this.date = (TextView) itemView.findViewById(R.id.review_date);
            this.headerLayout = (LinearLayout) itemView.findViewById(R.id.review_header_layout);

            //T-ODO: add the userPic instead of the left drawable of userName in the cardView
        }



        public void setData(Review current, int position){
            this.position = position;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            this.date.setText(dateFormat.format(current.getDate()));
            this.expandableText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if(first){ // here we check if the text is longer than the textview space for the first time. If not, don't need
                        // the button "see more"
                        first = false;
                        if(expandableText.getLineCount() < TextViewCompat.getMaxLines(expandableText))
                            btnSeeMore.setVisibility(View.INVISIBLE);
                    }
                    if(btnSeeMore.getVisibility() != View.INVISIBLE){ //if the text is expandable I set a listener to the button
                        btnSeeMore.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                if (!expandable) { // the textview is large, I want to compress it
                                    expandable = true;
                                    ObjectAnimator animation = ObjectAnimator.ofInt(expandableText, "maxLines", 3);
                                    animation.setDuration(200).start();
                                    btnSeeMore.setText(R.string.see_more);
                                } else { //the textview is compressed, I want to expand it
                                    expandable = false;
                                    ObjectAnimator animation = ObjectAnimator.ofInt(expandableText, "maxLines", 40);
                                    animation.setDuration(200).start();
                                    btnSeeMore.setText(R.string.see_less);
                                }

                            }
                        });
                    }
                }
            });

            this.expandableText.setText(current.getText());
            this.title.setText(current.getTitle());

//            this.userName.setText(users.getUser(current.getUserID())); /**da implementare**/

            DecimalFormat df = new DecimalFormat("0.0");
            this.score.setText(String.valueOf(df.format(current.getFinalScore())));
        }

//        public double roundToHalf(double d) {
//            return Math.round(d * 2) / 2.0;
//        }
    }
}
