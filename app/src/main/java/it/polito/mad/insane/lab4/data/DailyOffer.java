package it.polito.mad.insane.lab4.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by miche on 03/06/2016.
 */
public class DailyOffer implements Serializable
{

    private String ID;
    private String name;
    private String description;
    private Map<String, Integer> dishesIdMap = new HashMap<>(); // <dishID, quantity>
    private double price;
    private double discount;
    private int availableQuantity;

    public DailyOffer(){}

    public DailyOffer(String id, String name, String description, double price, int availableQuantity)
    {
//        ID = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.availableQuantity = availableQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Map<String, Integer> getDishesIdMap() {
        return dishesIdMap;
    }

    public void setDishesIdMap(Map<String, Integer> dishesIdMap) {
        this.dishesIdMap = dishesIdMap;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}
