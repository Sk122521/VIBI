package com.example.pollinguserapp.model;

import java.security.Key;

public class Item {
    private String title;
    private String description;
    private String gd;
    private String noofvotes;
    private String image;

    public Item(String title, String description, String image , String noofvotes , String gd) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.noofvotes = noofvotes;
        this.gd  = gd;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public Item() {
        // Default constructor required for Firebase
    }

    // Getters and setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public String getGd() {
//        return gd;
//    }
//
    public String getNoofvotes() {
        return noofvotes;
    }

    public void setNoofvotes(String noofvotes) {
        this.noofvotes = noofvotes;
    }

    public String getGd() {
        return gd;
    }

    public void setGd(String gd) {
        this.gd = gd;
    }

}

