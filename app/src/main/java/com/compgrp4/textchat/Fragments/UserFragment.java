package com.compgrp4.textchat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.compgrp4.textchat.Adapters.UsersAdapter;
import com.compgrp4.textchat.Models.UserObject;
import com.compgrp4.textchat.Models.Users;
import com.compgrp4.textchat.R;
import android.database.Cursor;
import android.provider.ContactsContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import se.simbio.encryption.Encryption;

public class UserFragment extends Fragment {

    private RecyclerView recyclerView;
    private UsersAdapter userAdapter;
    private ArrayList<Users> mUsers;
    Cursor cursor;
    String phone;
    int i=1;
    Encryption encryption;

    ArrayList<UserObject> userList, contactList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_user, container, false);
        recyclerView = view.findViewById(R.id.userRecyclarView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();
        contactList= new ArrayList<>();
        userList= new ArrayList<>();
        String key = "Sahil";
        String salt = "Sushant";
        byte[] iv = new byte[16];
        encryption = Encryption.getDefault(key, salt, iv);


        cursor = getActivity().getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (cursor.moveToNext()) {
            Log.d("susha", "while loop started ");

            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));


            if (phone.length() <= 10) {
                continue;
            }

            if (!(String.valueOf(phone.charAt(0)).equals("+"))){
                phone = "+91" + phone;
            }
            phone = phone.replace(" ", "");
            phone = phone.replace("-", "");
            phone = phone.replace("(", "");
            phone = phone.replace(")", "");

            final UserObject mContact = new UserObject("", name, phone);
            contactList.add(mContact);

            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
            Query query = mUserDB.orderByChild("phoneNumber").equalTo(mContact.getPhone());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("susha", "on data change method started");
                    // mUsers.clear();
                    if (dataSnapshot.exists()) {

                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            Log.d("susha", "chidren count"+dataSnapshot.getChildrenCount());
                                Users user = childSnapshot.getValue(Users.class);
                                if (!(user.getMail().equals(firebaseUser.getEmail()))) {
                                    if(i%5==1) {
                                        Log.d("susha1", "firebase user name " + firebaseUser.getUid());
                                        Log.d("susha1", "user added " + user.getUserId());
                                        Log.d("susha1", "user added " + user.getUserName());
                                        mUsers.add(user);
                                    }
                                    i=i+1;
                            }
                        }
                        userAdapter = new UsersAdapter(getContext(),mUsers,false,encryption);
                       recyclerView.setAdapter( userAdapter );
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        cursor.close();

        return view;
    }
}