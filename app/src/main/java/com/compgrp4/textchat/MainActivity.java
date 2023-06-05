package com.compgrp4.textchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.compgrp4.textchat.Adapters.FragmentsAdapter;
import com.compgrp4.textchat.Adapters.UsersAdapter;
import com.compgrp4.textchat.Models.Users;
import com.compgrp4.textchat.databinding.ActivityChatDetailBinding;
import com.compgrp4.textchat.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.sinch.android.rtc.SinchError;

import java.util.HashMap;

public class MainActivity extends BaseActivity {

    ActionBar actionBar;
    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        user=FirebaseAuth.getInstance().getCurrentUser();
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        actionBar=getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2b6cee")));
        binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
        binding.tablayout.setupWithViewPager(binding.viewPager);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Alert !");
        alertDialog.setMessage("Do you want to start calling feature ?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if ((getSinchServiceInterface() == null) || (!getSinchServiceInterface().isStarted())) {
                    getSinchServiceInterface().startClient(FirebaseAuth.getInstance().getCurrentUser().getUid()+"video");
                }
                Toast.makeText(MainActivity.this, "Calling service turned ON", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(MainActivity.this, "No clicked", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.show();

 }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onDestroy() {
        if (getSinchServiceInterface() != null) {
            getSinchServiceInterface().stopClient();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    private void status(String status) {

                                reference = FirebaseDatabase.getInstance().getReference( "Users" ).child( user.getUid() );
                                HashMap<String, Object> hashMap = new HashMap<>();
                                Log.d("tata", "child updated");
                                Log.d("tata", status);
                                hashMap.put( "status", status );
                                reference.updateChildren( hashMap );
    }

    @Override
    protected void onResume() {
        Log.d("tata","onResume");
        super.onResume();
        status( "online" );
    }

    @Override
    protected void onPause() {
        Log.d("tata","onPause");
        super.onPause();
        status( "offline" );
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.settings:
                Intent i = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.logout:
                auth.signOut();
                Intent intent = new Intent(MainActivity.this,SignInActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }

}