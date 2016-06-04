package it.polito.mad.insane.lab4.data;

import java.io.Serializable;

/**
 * Created by carlocaramia on 08/04/16.
 */
public class Dish implements Serializable /*, Parcelable*/{

    //campi del db
    private String ID;
    private String name;
    private String description;
    private String photoPath;
    private double price;
    private int availabilityQty;
//    private boolean isSelected;

//    public Dish(Parcel in)
//    {
//        this.ID = in.readString();
//        this.name = in.readString();
//        this.description= in.readString();
//        this.photoPath= in.readString();
//        this.price = in.readDouble();
//        this.availabilityQty = in.readInt();
//        this.isSelected = in.readByte() != 0;
//    }

    public Dish(String name, String description, String photoPath, double price, int availabilityQty, boolean selected)
    {
//        this.ID = ID;
        this.name = name;
        this.description= description;
        this.photoPath= photoPath;
        this.price = price;
        this.availabilityQty = availabilityQty;
//        this.isSelected = selected;
    }

    public Dish() {

    }


    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getPhotoPath()
    {
        return this.photoPath;
    }

    public void setPhotoPath(String photo_name)
    {
        this.photoPath = photo_name;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice (double price)
    {
        this.price = price;
    }

    public int getAvailabilityQty()
    {
        return availabilityQty;
    }

    public void setAvailabilityQty(int availabilityQty)
    {
        this.availabilityQty = availabilityQty;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }

//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(ID);
//        dest.writeString(name);
//        dest.writeString(description);
//        dest.writeString(photoPath);
//        dest.writeDouble(price);
//        dest.writeInt(availabilityQty);
//        dest.writeByte((byte) (isSelected ? 1 : 0));
////        readFromParcel:
////        myBoolean = in.readByte() != 0;
//    }

//    public static final Parcelable.Creator<Dish> CREATOR = new Parcelable.Creator<Dish>()
//    {
//        public Dish createFromParcel(Parcel in)
//        {
//            return new Dish(in);
//        }
//        public Dish[] newArray(int size)
//        {
//            return new Dish[size];
//        }
//    };
}
