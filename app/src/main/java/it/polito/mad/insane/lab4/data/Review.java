package it.polito.mad.insane.lab4.data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by Renato on 28/04/2016.
 */
public class Review {

    private String ID;
    private String userId;
    private String restaurantId;
    private String dateTime;
    private String title;
    private String text;
    private String username;
    private Map<String,Double> scoresMap; // <Parameter, score>
    private double avgFinalScore;
//    private String photoPath;


    /*
    public double[] getScores() {
        return scores;
    }
    */
    /*
    public void setScores(double[] scores) {
        this.scores = scores;
        double sum = 0;
        for(double d : scores)
            sum += d;
        this.finalScore = sum/scores.length;
    }
    */

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Map<String, Double> getScoresMap() {
        return scoresMap;
    }

    public void setScoresMap(Map<String, Double> scoresMap) {
        this.scoresMap = scoresMap;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
