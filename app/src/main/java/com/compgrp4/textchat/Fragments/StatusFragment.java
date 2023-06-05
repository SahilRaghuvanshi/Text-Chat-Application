package com.compgrp4.textchat.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.compgrp4.textchat.Adapters.StatusAdapter;
import com.compgrp4.textchat.ChatDetailActivity;
import com.compgrp4.textchat.MainActivity;
import com.compgrp4.textchat.Models.Status;
import com.compgrp4.textchat.Models.UserObject;
import com.compgrp4.textchat.Models.UserStatus;
import com.compgrp4.textchat.Models.Users;
import com.compgrp4.textchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class StatusFragment extends Fragment {

    private RecyclerView recyclerView;
    private StatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatuses;
    ProgressDialog progressDialog;
    Users user;
    FirebaseDatabase database;
    CircleImageView circleImageView;
    Cursor cursor;
    String phone;
    ArrayList<UserObject> userList, contactList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);
        recyclerView = view.findViewById(R.id.StatusRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        database = FirebaseDatabase.getInstance();
        userStatuses = new ArrayList<>();
        circleImageView = view.findViewById(R.id.statusprofile);
        contactList = new ArrayList<>();

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                user = snapshot.getValue(Users.class);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                Picasso.get().load(user.getProfilepic()).placeholder(R.drawable.avatar).into(circleImageView);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
            database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userStatuses.clear();
                        for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                            UserStatus status = new UserStatus();
                            status.setName(storySnapshot.child("name").getValue(String.class));
                            status.setProfileimage(storySnapshot.child("profileimage").getValue(String.class));
//                        status.setLastupdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                            ArrayList<Status> statuses = new ArrayList<>();

                            for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                                Status sampleStatus = statusSnapshot.getValue(Status.class);
                                statuses.add(sampleStatus);
                            }
                            status.setStatuses(statuses);
                            userStatuses.add(status);
                        }
                        statusAdapter = new StatusAdapter(getContext(), userStatuses);
                        recyclerView.setAdapter(statusAdapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            statusAdapter = new StatusAdapter(getContext(), userStatuses);
            recyclerView.setAdapter(statusAdapter);
            TextView addstatus = view.findViewById(R.id.addstatus);
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Uploading Image...");
            progressDialog.setCancelable(false);

            addstatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, 75);
                }
            });
            ImageView trash = view.findViewById(R.id.trash);
            trash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle("Alert !");
                    alertDialog.setMessage("Do you want delete your status ?");
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            FirebaseDatabase.getInstance().getReference().child("stories").child(FirebaseAuth.getInstance().getUid()).removeValue();
                        }
                    });
                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                }
            });
            return view;
        }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null){
            progressDialog.show();
            if(data.getData() != null){
                FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference = firebaseStorage.getReference().child("status").child(date.getTime()+"");
                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UserStatus userStatus = new UserStatus();
                                    userStatus.setName(user.getUserName());
                                    Log.d("ran", user.getUserName()+" "+user.getProfilepic());
                                    userStatus.setProfileimage(user.getProfilepic());
                                    userStatus.setLastupdated(new Date().getTime());
                                    HashMap<String,Object> obj = new HashMap<>();
                                    obj.put("name",userStatus.getName());
                                    obj.put("profileimage",userStatus.getProfileimage());
                                    obj.put("lastupdated",userStatus.getLastupdated());
                                    String imageUrl = uri.toString();
                                    Status status = new Status(imageUrl,userStatus.getLastupdated());
                                    database.getReference().child("stories").child(FirebaseAuth.getInstance().getUid()).updateChildren(obj);
                                    database.getReference().child("stories").child(FirebaseAuth.getInstance().getUid()).child("statuses").push().setValue(status);
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        }
    }
}