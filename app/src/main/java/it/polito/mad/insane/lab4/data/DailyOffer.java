package it.polito.mad.insane.lab4.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by miche on 03/06/2016.
 */
public class DailyOffer
{

    private String ID;
    private String name;
    private String description;
    private Map<String, String> dishes = new HashMap<String,String>(); // <dishID,dishID>
    private double price;

    public DailyOffer(){}

    public DailyOffer(String id, String name, String description, double price)
    {
        ID = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String,String> getDishes() {
        return dishes;
    }

    public void setDishes(Map<String,String> dishes) {
        this.dishes = dishes;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
