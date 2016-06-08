package it.polito.mad.insane.lab4.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.DishArrayAdapter;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.data.Dish;

public class EditOfferActivity extends AppCompatActivity
{

    // TODO x FEDE: sistemare la grafica delle dish_checkable_listview_item


    static final String PREF_LOGIN = "loginPref";
    private SharedPreferences mPrefs = null;
    private static String rid; // restaurant id
    private DishArrayAdapter dishesArrayAdapter = null;
    private TextView price;
    private TextView description;



    /** Standard methods **/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_offer_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        price = (TextView) findViewById(R.id.daily_offer_price);
        description = (TextView) findViewById(R.id.daily_offer_description);

        FloatingActionButton saveOffer = (FloatingActionButton) findViewById(R.id.save_edit_offer);
        if (saveOffer != null) {
            saveOffer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: implementa il salvataggio dell'offerta (Michele)
                    HashMap<Dish,Integer> quantitiesMap = dishesArrayAdapter.getQuantitiesMap();
                    DailyOffer newOffer = new DailyOffer();
//                    newOffer.setDescription();

                            //controlla che la lista di piatti nella dailyoffer non sia vuota e nel caso manda un toast di errore
                }
            });
        }

        // get restaurant id
        this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        if (mPrefs != null) {
            rid = this.mPrefs.getString("rid", null);
        }

        // get dish list from firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/restaurants/" + rid + "/dishMap");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Dish> dishesMap = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Dish>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });
                if (dishesMap!=null)
                {
                    // create a new map with all quantities = 0
                    HashMap<Dish, Integer> adapterDishesMap = new HashMap<Dish, Integer>();
                    for(Dish d : dishesMap.values())
                        adapterDishesMap.put(d, 0);

                    // set the adapter with this map
                    dishesArrayAdapter = new DishArrayAdapter(EditOfferActivity.this, R.layout.dish_checkable_listview_item, adapterDishesMap, 3);
                    ListView dishesListView = (ListView) findViewById(R.id.offer_checkable_listview);
                    dishesListView.setAdapter(dishesArrayAdapter);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_offer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.delete_dish:
                //deleteOffer(this.currentOffer.getID());
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);


    }

    /** Our Methods **/
    /**
     * Method that check if all the field of the activity are filled
     * @return
     */
    private boolean isEmpty(TextView tv)
    {
        if(tv.getText().toString().trim().length() > 0)
            return  false;
        else
            return true;

    }

}
