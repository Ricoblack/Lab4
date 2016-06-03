package it.polito.mad.insane.lab4.data;

import android.location.Location;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by carlocaramia on 28/04/16.
 */
public class Restaurant {

    //campi del db
    private Map<String,String> bookingsIdList;
    private Map<String,String> reviewsIdList;
    private Map<String,Dish> dishMap;
    private String restaurantID;
    private RestaurateurProfile profile;
    private String password;


    private static final int N_SCORES = 3;

    //private List<Review> reviews;
    //private List<Dish> dishes;

    //private double[] totalScores = new double[N_SCORES];
    //private double[] avgScores = new double[N_SCORES];
    private double avgFinalScore;
    private Location location;

    public Restaurant(){}

    public Restaurant(String restaurantID, String password, RestaurateurProfile profile, Map<String,String> reviewsIdList,Map<String,String> bookingsIdList, Map<String,Dish> dishMap, Location location) {
        this.profile = profile;
        this.reviewsIdList = reviewsIdList;
        this.bookingsIdList=bookingsIdList;
        //this.dishes = (List<Dish>) dishMap.values();
        this.restaurantID = restaurantID;
        this.location=location;
        this.password = password;

        this.dishMap=dishMap;

        // calcolo a priori punteggio del ristorante. quando avremo il server sarebbe conveniente farlo sul server per ridurre
        // la quantita' di calcoli sul client. in questo caso ogni activity fa una semplice get invece di calcolare ogni volta il punteggio

        /*
        Arrays.fill(totalScores, 0);
        for(Review r : this.reviews){
            for(int i = 0; i < N_SCORES; i++){
                this.totalScores[i] += r.getScores()[i];
            }
        }
        for (int i = 0; i < N_SCORES; i++) {
            avgScores[i] = totalScores[i] / this.reviews.size();
            avgFinalScore += avgScores[i];
        }
        avgFinalScore = avgFinalScore/N_SCORES;
        */
    }

    public RestaurateurProfile getProfile() {
        return profile;
    }

    public void setProfile(RestaurateurProfile profile) {
        this.profile = profile;
    }





    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    /*
    public double[] getAvgScores(){
        return this.avgScores;
    }
    public double[] getTotalScores (){
        return this.totalScores;
    }
    */
    public double getAvgFinalScore(){
        return this.avgFinalScore;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Map<String, Dish> getDishMap() {
        return dishMap;
    }

    public void setDishMap(Map<String, Dish> dishMap) {
        this.dishMap = dishMap;
    }

    public Map<String, String> getReviewsIdList() {
        return reviewsIdList;
    }

    public void setReviewsIdList(Map<String, String> reviewsIdList) {
        this.reviewsIdList = reviewsIdList;
    }

    public Map<String, String> getBookingsIdList() {
        return bookingsIdList;
    }

    public void setBookingsIdList(Map<String, String> bookingsIdList) {
        this.bookingsIdList = bookingsIdList;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
