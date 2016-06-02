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
    private int availability_qty;
    private boolean isSelected;

//    public Dish(Parcel in)
//    {
//        this.ID = in.readString();
//        this.name = in.readString();
//        this.description= in.readString();
//        this.photoPath= in.readString();
//        this.price = in.readDouble();
//        this.availability_qty = in.readInt();
//        this.isSelected = in.readByte() != 0;
//    }

    public Dish(String ID, String name, String description, String photoPath, double price, int availability_qty, boolean selected)
    {
        this.ID = ID;
        this.name = name;
        this.description= description;
        this.photoPath= photoPath;
        this.price = price;
        this.availability_qty = availability_qty;
        this.isSelected = selected;
    }

    public Dish() {

    }

    public String getID()
    {
        return ID;
    }

    public void setID(String ID)
    {
        this.ID = ID;
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

    public int getAvailability_qty()
    {
        return availability_qty;
    }

    public void setAvailability_qty(int availability_qty)
    {
        this.availability_qty = availability_qty;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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
//        dest.writeInt(availability_qty);
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
