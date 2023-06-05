package com.compgrp4.textchat.Models;

import java.util.ArrayList;

public class UserStatus {
    private String name,profileimage;
    private long lastupdated;
    private ArrayList<Status> statuses;

    public UserStatus() {
    }

    public UserStatus(String name,long lastupdated, ArrayList<Status> statuses) {
        this.name = name;
        this.lastupdated = lastupdated;
        this.statuses = statuses;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastupdated() {
        return lastupdated;
    }

    public void setLastupdated(long lastupdated) {
        this.lastupdated = lastupdated;
    }

    public ArrayList<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<Status> statuses) {
        this.statuses = statuses;
    }
}
