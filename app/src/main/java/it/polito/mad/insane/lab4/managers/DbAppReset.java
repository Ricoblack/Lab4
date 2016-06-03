package it.polito.mad.insane.lab4.managers;

import android.location.Location;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import java.util.Map;

import it.polito.mad.insane.lab4.data.Booking;
import it.polito.mad.insane.lab4.data.Dish;
import it.polito.mad.insane.lab4.data.Restaurant;
import it.polito.mad.insane.lab4.data.RestaurateurProfile;
import it.polito.mad.insane.lab4.data.Review;
import it.polito.mad.insane.lab4.data.User;

//New instance of DbApp class, used for debugging and for remotely resetting the online Db
public class DbAppReset
{
    private Map<String,Restaurant> restaurants;
    private Map<String,Booking> bookings;
    private Map<String,User> users;
    private Map<String,Review> reviews;




    public Map<String, Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Map<String, Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    public Map<String, Booking> getBookings() {
        return bookings;
    }

    public void setBookings(Map<String, Booking> bookings) {
        this.bookings = bookings;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void setUsers(Map<String, User> users) {
        this.users = users;
    }

    public Map<String, Review> getReviews() {
        return reviews;
    }

    public void setReviews(Map<String, Review> reviews) {
        this.reviews = reviews;
    }

    public void fillDbAppReset(){


        //CARICAMENTO DATI ORARI
        Date dClose = new Date();  //Debug date to test if time constraints on reservations work
        dClose.setHours(23);  //tutti i ristoranti sono aperti dalle 7.05 fino alle 23.55
        dClose.setMinutes(55);

        Date dStart=new Date();
        dStart.setHours(7);
        dStart.setMinutes(5);

        //CARICMENTO DATI LOCATIONS
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

        //CARICAMENTO DATI PROFILES
        RestaurateurProfile profile =new RestaurateurProfile("Pizza-Pazza","Corso Duca Degli Abruzzi, 10","PoliTo","Pizza","Venite a provare la pizza più gustosa di Torino",dStart,dClose,"Chiusi la domenica","Bancomat","Wifi-free");
        RestaurateurProfile profile2=new RestaurateurProfile("Just Pasta", "Via Roma, 55", "UniTo","Pasta","Pasta per tutti i gusti",dStart,dClose,"Aperti tutta la settimana","Bancomat,carta","Privo di barriere architettoniche");
        RestaurateurProfile profile3=new RestaurateurProfile("Pub la locanda", "Via Lagrange, 17", "UniTo","Ethnic", "L'isola felice dello studente universitario",dStart,dClose,"Giropizza il sabato sera","Bancomat","Wifi-free");
        RestaurateurProfile profile4=new RestaurateurProfile("Mangiaquì restaurant", "Via Saluzzo, 17", "PoliTo","Ethnic", "L'isola del miglior ovolollo studentesco",new Date(),new Date(),"Cicchetto di ben venuto il sabato sera","Bancomat","Wifi-free");
        RestaurateurProfile profile5=new RestaurateurProfile("Origami restaurant", "Piazza Vittorio Veneto, 18F", "UniTo","Ethnic", "Il miglior giapponese di Torino",dStart,dClose,"All you can eat a pranzo","Bancomat","Wifi-free");

        //CARICAMENTO DATI DISHES
        Dish dish1 = new Dish("0","Margherita", "La classica delle classiche", null, 5.50, 5, false);
        Dish dish2 = new Dish("1","Marinara", "Occhio all'aglio!", null, 2.50, 200, false);
        Dish dish3 = new Dish("2","Tonno", "Il gusto in una parola", null, 3.50, 300, false);
        Dish dish4 = new Dish("3","Politecnico", "Solo per veri ingegneri", null, 4.50, 104, false);
        Dish dish5 = new Dish("4","30L", "Il nome dice tutto: imperdibile", null, 5.55, 150, false);
        Dish dish6 = new Dish("5","Hilary", "Dedicata ad una vecchia amica", null, 5.55, 150, false);

        //CARICAMENTO DATI BOOKINGS
        Booking newBooking1 = new Booking();
        newBooking1.setID("book1");
        Map<String,String> elenco1=new HashMap<>();
        elenco1.put("dish1","1");
        newBooking1.setDishesIdList(elenco1);
        Calendar calendar = Calendar.getInstance();
        //calendar.set(Calendar.HOUR_OF_DAY, 15);
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyy HH:mm");
        newBooking1.setDateTime(parser.format(calendar.getTime()));
        //newBooking.setDate_time(calendar);
        newBooking1.setNote("Il cibo deve essere ben cotto");
        newBooking1.setRestaurantID("001");
        newBooking1.setUserId("0001");

        Booking newBooking2 = new Booking();
        newBooking2.setID("book2");
        Map<String,String> elenco2=new HashMap<>();
        elenco2.put("dish1","2");
        newBooking2.setDishesIdList(elenco2);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.DAY_OF_MONTH, 4);
        SimpleDateFormat parser2 = new SimpleDateFormat("dd/MM/yyy HH:mm");
        newBooking2.setDateTime(parser2.format(calendar2.getTime()));
        //newBooking.setDate_time(calendar);
        newBooking2.setNote("Il cibo deve essere ben cotto");
        newBooking2.setRestaurantID("001");
        newBooking2.setUserId("0002");

        Booking newBooking3 = new Booking();
        newBooking3.setID("book3");
        Map<String,String> elenco3=new HashMap<>();
        elenco3.put("dish1","3");
        newBooking3.setDishesIdList(elenco3);
        Calendar calendar3 = Calendar.getInstance();
        calendar3.set(Calendar.HOUR_OF_DAY, 18);
        SimpleDateFormat parser3 = new SimpleDateFormat("dd/MM/yyy HH:mm");
        newBooking3.setDateTime(parser3.format(calendar3.getTime()));
        //newBooking3.setDate_time(calendar3);
        newBooking3.setNote("Il cibo deve essere ben cotto");
        newBooking3.setRestaurantID("001");
        newBooking3.setUserId("0003");

        Booking newBooking4 = new Booking();
        newBooking4.setID("book4");
        Map<String,String> elenco4=new HashMap<>();
        elenco4.put("dish1","4");
        newBooking4.setDishesIdList(elenco4);
        Calendar calendar4 = Calendar.getInstance();
        //calendar.set(Calendar.HOUR_OF_DAY, 15);
        SimpleDateFormat parser4 = new SimpleDateFormat("dd/MM/yyy HH:mm");
        newBooking4.setDateTime(parser4.format(calendar4.getTime()));
        //newBooking.setDate_time(calendar);
        newBooking4.setNote("Il cibo deve essere ben cotto");
        newBooking4.setRestaurantID("001");
        newBooking4.setUserId("0004");

        Booking newBooking5 = new Booking();
        newBooking5.setID("book5");
        Map<String,String> elenco5=new HashMap<>();
        elenco5.put("dish1","5");
        newBooking5.setDishesIdList(elenco5);
        Calendar calendar5 = Calendar.getInstance();
        //calendar.set(Calendar.HOUR_OF_DAY, 15);
        SimpleDateFormat parser5 = new SimpleDateFormat("dd/MM/yyy HH:mm");
        newBooking5.setDateTime(parser5.format(calendar5.getTime()));
        //newBooking.setDate_time(calendar);
        newBooking5.setNote("Il cibo deve essere ben cotto");
        newBooking5.setRestaurantID("001");
        newBooking5.setUserId("0005");


            //CARICAMENTO DATI REVIEWS
            Review rev1=new Review();
            rev1.setReviewId("1234");
            rev1.setRestaurantID("rest1");
            rev1.setDate(new Date());
            rev1.setUserID("0001");
            //rev1.setScores(new double[]{8.0,10.0,7.0});
            rev1.setTitle("Splendido locale per studenti");
            rev1.setText("Il cibo è ottimo e la presenza del wifi garantisce il possibile studio anche a pranzo, i prezzi sono ottimi," +
                    " e inoltre aggiungiamo qualche riga per vedere se funziona la TextView espandibile!!!");


            Review rev2=new Review();
            rev2.setReviewId("5678");
            rev2.setRestaurantID("rest2");
            rev2.setDate(new Date());
            rev2.setUserID("0002");
            //rev2.setScores(new double[]{8.0,10.0,7.0});
            rev2.setTitle("Ottimo locale");
            rev2.setText("Servizio rapido");


            //CARICAMENTO DATI USERS
            Map<String,String> rev1Ids=new HashMap<String,String>();
            rev1Ids.put("1234","1234");
            rev1Ids.put("1234","5678");
            Map<String,String> booking1Ids=new HashMap<>();
            booking1Ids.put("book1","1");
            User u1=new User("0001","Pinco","Pallino",rev1Ids,booking1Ids,null);

            //CARICAMENTO DATI RESTAURANTS
            Map<String,String> rev1List=new HashMap<>();
            rev1List.put("1234","1234");
            Map<String,String> book1List=new HashMap<>();
            //TODO qui c'era scritto ("book1", "1") ma in realta' l'id della prima prenotazione e' appunto book1, quindi l'ho modificato
            //TODO giusto Charles? (Renato)
            book1List.put("book1","book1");
            book1List.put("book2","book2");
            book1List.put("book3","book3");
            book1List.put("book4","book4");
            book1List.put("book5","book5");

        HashMap<String,Dish> dish1Map=new HashMap<>();
            dish1Map.put("dish1",dish1);
            dish1Map.put("dish2",dish2);
            Restaurant restaurant1=new Restaurant("rest1", profile, rev1List,book1List,dish1Map,null);

            Map<String,String> rev2List=new HashMap<>();
            rev2List.put("5678","5678");
            Map<String,String> book2List=new HashMap<>();
            HashMap<String,Dish> dish2Map=new HashMap<>();
            dish2Map.put("dish2",dish3);
            dish2Map.put("dish3",dish4);
            Restaurant restaurant2=new Restaurant("rest2",profile2,rev2List,book2List,dish2Map,null);


        //CARICAMENTO DATI PRECEDENTI NEL DBNEW
        this.restaurants=new HashMap<String,Restaurant>();
        this.bookings=new HashMap<String,Booking>();
        this.users=new HashMap<String,User>();
        this.reviews=new HashMap<String,Review>();

        restaurants.put("rest1",restaurant1);
        restaurants.put("rest2",restaurant2);

        bookings.put("book1",newBooking1);
        bookings.put("book2",newBooking2);
        bookings.put("book3",newBooking3);
        bookings.put("book4",newBooking4);
        bookings.put("book5",newBooking5);


        users.put("0001",u1);

        reviews.put("1234",rev1);
        reviews.put("5678",rev2);


    }


}
