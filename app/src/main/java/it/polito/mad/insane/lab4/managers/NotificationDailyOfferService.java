package it.polito.mad.insane.lab4.managers;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.activities.RestaurantProfileActivity;
import it.polito.mad.insane.lab4.data.DailyOfferSimple;
import it.polito.mad.insane.lab4.data.Restaurant;

/**
 * Created by carlocaramia on 07/06/16.
 */
public class NotificationDailyOfferService extends IntentService {
    public Context myContext=this;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotificationDailyOfferService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference restaurantsRef = database.getReference("/offers");



        restaurantsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DailyOfferSimple offer=dataSnapshot.getValue(DailyOfferSimple.class);
                Intent dialogIntent = new Intent(myContext, RestaurantProfileActivity.class);
                dialogIntent.putExtra("ID",offer.getRestaurantId());
                dialogIntent.putExtra("Name",offer.getRestaurantName());
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
                Toast.makeText(myContext,"child added",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                DailyOfferSimple offer=dataSnapshot.getValue(DailyOfferSimple.class);
                Intent dialogIntent = new Intent(myContext, RestaurantProfileActivity.class);
                dialogIntent.putExtra("ID",offer.getRestaurantId());
                dialogIntent.putExtra("Name",offer.getRestaurantName());
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
                Toast.makeText(myContext,"child edited",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
