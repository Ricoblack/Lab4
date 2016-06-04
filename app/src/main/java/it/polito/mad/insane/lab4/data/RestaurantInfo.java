package it.polito.mad.insane.lab4.data;

import java.util.Date;

/**
 * Created by carlocaramia on 08/04/16.
 */
public class RestaurantInfo {

    private String additionalServices;
    private String address;
    private String closingHour;
    private String cuisineType;
    private String description;
    private String nearbyUniversity;
    private String openingHour;
    private String paymentMethod;
    private String restaurantName;
    private String timeInfo;

    //TODO aggiungerlo via codice (Renato)
    //private String imagePath;



    public RestaurantInfo(String restaurantName, String address, String nearbyUniversity, String cuisineType,
                          String description, String openingHour, String closingHour, String timeInfo, String paymentMethod,
                          String additionalServices){

        this.restaurantName = restaurantName;
        this.address=address;
        this.openingHour=openingHour;
        this.closingHour=closingHour;
        this.nearbyUniversity = nearbyUniversity;
        this.cuisineType = cuisineType;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.timeInfo = timeInfo;
        this.paymentMethod = paymentMethod;
        this.additionalServices = additionalServices;
        this.description = description;
    }

    public RestaurantInfo() {}

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(String openingHour) {
        this.openingHour = openingHour;
    }

    public String getClosingHour() {
        return closingHour;
    }

    public void setClosingHour(String closingHour) {
        this.closingHour = closingHour;
    }


    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getNearbyUniversity() {
        return nearbyUniversity;
    }

    public void setNearbyUniversity(String nearbyUniversity) {
        this.nearbyUniversity = nearbyUniversity;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public String getTimeInfo() {
        return timeInfo;
    }

    public void setTimeInfo(String timeInfo) {
        this.timeInfo = timeInfo;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getAdditionalServices() {
        return additionalServices;
    }

    public void setAdditionalServices(String additionalServices) {
        this.additionalServices = additionalServices;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
