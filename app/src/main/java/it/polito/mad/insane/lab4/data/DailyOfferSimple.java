package it.polito.mad.insane.lab4.data;

/**
 * Created by carlocaramia on 07/06/16.
 */
public class DailyOfferSimple {

    private String restaurantId;
    private String restaurantName;
    private String description;
    private String ID;
    private boolean isRead;

    public DailyOfferSimple(){}

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
