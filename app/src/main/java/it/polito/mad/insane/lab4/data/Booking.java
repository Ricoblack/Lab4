package it.polito.mad.insane.lab4.data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by miche on 06/04/2016.
 */
public class Booking implements Serializable, Comparable<Booking>
{


    //private Calendar date_time;
    /** N.B. "dishes" and "quantities" have a 1v1 matching! don't reorder/modify individually! **/
    private List<Dish> dishes = new ArrayList<>();
    private List<Integer> quantities = new ArrayList<>(); // quantity reserved for each dish
    private java.lang.String note;
    private java.lang.String restaurantID;
    private double totalPrice;


    //campi del db
    private java.lang.String ID;
    private Map<String, String> dishesIdList;
    private String userId;
    private String dateTime;

    public java.lang.String getID() {
        return ID;
    }

    public void setID(java.lang.String ID) {
        this.ID = ID;
    }


    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    /*
    public Calendar getDate_time() {
        return date_time;
    }

    public void setDate_time(Calendar date_time) {
        this.date_time = date_time;
    }
    */

    public java.lang.String getNote() {
        return note;
    }

    public void setNote(java.lang.String note) {
        this.note = note;
    }


    public java.lang.String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(java.lang.String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double price){
        this.totalPrice = price;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public void setQuantities(List<Integer> quantities) {
        this.quantities = quantities;
    }

    public int getTotalDishesQty()
    {
        int result = 0;

        for(Integer a: this.quantities)
            result += a;

        return result;
    }

    public Map<String, String> getDishesIdList() {
        return dishesIdList;
    }

    public void setDishesIdList(Map<String, String> dishesIdList) {
        this.dishesIdList = dishesIdList;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }



    @Override
    public int compareTo(Booking another) {
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(parser.parse(dateTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar cal2 = Calendar.getInstance();
        try {
            cal.setTime(parser.parse(another.getDateTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return cal.compareTo(cal2);
    }
}
