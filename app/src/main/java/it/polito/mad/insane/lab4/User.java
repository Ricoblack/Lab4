package it.polito.mad.insane.lab4;

import java.util.List;

/**
 * Created by carlocaramia on 25/05/16.
 */
public class User {

    private String userId;
    private String userName;
    private String userSurname;
    private List<String> reviewIds;
    private List<Booking> bookingIds;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserSurname() {
        return userSurname;
    }

    public void setUserSurname(String userSurname) {
        this.userSurname = userSurname;
    }

    public List<String> getReviewIds() {
        return reviewIds;
    }

    public void setReviewIds(List<String> reviewIds) {
        this.reviewIds = reviewIds;
    }

    public List<Booking> getBookingIds() {
        return bookingIds;
    }

    public void setBookingIds(List<Booking> bookingIds) {
        this.bookingIds = bookingIds;
    }
}
