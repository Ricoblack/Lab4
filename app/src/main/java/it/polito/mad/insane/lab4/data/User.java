package it.polito.mad.insane.lab4.data;

import java.util.List;
import java.util.Map;

/**
 * Created by carlocaramia on 25/05/16.
 */
public class User {

    //campi del db
    private String userId;
    private String userName;
    private String userSurname;
    private String password;
    private Map<String,String> reviewsIdList;
    private Map<String,String> bookingsIdList;
    private Map<String,String> favouritesIdList;

    public User(String userId, String password, String userName, String userSurname, Map<String, String> reviewsIdList, Map<String,
            String> bookingsIdList, Map<String, String> favouritesIdList){
        this.userId=userId;
        this.password = password;
        this.userName=userName;
        this.reviewsIdList=reviewsIdList;
        this.bookingsIdList=bookingsIdList;
        this.favouritesIdList=favouritesIdList;

    }
    public java.lang.String getUserId() {
        return userId;
    }

    public void setUserId(java.lang.String userId) {
        this.userId = userId;
    }

    public java.lang.String getUserName() {
        return userName;
    }

    public void setUserName(java.lang.String userName) {
        this.userName = userName;
    }

    public java.lang.String getUserSurname() {
        return userSurname;
    }

    public void setUserSurname(java.lang.String userSurname) {
        this.userSurname = userSurname;
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

    public Map<String, String> getFavouritesIdList() {
        return favouritesIdList;
    }

    public void setFavouritesIdList(Map<String, String> favouritesIdList) {
        this.favouritesIdList = favouritesIdList;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
