package it.polito.mad.insane.lab4.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import it.polito.mad.insane.lab4.data.Dish;

/**
 * Created by Federico on 10/06/2016.
 */
public class NotifyArrayAdapter extends ArrayAdapter<Dish> {

    private Context context;
    private int layoutResourceId;

    public NotifyArrayAdapter(Context context, int resource) {
        //costruttore
        super(context, resource);
        this.context = context;
        this.layoutResourceId = resource;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //qui vengono settati gli elementi grafici
        return null;
    }

    static class NotifyHolder
    {
        //qui puoi inserire gli elementi grafichi che vanno a rimepire l'elemento
//        private TextView quantity;
//        private TextView name;
//        private TextView totalPrice;
//        private ImageView minusButton;
//        private ImageView plusButton;
//        private Dish currentDish;
    }

}

