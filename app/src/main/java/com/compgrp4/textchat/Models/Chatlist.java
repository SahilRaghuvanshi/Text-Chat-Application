package com.compgrp4.textchat.Models;

import java.util.Comparator;

public class Chatlist  {
    public String id;
    public String datetime;

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Chatlist(String id) {
        this.id = id;
    }


    public Chatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    }
