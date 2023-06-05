package com.compgrp4.textchat.Fragments;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.compgrp4.textchat.Adapters.UsersAdapter;
import com.compgrp4.textchat.Models.Chatlist;
import com.compgrp4.textchat.Models.Users;
import com.compgrp4.textchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import se.simbio.encryption.Encryption;

public class ChatFragment extends Fragment {

        private RecyclerView recyclerView;
        private UsersAdapter userAdapter;
        private List<Users> mUsers;
        FirebaseUser firebaseUser;
        DatabaseReference reference;
        private List<Chatlist> usersList;
        Encryption encryption;
        SwipeRefreshLayout refreshLayout;


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_chat, container, false);
            recyclerView = view.findViewById(R.id.chatRecyclarView);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String key = "Sahil";
            String salt = "Sushant";
            byte[] iv = new byte[16];
            encryption = Encryption.getDefault(key, salt, iv);
            refreshLayout= view.findViewById(R.id.refresh);
            usersList = new ArrayList<>();
            recyclerView.getItemAnimator().setChangeDuration(0);
                    FirebaseDatabase.getInstance().getReference().child("Chatlist").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            usersList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Chatlist chatlist = snapshot.getValue(Chatlist.class);
                                usersList.add(chatlist);
                                Log.d("tommy", "Before......");
                                for(Chatlist c : usersList) {
                                    String str = c.getDatetime();
                                    try {
                                        Log.d("tommy", c.getId() + "   " + Long.parseLong(str) + "   ");
                                    }
                                    catch(Exception e){
                                        Log.d("erroron", "Number format "+e);
                                    }
                                }
                                try {
                                    int i = 0, j = 0;
                                    for (i = 0; i < (usersList.size() - 1); i++) {
                                        for (j = 0; j < usersList.size() - i - 1; j++) {
                                            if (Long.parseLong(usersList.get(j).getDatetime()) < Long.parseLong(usersList.get(j + 1).getDatetime())) {
                                                Chatlist temp = usersList.get(j);
                                                usersList.set(j, usersList.get(j + 1));
                                                usersList.set(j + 1, temp);
                                            }
                                        }
                                    }
                                }
                                catch(Exception e){
                                    Log.d("erroron", "Number format "+e);
                                }
                                Log.d("tommy", "After......");
                                for(Chatlist c : usersList) {
                                    String str = c.getDatetime();
                                    try {
                                        Log.d("tommy", c.getId() + "   " + Long.parseLong(str) + "   ");
                                    }
                                    catch(Exception e){
                                        Log.d("erroron", "Number format "+e);
                                    }
                                }
                            }
                            mUsers = new ArrayList<>();
                            FirebaseDatabase.getInstance().getReference("Users").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    mUsers.clear();
                                    for(Chatlist chatlist : usersList){

                                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                            Users user = snapshot.getValue(Users.class);
                                            if (user.getUserId().equals(chatlist.getId())) {
                                                mUsers.add(user);
                                            }
                                        }

                                    }
                                    userAdapter = new UsersAdapter(getContext(), mUsers, true,encryption);
                                    recyclerView.setAdapter(userAdapter);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            userAdapter.notifyDataSetChanged();
                            refreshLayout.setRefreshing(false);
                        }
                    });
            return view;
        }
    }