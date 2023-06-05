package com.compgrp4.textchat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Popup extends AppCompatActivity {
    public static ToggleButton translate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        getSupportActionBar().hide();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Log.d("sap", width+" "+height);
        getWindow().setLayout((int)(width*0.9),(int)(height*0.28));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y=(400*height)/1448;
        getWindow().setAttributes(params);
        ImageButton textrecog = findViewById(R.id.paperclip);
        ImageButton gallery = findViewById(R.id.gallery);
        ImageButton camera = findViewById(R.id.camera);
        ImageButton location = findViewById(R.id.location);
        ImageButton contact = findViewById(R.id.contact);
        translate = findViewById(R.id.toggleButton);
        TextView lang = findViewById(R.id.lang);
        boolean bool = getIntent().getExtras().getBoolean("toggle");
        if(bool){
            translate.setChecked(bool);
            lang.setText("Hindi");

        }
        else
        {
            translate.setChecked(bool);
            lang.setText("English");
        }
        textrecog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(90,intent);
                finish();
            }
        });
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Popup.this,ContactActivity.class);
                startActivityForResult(intent,78);
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(85,intent);
                finish();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(86,intent);
                finish();
            }
        });
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(87,intent);
                finish();
            }
        });
            translate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked)
                    {
                        Log.d("christ", "Translation Started");
                        Intent intent = new Intent();
                        intent.putExtra("togglestate",isChecked);
                        setResult(345,intent);
                        lang.setText("Hindi");
                        finish();
                    }
                    else
                    {
                        Log.d("christ", "Translation Started");
                        Intent intent = new Intent();
                        intent.putExtra("togglestate",isChecked);
                        setResult(346,intent);
                        finish();
                        lang.setText("English");
                    }
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 78:
                if(resultCode==55){
                   setResult(347,data);
                   finish();
                }
                break;
        }
    }
}