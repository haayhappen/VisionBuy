package com.google.sample.cloudvision;

/**
 * Created by uidp0609 on 23.06.2017.
 */

public class IntroCard {

    private String title;
    private int thumbnail;

    public IntroCard(){
    }

    public IntroCard(String title,int thumbnail){
        this.title = title;
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }
}
