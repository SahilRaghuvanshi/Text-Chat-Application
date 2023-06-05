package com.compgrp4.textchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.compgrp4.textchat.Models.Users;
import com.compgrp4.textchat.databinding.ActivitySettingsBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
ActivitySettingsBinding binding;
FirebaseDatabase database;
FirebaseAuth auth;
FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        storage = FirebaseStorage.getInstance("gs://myfirstapp-7f0c0.appspot.com");
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
finish();
            }
        });
        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 33);
            }
        });
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                Picasso.get().load(user.getProfilepic()).placeholder(R.drawable.avatar).into(binding.profileImage);
                binding.etuserName.setText(user.getUserName());
                binding.etStatus.setText(user.getAbout());
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        binding.savebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.etuserName.getText().toString();
                String status = binding.etStatus.getText().toString();
                HashMap<String ,Object> obj = new HashMap<>();
                obj.put("userName", username);
                obj.put("about", status);
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).updateChildren(obj);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getData() != null) {

                Uri sfile = data.getData();
                binding.profileImage.setImageURI(sfile);
                final StorageReference reference = storage.getReference().child("profile pictures").child(FirebaseAuth.getInstance().getUid());
                reference.putFile(sfile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("profilepic").setValue(uri.toString());
                                Toast.makeText(SettingsActivity.this, "profile image updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }
    }
}