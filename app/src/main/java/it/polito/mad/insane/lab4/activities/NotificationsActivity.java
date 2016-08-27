package it.polito.mad.insane.lab4.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.NotifyArrayAdapter;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class NotificationsActivity extends AppCompatActivity {

    RestaurateurJsonManager manager=RestaurateurJsonManager.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.notifications);

        final ListView lv = (ListView) findViewById(R.id.notification_list_view);
        if (lv != null){
            RestaurateurJsonManager manager = RestaurateurJsonManager.getInstance(NotificationsActivity.this);
            if(manager.getDailyOffersSimple().size() == 0){
                lv.setVisibility(View.GONE);
                TextView tv = (TextView) findViewById(R.id.no_notification_textview);
                if (tv != null) {
                    tv.setVisibility(View.VISIBLE);
                }
            }
            else {
                NotifyArrayAdapter adapter = new NotifyArrayAdapter(NotificationsActivity.this, R.layout.notify_listview_item, manager.getDailyOffersSimple());
                lv.setAdapter(adapter);
            }
        }

        Button readAll=(Button) findViewById(R.id.buttonRead);
        readAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.setAllDailyOffersRead();
               // ((BaseAdapter) lv.getAdapter()).notifyDataSetChanged();
                //TODO: notificare il refresh della listview così da notare se segno tutto come letto
            }
        });
    }

}
