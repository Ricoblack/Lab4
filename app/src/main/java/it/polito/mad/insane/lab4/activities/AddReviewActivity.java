package it.polito.mad.insane.lab4.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.AddReviewSpinnerAdapter;
import it.polito.mad.insane.lab4.data.Restaurant;

public class AddReviewActivity extends AppCompatActivity {

    private static final int N_SCORES = 3;
    private static double reviewScores[];
    private static double finalScore = -1;
    private static String restaurantId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_review_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle bundle = getIntent().getExtras();
        restaurantId = bundle.getString("ID");

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
                        int total = 0;
                        for(double d : reviewScores){
                            if(d == -1){
                                total = -1;
                                break;
                            }
                            else
                                total += d;
                        }
                        if(total != -1) {
                            finalScore = total / N_SCORES;

                            LinearLayout ll = (LinearLayout) findViewById(R.id.final_score_layout);
                            TextView tvHint = (TextView) findViewById(R.id.add_review_hint);
                            TextView tvScore = (TextView) findViewById(R.id.add_review_final_score);
                            if (tvHint != null && ll != null && tvScore != null) {
                                tvHint.setVisibility(View.GONE);
                                ll.setVisibility(View.VISIBLE);
                                tvScore.setText(String.valueOf(finalScore));
                            }

                        }
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
                        int total = 0;
                        for(double d : reviewScores){
                            if(d == -1){
                                total = -1;
                                break;
                            }
                            else
                                total += d;
                        }
                        if(total != -1) {
                            finalScore = total / N_SCORES;

                            LinearLayout ll = (LinearLayout) findViewById(R.id.final_score_layout);
                            TextView tvHint = (TextView) findViewById(R.id.add_review_hint);
                            TextView tvScore = (TextView) findViewById(R.id.add_review_final_score);
                            if (tvHint != null && ll != null && tvScore != null) {
                                tvHint.setVisibility(View.GONE);
                                ll.setVisibility(View.VISIBLE);
                                tvScore.setText(String.valueOf(finalScore));
                            }

                        }
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
                        int total = 0;
                        for(double d : reviewScores){
                            if(d == -1){
                                total = -1;
                                break;
                            }
                            else
                                total += d;
                        }
                        if(total != -1) {
                            finalScore = total / N_SCORES;

                            LinearLayout ll = (LinearLayout) findViewById(R.id.final_score_layout);
                            TextView tvHint = (TextView) findViewById(R.id.add_review_hint);
                            TextView tvScore = (TextView) findViewById(R.id.add_review_final_score);
                            if (tvHint != null && ll != null && tvScore != null) {
                                tvHint.setVisibility(View.GONE);
                                ll.setVisibility(View.VISIBLE);
                                tvScore.setText(String.valueOf(finalScore));
                            }

                        }
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
                    if(finalScore == -1)
                        Toast.makeText(AddReviewActivity.this, R.string.rate_all_section, Toast.LENGTH_SHORT).show();
                    //TODO gestire la questione del titolo e del testo (Renato)
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddReviewActivity.this);
                        builder.setTitle(AddReviewActivity.this.getResources().getString(R.string.alert_title_review))
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        saveReview();
                                        clearStaticVariables();
                                        finish();
                                        Toast.makeText(getApplicationContext(), getString(R.string.add_review_success), Toast.LENGTH_SHORT).show();
                                        //FIXME se si fa in tempo creare activity MyReviews (Renato)
                                        Intent intent = new Intent(AddReviewActivity.this, RestaurantProfileActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("ID", restaurantId);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });

                        Dialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void clearStaticVariables() {
        finalScore = -1;
        reviewScores = null;
        restaurantId = null;
    }

    private void saveReview() {
        //TODO aggiungere la review al db e calcolare il nuovo punteggio del ristorante (Renato)
    }
}
