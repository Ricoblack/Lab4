package it.polito.mad.insane.lab4.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import java.util.List;

import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.adapters.NotifyArrayAdapter;
import it.polito.mad.insane.lab4.managers.RestaurateurJsonManager;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView lv = (ListView) findViewById(R.id.notification_list_view);
        if (lv != null){
            RestaurateurJsonManager manager = RestaurateurJsonManager.getInstance(NotificationsActivity.this);
            NotifyArrayAdapter adapter = new NotifyArrayAdapter(NotificationsActivity.this, R.layout.notify_listview_item,
                    manager.getDailyOffersSimple());
            lv.setAdapter(adapter);
        }
    }
}
