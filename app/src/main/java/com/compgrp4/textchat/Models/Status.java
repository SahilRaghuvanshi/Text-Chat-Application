package com.compgrp4.textchat.Models;

public class Status {
    private String imageUrl,name;
    private long timestamp;

    public Status(String imageUrl, long timestamp) {
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public Status() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
