package it.polito.mad.insane.lab4.data;

import android.location.Location;

import java.util.Arrays;
import java.util.List;

/**
 * Created by carlocaramia on 28/04/16.
 */
public class Restaurant {

    private static final int N_SCORES = 3;
    private RestaurateurProfile profile;
    private List<Review> reviews;
    private List<Dish> dishes;
    private String restaurantID;
    private double[] totalScores = new double[N_SCORES];
    private double[] avgScores = new double[N_SCORES];
    private double avgFinalScore;
    private Location location;

    public Restaurant(){}

    public Restaurant(String restaurantID, RestaurateurProfile profile, List<Review> reviews, List<Dish> dishes, Location location) {
        this.profile = profile;
        this.reviews = reviews;
        this.dishes = dishes;
        this.restaurantID = restaurantID;
        this.location=location;

        // calcolo a priori punteggio del ristorante. quando avremo il server sarebbe conveniente farlo sul server per ridurre
        // la quantita' di calcoli sul client. in questo caso ogni activity fa una semplice get invece di calcolare ogni volta il punteggio

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

    }

    public RestaurateurProfile getProfile() {
        return profile;
    }

    public void setProfile(RestaurateurProfile profile) {
        this.profile = profile;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public double[] getAvgScores(){
        return this.avgScores;
    }
    public double getAvgFinalScore(){
        return this.avgFinalScore;
    }
    public double[] getTotalScores (){
        return this.totalScores;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
