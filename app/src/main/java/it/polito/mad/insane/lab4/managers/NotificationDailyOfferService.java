package it.polito.mad.insane.lab4.managers;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
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
import it.polito.mad.insane.lab4.activities.HomePageActivity;
import it.polito.mad.insane.lab4.activities.RestaurantProfileActivity;
import it.polito.mad.insane.lab4.data.DailyOfferSimple;
import it.polito.mad.insane.lab4.data.Restaurant;

/**
 * Created by carlocaramia on 07/06/16.
 */
public class NotificationDailyOfferService extends Service {
    private static final int CHILD_ADDED = 0;
    private static final int CHILD_MODIFIED = 1;
    static final String PREF_NAME = "myPrefNotification";
    private SharedPreferences mPrefs = null;

    public Context myContext=this;
    private ChildEventListener addedListener;
    ChildEventListener changedListener;
    DatabaseReference offersRef;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        offersRef = database.getReference("/offers");




        addedListener=new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                DailyOfferSimple offer=dataSnapshot.getValue(DailyOfferSimple.class);

                //check se ho già mostrato l'ultima aggiunta (se il service è ripartito) ed evito di rimostrarla
                //firebase al primo lancio mi notifica tutto
                mPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

                String lastAddedId=mPrefs.getString("lastAddedId","");
                if(lastAddedId.equals(offer.getID())==false) {
                    //nuova offerta aggiunta
                    mPrefs.edit().putString("lastAddedId",offer.getID()).commit();
                    showNotification(offer, CHILD_ADDED);
                    addOfferToSharedPreferences(offer);
                }



            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

               // DailyOfferSimple offer=dataSnapshot.getValue(DailyOfferSimple.class);

                //gli eventi di cambiamento vengono correttamente lanciati solo al cambio dallo stato attuale, quindi
                //lascio la notifica ogni volta

                /*
                //check se ho già mostrato l'ultimo change
                mPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

                String lastChangedId=mPrefs.getString("lastChangedId","");
                if(lastChangedId.equals(offer.getID())==false) {
                    //nuova offerta modificata
                    mPrefs.edit().putString("lastChangedId",offer.getID()).commit();

                }
                */
                //showNotification(offer, CHILD_MODIFIED);


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
        };

        changedListener=new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                /*
                DailyOfferSimple offer=dataSnapshot.getValue(DailyOfferSimple.class);

                //check se ho già mostrato l'ultima aggiunta (se il service è ripartito) ed evito di rimostrarla
                //firebase al primo lancio mi notifica tutto
                mPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

                String lastAddedId=mPrefs.getString("lastAddedId","");
                if(lastAddedId.equals(offer.getID())==false) {
                    //nuova offerta aggiunta
                    mPrefs.edit().putString("lastAddedId",offer.getID()).commit();
                    showNotification(offer, CHILD_ADDED);
                }
                */
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                 DailyOfferSimple offer=dataSnapshot.getValue(DailyOfferSimple.class);

                //gli eventi di cambiamento vengono correttamente lanciati solo al cambio dallo stato attuale, quindi
                //lascio la notifica ogni volta

                /*
                //check se ho già mostrato l'ultimo change
                mPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

                String lastChangedId=mPrefs.getString("lastChangedId","");
                if(lastChangedId.equals(offer.getID())==false) {
                    //nuova offerta modificata
                    mPrefs.edit().putString("lastChangedId",offer.getID()).commit();

                }
                */
                showNotification(offer, CHILD_MODIFIED);
                addOfferToSharedPreferences(offer);

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
        };


        offersRef.limitToLast(1).addChildEventListener(addedListener);
        offersRef.addChildEventListener(changedListener);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        offersRef.removeEventListener(changedListener);
        offersRef.removeEventListener(addedListener);
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    private void showNotification(DailyOfferSimple offer, int type){


        if(type==CHILD_ADDED){
            //child added
            NotificationCompat.Builder mBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(myContext)
                            .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_pressed)  //TODO inventare icona notifica (carlo)
                            .setContentTitle(getResources().getText(R.string.new_offer))
                            .setContentText(offer.getDescription())
                            .setAutoCancel(true);
            Intent resultIntent = new Intent(myContext, RestaurantProfileActivity.class);
            resultIntent.putExtra("ID",offer.getRestaurantId());
            resultIntent.putExtra("Name",offer.getRestaurantName());

            // Because clicking the notification opens a new ("special") activity, there's
            // no need to create an artificial back stack.
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            myContext,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );


            mBuilder.setContentIntent(resultPendingIntent);

            // Sets an ID for the notification (inutile)
            int mNotificationId = 001;
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            Notification notif=mBuilder.build();
            notif.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

            mNotifyMgr.notify(mNotificationId, notif);


        }
        else if(type==CHILD_MODIFIED){
            //child modified
            NotificationCompat.Builder mBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(myContext)
                            .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_pressed)  //TODO inventare icona notifica (carlo)
                            .setContentTitle(getResources().getText(R.string.offer_edited))
                            .setContentText(offer.getDescription())
                            .setAutoCancel(true);
            Intent resultIntent = new Intent(myContext, RestaurantProfileActivity.class);
            resultIntent.putExtra("ID",offer.getRestaurantId());
            resultIntent.putExtra("Name",offer.getRestaurantName());

            // Because clicking the notification opens a new ("special") activity, there's
            // no need to create an artificial back stack.
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            myContext,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );


            mBuilder.setContentIntent(resultPendingIntent);


            // Sets an ID for the notification
            int mNotificationId = 123456789;
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Builds the notification and issues it.

            Notification notif=mBuilder.build();
            notif.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

            mNotifyMgr.notify(mNotificationId, notif);



        }



    }
    private void addOfferToSharedPreferences(DailyOfferSimple offer) {
        //add notification to shared pref
        TinyDB db=new TinyDB(myContext);
        ArrayList<Object> list=db.getListObject("notification",DailyOfferSimple.class);
        if(list==null) list=new ArrayList<>();
        list.add(offer);
    }
}

