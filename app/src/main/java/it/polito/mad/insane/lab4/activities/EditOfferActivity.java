package it.polito.mad.insane.lab4.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.DishArrayAdapter;
import it.polito.mad.insane.lab4.data.DailyOffer;
import it.polito.mad.insane.lab4.data.DailyOfferSimple;
import it.polito.mad.insane.lab4.data.Dish;

public class EditOfferActivity extends AppCompatActivity
{
    static final String PREF_LOGIN = "loginPref";
    private SharedPreferences mPrefs = null;
    private static String rid; // restaurant id
    private static String rName; // restaurant name
    private DishArrayAdapter dishesArrayAdapter = null;
    private EditText price;
    private EditText description;
    private EditText name;
    private DailyOffer currentOffer = null;

    /** Standard methods **/
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_offer_activity);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get restaurant id
        this.mPrefs = getSharedPreferences(PREF_LOGIN, MODE_PRIVATE);
        if (mPrefs != null) {
            rid = this.mPrefs.getString("rid", null);
            rName = this.mPrefs.getString("rName", null);
        }

        price = (EditText) findViewById(R.id.daily_offer_price);
        description = (EditText) findViewById(R.id.daily_offer_description_text);
        name = (EditText)  findViewById(R.id.daily_offer_name);

        // get offer name and ID, if present
        currentOffer = (DailyOffer) getIntent().getSerializableExtra("offer");
        if(currentOffer != null)
        {
            setTitle(currentOffer.getName());
            name.setText(currentOffer.getName());
            price.setText(String.valueOf(currentOffer.getPrice()));
            description.setText(currentOffer.getDescription());
        }


        FloatingActionButton saveOffer = (FloatingActionButton) findViewById(R.id.save_edit_offer);
        if (saveOffer != null)
        {
            saveOffer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int allFilled = 1; // -1 = not filled all field, 0 = warning on price, 1 = filled all field

                    HashMap<Dish,Integer> quantitiesMap = dishesArrayAdapter.getQuantitiesMap();

                    // adding a new offer
                    DailyOffer newOffer = new DailyOffer();
                    // check if the field are empty
                    if (!isEmpty(description) && !isEmpty(price) && !isEmpty(name)) {
                        newOffer.setDescription(description.getText().toString());
                        newOffer.setPrice(Double.parseDouble(price.getText().toString()));
                        newOffer.setName(name.getText().toString());
                    } else
                        allFilled = -1;

                    if (allFilled != -1) {

                        double totalPrice = 0;
                        // take dishes quantities and put in a new map <IdDish, quantities>
                        HashMap<String, Integer> dishesIdMap = new HashMap<String, Integer>();
                        for (Map.Entry<Dish, Integer> entry : quantitiesMap.entrySet()) {
                            if (entry.getValue() > 0) { // dish quantity > 0
                                dishesIdMap.put(entry.getKey().getID(), entry.getValue());
                                totalPrice += entry.getValue() * entry.getKey().getPrice();
                            }
                        }
                        // check if there are some dishes in the offer
                        if (!dishesIdMap.isEmpty()) {
                            newOffer.setDishesIdMap(dishesIdMap);

                            //check the price
                            if (newOffer.getPrice() <= totalPrice)
                                newOffer.setDiscount(totalPrice - newOffer.getPrice());
                            else
                                allFilled = 0;
                        } else
                            allFilled = -1;
                    }

                    if (allFilled == 1)
                    {
                        if(currentOffer == null)
                        {
                            // save the new offer in firebase
                            newOffer.setID(null);
                        }else
                        {
                            // overwrite the current offer on firebase
                            newOffer.setID(currentOffer.getID());
                        }
                        addOfferInFirebase(newOffer);
                        finish();
                    } else if (allFilled == 0)
                        Toast.makeText(EditOfferActivity.this, R.string.warning_price_daily_offer, Toast.LENGTH_LONG).show();
                    else if (allFilled == -1)
                        Toast.makeText(EditOfferActivity.this, R.string.error_input_new_daily_offer, Toast.LENGTH_SHORT).show();

                }
            });
        }

        // get dish list from firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/restaurants/" + rid + "/dishMap");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                HashMap<String, Dish> dishesMap = dataSnapshot.getValue(new GenericTypeIndicator<HashMap<String, Dish>>() {
                    @Override
                    protected Object clone() throws CloneNotSupportedException {
                        return super.clone();
                    }
                });
                if (dishesMap!=null)
                {
                    HashMap<Dish, Integer> adapterDishesMap = new HashMap<Dish, Integer>();

                    // create a new map with all quantities = 0
                    for (Dish d : dishesMap.values())
                        adapterDishesMap.put(d, 0);

                    if(currentOffer != null)
                    {
                        // set the correct quantities for the dished saved in the currenDailyOffer
                        for (Dish d : dishesMap.values())
                        {
                            for (Map.Entry<String, Integer> entryCurrentOffer : currentOffer.getDishesIdMap().entrySet())
                                if (entryCurrentOffer.getKey().equals(d.getID())) {
                                    adapterDishesMap.put(d, entryCurrentOffer.getValue());
                                    break;
                                }
                        }
                    }

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
                deleteOffer(currentOffer);
                // TODO aggiungi alert dialog di conferma (Michele)
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
    private boolean isEmpty(EditText tv)
    {
        if(tv.getText().toString().trim().length() > 0)
            return  false;
        else
            return true;

    }

    private void addOfferInFirebase(final DailyOffer offer)
    {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference restaurantRef = database.getReference("/restaurants/"+rid+"/");


        if(offer.getID() == null)
        {
            // adding new offer
            restaurantRef.runTransaction(new Transaction.Handler() {

                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    DatabaseReference dailyOffersRef = restaurantRef.child("dailyOfferMap");
                    DatabaseReference newRef = dailyOffersRef.push();
                    String key = newRef.getKey();
                    offer.setID(key);
                    newRef.setValue(offer);


                    // insert simple daily offer in /offers
                    DailyOfferSimple simpleOffer = new DailyOfferSimple();
                    simpleOffer.setID(offer.getID());
                    simpleOffer.setDescription(offer.getDescription());
                    simpleOffer.setRestaurantId(rid);
                    simpleOffer.setRestaurantName(rName);
                    DatabaseReference offersRef = database.getReference("/offers/"+key);
                    offersRef.setValue(simpleOffer);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot)
                {
                    if(!committed)
                        Toast.makeText(EditOfferActivity.this, R.string.confirm_add_offer, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(EditOfferActivity.this, R.string.error_add_offer, Toast.LENGTH_SHORT).show();

                }
            });
        }else
        {
            // overwrite existing offer
            restaurantRef.runTransaction(new Transaction.Handler() {

                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    DatabaseReference dailyOffersRef = restaurantRef.child("dailyOfferMap").child(offer.getID());
                    dailyOffersRef.setValue(offer);


                    // insert simple daily offer in /offers
                    DailyOfferSimple simpleOffer = new DailyOfferSimple();
                    simpleOffer.setID(offer.getID());
                    simpleOffer.setDescription(offer.getDescription());
                    simpleOffer.setRestaurantId(rid);
                    simpleOffer.setRestaurantName(rName);
                    DatabaseReference offersRef = database.getReference("/offers/"+offer.getID());
                    offersRef.setValue(simpleOffer);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot)
                {
                    if(!committed)
                        Toast.makeText(EditOfferActivity.this, R.string.confirm_edit_offer, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(EditOfferActivity.this, R.string.error_edit_offer, Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    private void deleteOffer(final DailyOffer offer) {
        if (currentOffer == null) {
            Toast.makeText(EditOfferActivity.this, R.string.cant_delete_offer, Toast.LENGTH_SHORT).show();
            return;
        }

        // remove from local cache in recyclerAdapter
        DailyMenuActivity.removeOffer(offer);


        // remove from firebase
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference restaurantRef = database.getReference("/restaurants/" + rid + "/");
        // adding new offer
        restaurantRef.runTransaction(new Transaction.Handler() {

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                DatabaseReference dailyOffersRef = restaurantRef.child("dailyOfferMap").child(offer.getID());
                dailyOffersRef.setValue(null);


                // delete simple daily offer in /offers
                DatabaseReference offersRef = database.getReference("/offers/" + offer.getID());
                offersRef.setValue(null);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (!committed)
                    Toast.makeText(EditOfferActivity.this, R.string.confirm_delete_offer, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(EditOfferActivity.this, R.string.error_delete_offer, Toast.LENGTH_SHORT).show();

            }
        });
        finish();
    }
}