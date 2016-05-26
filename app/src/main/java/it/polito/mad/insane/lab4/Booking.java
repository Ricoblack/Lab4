package it.polito.mad.insane.lab4;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by miche on 06/04/2016.
 */
public class Booking implements Serializable,  Comparable<Booking>
{

    private String ID;
    private Calendar date_time;
    /** N.B. "dishes" and "quantities" have a 1v1 matching! don't reorder/modify individually! **/
    private List<Dish> dishes = new ArrayList<>();
    private List<Integer> quantities = new ArrayList<>(); // quantity reserved for each dish
    private String note;
    private String restaurantID;
    private double totalPrice;


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }


    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public Calendar getDate_time() {
        return date_time;
    }

    public void setDate_time(Calendar date_time) {
        this.date_time = date_time;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public int compareTo(Booking another) {
        return this.getDate_time().compareTo(another.getDate_time());
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
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
}
