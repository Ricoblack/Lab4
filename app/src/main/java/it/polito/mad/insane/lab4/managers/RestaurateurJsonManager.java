package it.polito.mad.insane.lab4.managers;

import android.content.Context;
import android.content.ContextWrapper;
import android.location.Location;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import it.polito.mad.insane.lab4.data.Booking;
import it.polito.mad.insane.lab4.R;
import it.polito.mad.insane.lab4.data.Restaurant;

/**
 * Created by carlocaramia on 08/04/16.
 */
// Singleton Class
public class RestaurateurJsonManager
{

    private static RestaurateurJsonManager instance = null;

    //private static DbApp dbApp;
    private DbAppReset dbAppReset;
    private Context myContext;
    private Location location;  //setto il polito come location dove cercare i ristoranti

    public static RestaurateurJsonManager getInstance(Context myContext)
    {
        if(RestaurateurJsonManager.instance == null)
            RestaurateurJsonManager.instance = new RestaurateurJsonManager(myContext);

        return RestaurateurJsonManager.instance;
    }



    private RestaurateurJsonManager(Context myContext)
    {
        dbAppReset = new DbAppReset();
        this.myContext = myContext;

        //set fake location
        this.location = new Location("me");
        location.setLatitude(45.064480);
        location.setLongitude(7.660290);

        //Se l'app è aperta per la prima volta non c'è un json di reset, qui lo creo e lo riempio con dati random per il reset
        //Altrimenti recupero il json salvato

        /*
        if(getDbApp()==null)
        {
            dbAppReset.fillDbAppReset();
            saveDbApp();
        }
        else
        {
            //recupero json
            this.dbAppReset = getDbApp();
        }
        */
        try {
            dbAppReset.fillDbAppReset();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public String getJsonString()
    {
        //ritorna la stringa del Json

        if(dbAppReset == null) return "";

        Gson gson = new Gson();
        String json = gson.toJson(dbAppReset);

        return json;
    }

    public int saveDbApp(){

        //scrivo la stringa json su disco
        String jsonString = this.getJsonString();

        ContextWrapper cw = new ContextWrapper(myContext);

        // path to /data/data/yourapp/app_data/jsonDir
        File directory = cw.getDir("jsonDir", Context.MODE_PRIVATE);

        // Create jsonDir
        File mypath = new File(directory,"dbappreset.json");

        BufferedWriter bufferedWriter=null;

        try
        {
            bufferedWriter = new BufferedWriter(new FileWriter(mypath));

            bufferedWriter.write(jsonString);

            bufferedWriter.close();

        } catch (Exception e)
        {
            return -1;
        }
        finally
        {
            try
            {
                bufferedWriter.close();
            } catch (IOException e)
            {
                return -2;
            }
        }
        return 0; //tutto ok
    }

    public DbAppReset getDbApp()
    {
        Gson gson = new Gson();

        ContextWrapper cw = new ContextWrapper(myContext);
        // path to /data/data/yourapp/app_data/jsonDir
        File directory = cw.getDir("jsonDir", Context.MODE_PRIVATE);
        BufferedReader bufferedReader = null;

        try
        {
            File f = new File(directory, "dbapp.json");
            bufferedReader = new BufferedReader(new FileReader(f));
        }
        catch (FileNotFoundException e)
        {
            //nothing
            return null;
        }

        //convert the json string back to object
        DbAppReset obj = gson.fromJson(bufferedReader, DbAppReset.class);

        this.dbAppReset = obj;
        return obj;
    }

    public void resetDbApp(){
        //contact firebase server and reset all db
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/");

        myRef.setValue(dbAppReset, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Toast.makeText(myContext,"DB RESETTATO",Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean checkIfRespectsDistanceConstraint(Restaurant r, String distanceValue) {

        String subString=distanceValue.substring(0,distanceValue.length()-1);
        float distance=Float.parseFloat(subString);

        //TODO bisogna implementare il funzionamento della geolocalizzazione (Michele)
//        if(r.getLocation().distanceTo(this.location)<=distance) return true;


        return false;
    }

    private boolean checkIfRespectsTimeConstraint(Restaurant r, String timeValue) throws ParseException {

        String[] array=timeValue.split("-");
        String startTimeString=array[0];
        String endTimeString=array[1];

        DateFormat df=new SimpleDateFormat("HH.mm");
        Date startTimeDate=new Date();
        Date endTimeDate=new Date();

        try {
            startTimeDate=df.parse(startTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            endTimeDate=df.parse(endTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(sdf.parse(r.getInfo().getOpeningHour()));

        //TODO trasformare le date in calendar ed eliminare i dati deprecated (Renato)
        if(sdf.parse(r.getInfo().getOpeningHour()).getHours()<=startTimeDate.getHours() &&
                sdf.parse(r.getInfo().getOpeningHour()).getHours()>=endTimeDate.getHours()) return true;

        return false;
    }

    private boolean checkIfRespectsTypeConstraint(Restaurant r, String typeValue) {

        if(r.getInfo().getCuisineType().toLowerCase().equals(typeValue.toLowerCase())) return true;
        return false;
    }

    /*
    private boolean checkIfRespectsPriceConstraint(Restaurant r, String priceValue) {

        //check if exists at least one dish that costs less that price
        String subString=priceValue.substring(0,priceValue.length()-1);
        int price=Integer.parseInt(subString);
        for(Dish d : r.getDishes()){
            if(d.getPrice()<=price) return true;
        }

        return false;
    }
    */

    public List<Restaurant> getOrderedRestaurants(String orderBy, List<Restaurant> listaFiltrata) {
        List<Restaurant> lista = listaFiltrata;

        //TODO bisogna implementare il funzionamento della geolocalizzazione (Michele)
        if(orderBy.toLowerCase().equals(myContext.getResources().getString(R.string.distance).toLowerCase())){
//            Collections.sort(lista, new Comparator<Restaurant>() {
//                @Override
//                public int compare(Restaurant lhs, Restaurant rhs) {
//                    return (int)(location.distanceTo(lhs.getLocation())-location.distanceTo(rhs.getLocation()));
//                }
//            });
        }
        else if(orderBy.toLowerCase().equals(myContext.getResources().getString(R.string.score).toLowerCase())){
            Collections.sort(lista, new Comparator<Restaurant>() {
                @Override
                public int compare(Restaurant lhs, Restaurant rhs) {
                    return (int)(rhs.getAvgFinalScore()- lhs.getAvgFinalScore());
                }
            });
        }
        return lista;
    }

    public Location getLocation(){
        return this.location;
    }

    private boolean timeIsBefore(Date d1, Date d2) {
        DateFormat f = new SimpleDateFormat("HH:mm:ss.SSS");
        return f.format(d1).compareTo(f.format(d2)) < 0;
    }
    private boolean timeIsAfter(Date d1, Date d2) {
        DateFormat f = new SimpleDateFormat("HH:mm:ss.SSS");
        return f.format(d1).compareTo(f.format(d2)) >= 0;
    }

    //TODO: da scommentare successivamente (Renato)
//    public static void deleteReservationByID(List<Booking> mData, String id)
//    {
//        ArrayList<Booking> bookings= (ArrayList<Booking>) mData;
//        for(int i=0; i<bookings.size();i++)
//        {
//            Booking b = bookings.get(i);
//            if(b.getID().equals(id))
//            {
//                bookings.remove(i);
//                return;
//            }
//        }
//        return;
//    }

    /**
     * Created by carlocaramia on 09/04/16.
     */
    private class DbApp
    {

        private List<Restaurant> restaurants;

        private List<Booking> bookings;

        private int currentReservationID = 0;

        public  DbApp()
        {
            this.restaurants = null;
            this.bookings = null;
            this.currentReservationID = 0;
        }

        public List<Restaurant> getRestaurants() {
            return restaurants;
        }

        public void setRestaurants(List<Restaurant> restaurants) {
            this.restaurants = restaurants;
        }

        public List<Booking> getBookings() {
            return bookings;
        }

        public void setBookings(List<Booking> bookings) {
            this.bookings = bookings;
        }

        public int getCurrentReservationID() {
            return currentReservationID;
        }

        public void setCurrentReservationID(int currentReservationID) {
            this.currentReservationID = currentReservationID;
        }

        public void fillDbApp()
        {
        /*
            Date dClose = new Date();  //Debug date to test if time constraints on reservations work
            dClose.setHours(23);  //tutti i ristoranti sono aperti dalle 7.05 fino alle 23.55
            dClose.setMinutes(55);

            Date dStart=new Date();
            dStart.setHours(7);
            dStart.setMinutes(5);

            //CARICAMENTO DATI RISTORANTI
            RestaurantInfo profile =new RestaurantInfo("Pizza-Pazza","Corso Duca Degli Abruzzi, 10","PoliTo","Pizza","Venite a provare la pizza più gustosa di Torino",dStart,dClose,"Chiusi la domenica","Bancomat","Wifi-free");
            RestaurantInfo profile2=new RestaurantInfo("Just Pasta", "Via Roma, 55", "UniTo","Pasta","Pasta per tutti i gusti",dStart,dClose,"Aperti tutta la settimana","Bancomat,carta","Privo di barriere architettoniche");
            RestaurantInfo profile3=new RestaurantInfo("Pub la locanda", "Via Lagrange, 17", "UniTo","Ethnic", "L'isola felice dello studente universitario",dStart,dClose,"Giropizza il sabato sera","Bancomat","Wifi-free");
            RestaurantInfo profile4=new RestaurantInfo("Mangiaquì restaurant", "Via Saluzzo, 17", "PoliTo","Ethnic", "L'isola del miglior ovolollo studentesco",new Date(),new Date(),"Cicchetto di ben venuto il sabato sera","Bancomat","Wifi-free");
            RestaurantInfo profile5=new RestaurantInfo("Origami restaurant", "Piazza Vittorio Veneto, 18F", "UniTo","Ethnic", "Il miglior giapponese di Torino",dStart,dClose,"All you can eat a pranzo","Bancomat","Wifi-free");

            //CARICAMENTO DATI DISHES
            ArrayList<Dish> dishes1=new ArrayList<Dish>();
            ArrayList<Dish> dishes2=new ArrayList<Dish>();
            ArrayList<Dish> dishes3=new ArrayList<Dish>();

            //ristorante1
            Dish dish1 = new Dish("0","Margherita", "La classica delle classiche", null, 5.50, 5, false);
            dishes1.add(dish1);

            Dish dish2 = new Dish("1","Marinara", "Occhio all'aglio!", null, 2.50, 200, false);
            dishes1.add(dish2);

            Dish dish3 = new Dish("2","Tonno", "Il gusto in una parola", null, 3.50, 300, false);
            dishes1.add(dish3);

            Dish dish4 = new Dish("3","Politecnico", "Solo per veri ingegneri", null, 4.50, 104, false);
            dishes1.add(dish4);

            Dish dish5 = new Dish("4","30L", "Il nome dice tutto: imperdibile", null, 5.55, 150, false);
            dishes1.add(dish5);

            Dish dish6 = new Dish("5","Hilary", "Dedicata ad una vecchia amica", null, 5.55, 150, false);
            dishes1.add(dish6);

            Dish dish7 = new Dish("6","Carlo", "Pomodoro e pesto, il mix perfetto", null, 5.55, 150, false);
            dishes1.add(dish7);

            Dish dish8 = new Dish("7","Federico", "La vera pizza napoletana, spessa al punto giusto", null, 5.55, 150, false);
            dishes1.add(dish8);

            Dish dish9 = new Dish("8","Michele", "Pomodoro, patatine, wurstel, senape", null, 5.55, 150, false);
            dishes1.add(dish9);

            Dish dish10 = new Dish("9","Renato", "Pizza e pasta: mai dire mai al gusto!", null, 5.55, 150, false);
            dishes1.add(dish10);

            //ristorante2
            Dish dish11 = new Dish("0","Pasta al ragù", "pasta al ragu", null, 5.50, 100, false);
            dishes2.add(dish11);

            Dish dish12 = new Dish("1","Pasta al pesto", "la vera genovese", null, 2.50, 200, false);
            dishes2.add(dish12);

            Dish dish13 = new Dish("2","Pasta alle olive", "mediterranea", null, 3.50, 300, false);
            dishes2.add(dish13);

            Dish dish14 = new Dish("3","Pasta al burro", "povera ma gustosa", null, 4.50, 104, false);
            dishes2.add(dish14);

            //ristorante3
            Dish dish15 = new Dish("0","hamburger con patate", "vieni a provarlo", null, 5.50, 100, false);
            dishes3.add(dish15);

            Dish dish16 = new Dish("1","Riso all'inglese", "riso in bianco", null, 2.50, 200, false);
            dishes3.add(dish16);

            Dish dish17 = new Dish("2","Pasta ai 4 formaggi", "mediterranea", null, 3.50, 300, false);
            dishes3.add(dish17);

            Dish dish18 = new Dish("3","Fiorentina", "per i più coraggiosi", null, 4.50, 104, false);
            dishes3.add(dish18);


            //CARICAMENTO DATI BOOKINGS
            this.bookings=new ArrayList<Booking>();



            Booking newBooking = new Booking();
            newBooking.setID("1");
            ArrayList<Dish> elenco1=new ArrayList<Dish>();
            elenco1.add(dishes1.get(2));
            elenco1.add(dishes1.get(5));
            newBooking.setDishes(elenco1);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 15);
            newBooking.setDate_time(calendar);
            newBooking.setNote("Il cibo deve essere ben cotto");
            newBooking.setRestaurantID("001");
            bookings.add(newBooking);

            Booking newBooking2 = new Booking();
            newBooking2.setID("2");
            ArrayList<Dish> elenco2=new ArrayList<Dish>();
            elenco2.add(dishes1.get(3));
            newBooking2.setDishes(elenco2);
            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 16);
            newBooking2.setDate_time(calendar);
            newBooking2.setNote("Sono allergico ai latticini. Prego il ristoratore di evitarne l'utilizzo");
            newBooking2.setRestaurantID("001");
            bookings.add(newBooking2);

            String newBooking3 = new String();
            newBooking3.setID("3");
            ArrayList<Dish> elenco3=new ArrayList<Dish>();
            elenco3.add(dishes1.get(3));
            newBooking3.setDishes(elenco3);
            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 18);
            newBooking3.setDate_time(calendar);
            newBooking3.setRestaurantID("001");
            bookings.add(newBooking3);

            String newBooking4 = new String();
            newBooking4.setID("4");
            ArrayList<Dish> elenco4=new ArrayList<Dish>();
            elenco4.add(dishes2.get(2));
            newBooking4.setDishes(elenco4);
            calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-2);
            calendar.set(Calendar.HOUR_OF_DAY, 17);
            newBooking4.setDate_time(calendar);
            newBooking4.setRestaurantID("002");
            bookings.add(newBooking4);

            String newBooking5 = new String();
            newBooking5.setID("5");
            ArrayList<Dish> elenco5=new ArrayList<Dish>();
            elenco5.add(dishes2.get(0));
            elenco5.add(dishes2.get(1));
            elenco5.add(dishes2.get(0));
            newBooking5.setDishes(elenco5);
            calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-1);
            calendar.set(Calendar.HOUR_OF_DAY, 19);
            newBooking5.setDate_time(calendar);
            newBooking5.setRestaurantID("002");
            bookings.add(newBooking5);


            String newBooking6 = new String();
            newBooking6.setID("6");
            ArrayList<Dish> elenco6=new ArrayList<Dish>();
            elenco6.add(dishes2.get(3));
            newBooking6.setDishes(elenco6);
            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 8);
            newBooking6.setDate_time(calendar);
            newBooking6.setRestaurantID("002");
            newBooking6.setNote("Se arrivo tardi tenete il piatto al caldo, grazie");
            bookings.add(newBooking6);

            String newBooking7 = new String();
            newBooking7.setID("7");
            ArrayList<Dish> elenco7=new ArrayList<Dish>();
            elenco7.add(dishes2.get(0));
            elenco7.add(dishes2.get(1));
            newBooking7.setDishes(elenco7);
            calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-1);
            calendar.set(Calendar.HOUR_OF_DAY, 13);
            newBooking7.setDate_time(calendar);
            newBooking7.setRestaurantID("002");
            bookings.add(newBooking7);

            String newBooking8 = new String();
            newBooking8.setID("8");
            ArrayList<Dish> elenco8=new ArrayList<Dish>();
            elenco8.add(dishes3.get(3));
            elenco8.add(dishes3.get(1));
            elenco8.add(dishes3.get(0));
            newBooking8.setDishes(elenco8);
            calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)-1);
            calendar.set(Calendar.HOUR_OF_DAY, 14);
            newBooking8.setDate_time(calendar);
            newBooking8.setRestaurantID("003");
            bookings.add(newBooking8);

            String newBooking9 = new String();
            newBooking9.setID("9");
            ArrayList<Dish> elenco9=new ArrayList<Dish>();
            elenco9.add(dishes3.get(1));
            elenco9.add(dishes3.get(2));
            elenco9.add(dishes3.get(3));
            newBooking9.setDishes(elenco9);
            calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)+1);
            calendar.set(Calendar.HOUR_OF_DAY, 15);
            newBooking9.setDate_time(calendar);
            newBooking9.setRestaurantID("003");
            bookings.add(newBooking9);

            String newBooking10 = new String();
            newBooking10.setID("10");
            ArrayList<Dish> elenco10=new ArrayList<Dish>();
            elenco10.add(dishes1.get(3));
            elenco10.add(dishes1.get(1));
            elenco10.add(dishes1.get(2));
            newBooking10.setDishes(elenco10);
            calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)+1);
            calendar.set(Calendar.HOUR_OF_DAY, 16);
            newBooking10.setDate_time(calendar);
            newBooking10.setRestaurantID("003");
            bookings.add(newBooking10);


            //CARICAMENTO REVIEWS

            ArrayList<Review> reviews1=new ArrayList<Review>();
            ArrayList<Review> reviews2=new ArrayList<Review>();
            ArrayList<Review> reviews3=new ArrayList<Review>();
            ArrayList<Review> reviews5=new ArrayList<Review>();

            Review rev1=new Review();
            rev1.setRestaurantID("001");
            rev1.setDate(new Date());
            rev1.setUserID("0001");
            rev1.setScores(new double[]{8.0,10.0,7.0});
            rev1.setTitle("Splendido locale per studenti");
            rev1.setText("Il cibo è ottimo e la presenza del wifi garantisce il possibile studio anche a pranzo, i prezzi sono ottimi," +
                    " e inoltre aggiungiamo qualche riga per vedere se funziona la TextView espandibile!!!");
            reviews1.add(rev1);

            Review rev2=new Review();
            rev2.setRestaurantID("002");
            rev2.setDate(new Date());
            rev2.setUserID("0002");
            rev2.setScores(new double[]{8.0,10.0,7.0});
            rev2.setTitle("Ottimo locale");
            rev2.setText("Servizio rapido");
            reviews1.add(rev2);


            Review rev3=new Review();
            rev3.setRestaurantID("002");
            rev3.setDate(new Date());
            rev3.setUserID(3);
            rev3.setScores(new double[]{7.0,9.5,8.0});
            rev3.setTitle("Vicino alla facoltà di lettere");
            //non setto apposta il testo
            reviews2.add(rev3);

            Review rev4=new Review();
            rev4.setRestaurantID("002");
            rev4.setDate(new Date());
            rev4.setUserID(4);
            rev4.setScores(new double[]{7.0,9.5,8.0});
            rev4.setTitle("Vicino alla facoltà di filosofia");
            //non setto apposta il testo
            reviews2.add(rev4);

            Review rev5=new Review();
            rev5.setRestaurantID("003");
            rev5.setDate(new Date());
            rev5.setUserID(5);
            rev5.setScores(new double[]{5.0,5.0,4.5});
            rev5.setTitle("Comodo il posto, troppo caro il locale");
            reviews3.add(rev5);

            Review rev6=new Review();
            rev6.setRestaurantID("003");
            rev6.setDate(new Date());
            rev6.setUserID(6);
            rev6.setScores(new double[]{5.0,5.0,4.5});
            rev6.setTitle("Più che studenti adatto a professori");
            rev6.setText("Posto molto centrale e comodo, ma i prezzi sono troppo alti");
            reviews3.add(rev6);

            reviews5.add(rev1);
            reviews5.add(rev4);
            reviews5.add(rev6);


            //Creazione locations dei ristoranti
            Location loc1 = new Location("001");
            loc1.setLatitude(45.064136);
            loc1.setLongitude(7.659370);

            Location loc2 = new Location("002");
            loc2.setLatitude(45.064605);
            loc2.setLongitude(7.668833);

            Location loc3 = new Location("003");
            loc3.setLatitude(45.064151);
            loc3.setLongitude(7.673167);

            Location loc4 = new Location("004");
            loc4.setLatitude(45.0595401);
            loc4.setLongitude(7.6771335);

            Location loc5 = new Location("005");
            loc5.setLatitude(45.0608443);
            loc5.setLongitude(7.6803656);

            //CREAZIONE RISTORANTI

            this.restaurants = new ArrayList<Restaurant>();

            Restaurant restaurant1=new Restaurant("001", profile, reviews1, dishes1,loc2);
            Restaurant restaurant2=new Restaurant("002",profile2,reviews2,dishes2,loc1);
            Restaurant restaurant3=new Restaurant("003",profile3,reviews3,dishes3,loc3);
            Restaurant restaurant4=new Restaurant("004",profile4,reviews3,dishes3,loc4);
            Restaurant restaurant5=new Restaurant("005",profile5,reviews5,dishes3,loc5);


            this.restaurants.add(restaurant1);
            this.restaurants.add(restaurant2);
            this.restaurants.add(restaurant3);
            this.restaurants.add(restaurant4);
            this.restaurants.add(restaurant5);
        */
        }


        public int getNextReservationID()
        {
            this.currentReservationID++;
            return this.currentReservationID;
        }
    }




}


