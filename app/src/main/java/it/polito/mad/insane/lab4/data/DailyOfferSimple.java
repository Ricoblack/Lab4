package it.polito.mad.insane.lab4.data;

/**
 * Created by carlocaramia on 07/06/16.
 */
public class DailyOfferSimple {

    private String restaurantId;
    private String restaurantName;
    private String offerText;
    private String ID;

    public DailyOfferSimple(){}

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
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
}
