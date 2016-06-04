package it.polito.mad.insane.lab4.data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by miche on 06/04/2016.
 */
public class Booking implements Serializable, Comparable<Booking>
{


    //private Calendar date_time;
//    /** N.B. "dishes" and "quantities" have a 1v1 matching! don't reorder/modify individually! **/
//    private List<Dish> dishes = new ArrayList<>();
//    private List<Integer> quantities = new ArrayList<>(); // quantity reserved for each dish
    private HashMap<String, Integer>  dishesIdMap; // <ID, Qty>
    private String notes;
    private double totalDiscount; // it is the sum of the discounts of each (optional) daily offer in the booking // TODO: gestire questo attributo
    private double totalPrice;
    private int totalDishesQty;
    private String dateTime;
    private String userId;
    private String restaurantId;
    private String ID;
//    private Map<String, String> dishesIdList;


    /*
    public Calendar getDate_time() {
        return date_time;
    }

    public void setDate_time(Calendar date_time) {
        this.date_time = date_time;
    }
    */


//    public List<Integer> getQuantities() {
//        return quantities;
//    }
//
//    public void setQuantities(List<Integer> quantities) {
//        this.quantities = quantities;
//    }
//
//    public int getTotalDishesQty()
//    {
//        int result = 0;
//
//        for(Integer a: this.quantities)
//            result += a;
//
//        return result;
//    }
//
//    public Map<String, String> getDishesIdList() {
//        return dishesIdList;
//    }
//
//    public void setDishesIdList(Map<String, String> dishesIdList) {
//        this.dishesIdList = dishesIdList;
//    }




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


    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalDishesQty() {
        return totalDishesQty;
    }

    public void setTotalDishesQty(int totalDishesQty) {
        this.totalDishesQty = totalDishesQty;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDateTime() {
        return this.dateTime;
    }

    public HashMap<String, Integer> getDishesIdMap() {
        return dishesIdMap;
    }

    public void setDishesIdMap(HashMap<String, Integer> dishesIdMap) {
        this.dishesIdMap = dishesIdMap;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
