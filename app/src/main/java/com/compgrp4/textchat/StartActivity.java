package com.compgrp4.textchat;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;
    @Override
    protected void onStart() {
        super.onStart();
        Thread thread=new Thread()
        {
            @Override
            public void run() {
                try
                {
                    sleep(2500);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                finally {
                    Intent mainIntent=new Intent(StartActivity.this,SignInActivity.class);
                    startActivity(mainIntent);
                }
            }
        };
        thread.start();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        getSupportActionBar().hide();
        }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}