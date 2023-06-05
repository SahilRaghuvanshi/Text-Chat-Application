package com.compgrp4.textchat.Models;

public class Users {

    String profilepic , userName , mail , password , userId , lastMessage,phoneNumber,status,about;
//    public Users(String profilepic, String userName, String mail, String password, String userId, String lastMessage) {
//        this.profilepic = profilepic;
//        this.userName = userName;
//        this.mail = mail;
//        this.password = password;
//        this.userId = userId;
//        this.lastMessage = lastMessage;
//    }
    public Users(){}

    //SignUp Constructor

    public Users(String userId,String userName, String mail, String password,String phoneNumber,String status) {
        this.userId=userId;
        this.userName = userName;
        this.mail = mail;
        this.password = password;
        this.phoneNumber=phoneNumber;
        this.status = status;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public String getUserName() {
        return userName;
    }

    public String getMail() {
        return mail;
    }

    public String getPassword() {
        return password;
    }

    public String getUserId(String key) {
        return userId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
