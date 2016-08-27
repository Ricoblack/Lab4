package it.polito.mad.insane.lab4.data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Renato on 27/08/2016.
 */
public class Cart implements Serializable {
    private HashMap<Dish, Integer> dishesQuantityMap; // mappa che contiene le quantita' selezionate di ogni piatto del menu'
    private HashMap<DailyOffer, Integer> offersQuantityMap; // mappa che contiene le quantita' selezionate di ogni dailyOffer del menu'
    private  int reservationQty; // quantita' totale di item presenti nella prenotazione in esame
    private  double reservationPrice; //prezzo totale degli item presenti nella prenotazione in esame

    public Cart(){
        dishesQuantityMap = new HashMap<>();
        offersQuantityMap = new HashMap<>();
        reservationQty = 0;
        reservationPrice = 0;
    }

    public HashMap<Dish, Integer> getDishesQuantityMap() {
        return dishesQuantityMap;
    }

    public void setDishesQuantityMap(HashMap<Dish, Integer> quantitiesMap) {
        this.dishesQuantityMap = quantitiesMap;
    }

    public HashMap<DailyOffer, Integer> getOffersQuantityMap() {
        return offersQuantityMap;
    }

    public void setOffersQuantityMap(HashMap<DailyOffer, Integer> offersMap) {
        this.offersQuantityMap = offersMap;
    }

    public int getReservationQty() {
        return reservationQty;
    }

    public void setReservationQty(int reservationQty) {
        this.reservationQty = reservationQty;
    }

    public double getReservationPrice() {
        return reservationPrice;
    }

    public void setReservationPrice(double reservationPrice) {
        this.reservationPrice = reservationPrice;
    }
}
