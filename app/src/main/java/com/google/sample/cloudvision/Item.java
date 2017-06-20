package com.google.sample.cloudvision;

import java.io.Serializable;

/**
 * Created by Fynn on 20.06.2017.
 */

public class Item implements Serializable {
    public String title;
    public String brand;
    public String foramattedPrice;
    public String imageURL;

    public Item(String title, String brand, String foramattedPrice, String imageURL) {
        this.title = title;
        this.brand = brand;
        this.foramattedPrice = foramattedPrice;
        this.imageURL = imageURL;
    }


}
