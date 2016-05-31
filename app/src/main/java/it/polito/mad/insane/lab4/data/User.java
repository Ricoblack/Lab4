package it.polito.mad.insane.lab4.data;

import java.util.List;

/**
 * Created by carlocaramia on 25/05/16.
 */
public class User {

    //campi del db
    private String userId;
    private String userName;
    private String userSurname;
    private List<String> reviewsIdList;
    private List<String> bookingsIdList;
    private List<String> favouritesIdList;

    public User(String userId,String userName,String userSurname,List<String> reviewsIdList,List<String> bookingsIdList,List<String>favouritesIdList){
        this.userId=userId;
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

    public List<java.lang.String> getReviewsIdList() {
        return reviewsIdList;
    }

    public void setReviewsIdList(List<java.lang.String> reviewsIdList) {
        this.reviewsIdList = reviewsIdList;
    }

    public List<String> getBookingsIdList() {
        return bookingsIdList;
    }

    public void setBookingsIdList(List<String> bookingsIdList) {
        this.bookingsIdList = bookingsIdList;
    }

    public List<java.lang.String> getFavouritesIdList() {
        return favouritesIdList;
    }

    public void setFavouritesIdList(List<java.lang.String> favouritesIdList) {
        this.favouritesIdList = favouritesIdList;
    }
}
