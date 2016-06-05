package it.polito.mad.insane.lab4.data;

import android.location.Location;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import it.polito.mad.insane.lab4.managers.Cryptography;

/**
 * Created by carlocaramia on 28/04/16.
 */
public class Restaurant {
//    private static final int N_SCORES = 3;
//    private Map<String,String> bookingsIdList;
//    private Map<String,String> reviewsIdList;
//    TODO aggiungerlo via codice nel db (Michele)
//    private Location location;
    private String ID;
    private Map<String,Dish> dishMap;
    private Map<String,DailyOffer> dailyOfferMap;
    private String username;
    private String password;
    private RestaurantInfo info;
    private Map<String,Double> avgScores;
    private double avgFinalScore;


    public Restaurant(){}

    public Map<String, Dish> getDishMap() {
        return dishMap;
    }

    public void setDishMap(Map<String, Dish> dishMap) {
        this.dishMap = dishMap;
    }

    public Map<String, DailyOffer> getDailyOfferMap() {
        return dailyOfferMap;
    }

    public void setDailyOfferMap(Map<String, DailyOffer> dailyOfferMap) {
        this.dailyOfferMap = dailyOfferMap;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        //Cryptography cryptography = new Cryptography();
        //this.password = cryptography.SHA1(password);
        this.password = password;
    }

    public RestaurantInfo getInfo() {
        return info;
    }

    public void setInfo(RestaurantInfo info) {
        this.info = info;
    }

    public Map<String, Double> getAvgScores() {
        return avgScores;
    }

    public void setAvgScores(Map<String, Double> avgScores) {
        this.avgScores = avgScores;
    }

    public double getAvgFinalScore() {
        return avgFinalScore;
    }

    public void setAvgFinalScore(double avgFinalScore) {
        this.avgFinalScore = avgFinalScore;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

//    public Restaurant(String restaurantID, String password, RestaurantInfo profile, Map<String,String> reviewsIdList, Map<String,String> bookingsIdList, Map<String,Dish> dishMap, Location location)throws NoSuchAlgorithmException, UnsupportedEncodingException {
//        Cryptography cryptography = new Cryptography();
//        this.profile = profile;
//        this.reviewsIdList = reviewsIdList;
//        this.bookingsIdList=bookingsIdList;
//        //this.dishes = (List<Dish>) dishMap.values();
//        this.restaurantID = restaurantID;
//        this.location=location;
//        this.password = cryptography.SHA1(password);
//
//        this.dishMap=dishMap;
//
//        // calcolo a priori punteggio del ristorante. quando avremo il server sarebbe conveniente farlo sul server per ridurre
//        // la quantita' di calcoli sul client. in questo caso ogni activity fa una semplice get invece di calcolare ogni volta il punteggio
//
//        /*
//        Arrays.fill(totalScores, 0);
//        for(Review r : this.reviews){
//            for(int i = 0; i < N_SCORES; i++){
//                this.totalScores[i] += r.getScores()[i];
//            }
//        }
//        for (int i = 0; i < N_SCORES; i++) {
//            avgScores[i] = totalScores[i] / this.reviews.size();
//            avgFinalScore += avgScores[i];
//        }
//        avgFinalScore = avgFinalScore/N_SCORES;
//        */
//    }

}
