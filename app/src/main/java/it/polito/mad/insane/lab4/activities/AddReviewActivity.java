package it.polito.mad.insane.lab4.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.AddReviewSpinnerAdapter;
import it.polito.mad.insane.lab4.data.Review;

public class AddReviewActivity extends AppCompatActivity {

    private static final int N_SCORES = 3;
    static final String PREF_LOGIN = "loginPref";
    private static double reviewScores[];
    private static String reviewTitle;
    private static String reviewText;
    private static double reviewFinalScore = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_review_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.add_review_activity_title));

        Bundle bundle = getIntent().getExtras();
        final String restaurantId = bundle.getString("ID");
        final HashMap<String, Double> restaurantScoresMap = (HashMap<String, Double>) bundle.getSerializable("scoresMap");
        final double restaurantFinalScore = bundle.getDouble("finalScore");
        final double reviewsNumber = bundle.getInt("reviewsNumber");

        reviewScores = new double[N_SCORES];
        Arrays.fill(reviewScores, -1);

        List<String> scores = new ArrayList<>();
        Resources res = getResources();
        final String[] scoreStrings = res.getStringArray(R.array.scores_array);
        Collections.addAll(scores, scoreStrings);
        AddReviewSpinnerAdapter adapter = new AddReviewSpinnerAdapter(AddReviewActivity.this, R.layout.support_simple_spinner_dropdown_item,
                scores);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        Spinner scoreSpinner = (Spinner) findViewById(R.id.score1_spinner);
        if (scoreSpinner != null) {
            scoreSpinner.setAdapter(adapter);
            scoreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String s = String.valueOf(parent.getItemAtPosition(position));
                    if(!s.equals(scoreStrings[0])) {
                        reviewScores[0] = (Double.parseDouble(s));
                        checkAllVotes();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        scoreSpinner = (Spinner) findViewById(R.id.score2_spinner);
        if (scoreSpinner != null) {
            scoreSpinner.setAdapter(adapter);
            scoreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String s = String.valueOf(parent.getItemAtPosition(position));
                    if(!s.equals(scoreStrings[0])) {
                        reviewScores[1] = (Double.parseDouble(s));
                        checkAllVotes();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        scoreSpinner = (Spinner) findViewById(R.id.score3_spinner);
        if (scoreSpinner != null) {
            scoreSpinner.setAdapter(adapter);
            scoreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String s = String.valueOf(parent.getItemAtPosition(position));
                    if(!s.equals(scoreStrings[0])) {
                        reviewScores[2] = (Double.parseDouble(s));
                        checkAllVotes();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (reviewFinalScore == -1) // se non ho settato tutti i punteggi
                        Toast.makeText(AddReviewActivity.this, R.string.rate_all_section, Toast.LENGTH_LONG).show();
                    else {
                        EditText etTitle = (EditText) findViewById(R.id.add_review_title);
                        final String title = String.valueOf(etTitle.getText());
                        EditText etText = (EditText) findViewById(R.id.add_review_text);
                        final String text = String.valueOf(etText.getText());

                        if (!text.equals("") && title.equals("")) {
                            //se ho settato il testo ma non il titolo della recensione
                            Toast.makeText(AddReviewActivity.this, R.string.hint_review_title, Toast.LENGTH_LONG).show();
                        }
                        else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AddReviewActivity.this);
                            if (String.valueOf(etText.getText()).equals("")) // sto inserendo una recensione senza commento
                                builder.setTitle(AddReviewActivity.this.getResources().getString(R.string.alert_title_review_notext));
                            else //sto inserendo una recensione con commento
                                builder.setTitle(AddReviewActivity.this.getResources().getString(R.string.alert_title_review));

                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if( !text.equals("") ){
                                        reviewText = text;
                                        reviewTitle = title;
                                    }
                                    saveReview(restaurantId, restaurantFinalScore, reviewsNumber, restaurantScoresMap);
                                    finish();
                                    clearStaticVariables();
                                    Toast.makeText(getApplicationContext(), getString(R.string.add_review_success), Toast.LENGTH_SHORT).show();
                                    //FIXME se si fa in tempo creare activity MyReviews (Renato)
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                            Dialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                }
            });
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void clearStaticVariables() {
        reviewFinalScore = -1;
        reviewScores = null;
        reviewTitle = null;
        reviewText = null;
    }

    private void checkAllVotes(){
        double total = 0;
        for(double d : reviewScores){
            if(d == -1){
                total = -1;
                break;
            }
            else
                total += d;
        }
        if(total != -1) {
            reviewFinalScore = total / N_SCORES;

            LinearLayout ll = (LinearLayout) findViewById(R.id.final_score_layout);
            TextView tvHint = (TextView) findViewById(R.id.add_review_hint);
            TextView tvScore = (TextView) findViewById(R.id.add_review_final_score);
            if (tvHint != null && ll != null && tvScore != null) {
                tvHint.setVisibility(View.GONE);
                ll.setVisibility(View.VISIBLE);
                DecimalFormat df = new DecimalFormat("0.0");
                tvScore.setText(String.valueOf(df.format(reviewFinalScore)));
            }

        }
    }

    private void saveReview(final String restaurantId, double restaurantFinalScore, double reviewsNumber, HashMap<String, Double> restaurantScoresMap) {

        //CREO L'OGGETTO REVIEW
        final Review r = new Review();

        r.setAvgFinalScore(reviewFinalScore);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        r.setDateTime(sdf.format(cal.getTime()));
        r.setRestaurantId(restaurantId);

        HashMap<String, Double> scoresMap = new HashMap<>();
        scoresMap.put(getResources().getString(R.string.first_score), reviewScores[0]);
        scoresMap.put(getResources().getString(R.string.second_score), reviewScores[1]);
        scoresMap.put(getResources().getString(R.string.third_score), reviewScores[2]);
        r.setScoresMap(scoresMap);

        if( reviewText != null )
            r.setText(reviewText);
        else
            r.setText("");

        if(reviewTitle != null)
            r.setTitle(reviewTitle);
        else
            r.setTitle("");

        SharedPreferences mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        if (mPrefs != null) {
            r.setUserId(mPrefs.getString("uid", null));
            r.setUsername(mPrefs.getString("uName", null));
        }

        //AGGIORNO L'OGGETTO RISTORANTE
        double updateScore;
        HashMap<String, Double> updateMap;
        if(restaurantFinalScore != -1) {
            updateScore = restaurantFinalScore * reviewsNumber;
            updateScore = (updateScore + reviewFinalScore) / (reviewsNumber + 1);

            updateMap = restaurantScoresMap;

            double temp = updateMap.get(getResources().getString(R.string.first_score)) * reviewsNumber;
            updateMap.put(getResources().getString(R.string.first_score), ((temp + reviewScores[0]) / (reviewsNumber + 1)));

            temp = updateMap.get(getResources().getString(R.string.second_score)) * reviewsNumber;
            updateMap.put(getResources().getString(R.string.second_score), ((temp + reviewScores[1]) / (reviewsNumber +1)));

            temp = updateMap.get(getResources().getString(R.string.third_score)) * reviewsNumber;
            updateMap.put(getResources().getString(R.string.third_score), ((temp + reviewScores[2]) / (reviewsNumber +1)));
        }
        else{
            updateScore = reviewFinalScore;
            updateMap = scoresMap;
        }

        //AGGIORNO IL DB
        final double finalUpdateScore = updateScore;
        final HashMap<String, Double> finalUpdateMap = updateMap;

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference addReviewRef = database.getReference("/reviews");

        addReviewRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                DatabaseReference restaurantBookingsRef = addReviewRef.child("restaurants").child(restaurantId);
                DatabaseReference pushRef = restaurantBookingsRef.push();
                String key = pushRef.getKey();
                r.setID(key);
                pushRef.setValue(r);

                DatabaseReference userBookingRef = addReviewRef.child("users").child(r.getUserId()).child(key);
                userBookingRef.setValue(r);

                //TODO aggiornare i punteggi del ristorante
                DatabaseReference restaurantRef = database.getReference("/restaurants/" + restaurantId);

                restaurantRef.child("avgFinalScore").setValue(finalUpdateScore);
                restaurantRef.child("avgScores").setValue(finalUpdateMap);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                // FIXME non entra qui dentro anche se ha salvato tutte le modifiche sul DB
//                finish();
//                clearStaticVariables();
//                Toast.makeText(getApplicationContext(), getString(R.string.add_review_success), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
