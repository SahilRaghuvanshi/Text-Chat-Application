package com.compgrp4.textchat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.compgrp4.textchat.ChatDetailActivity;
import com.compgrp4.textchat.Models.Users;
import com.compgrp4.textchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import se.simbio.encryption.Encryption;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private List<Users> list;
    private Context context;
    private boolean ischat;
    private String theLastMessage;
    Context thiscontext;
    Encryption encryption;

    public UsersAdapter(Context context, List<Users> list, boolean ischat,Encryption encryption) {
        this.list = list;
        this.context = context;
        this.ischat = ischat;
        this.encryption=encryption;
    }

    public UsersAdapter() {
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
        thiscontext=parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UsersAdapter.ViewHolder holder, int position) {
        Users users = list.get(position);
        holder.setIsRecyclable(false);
        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + users.getUserId();
        Picasso.get().load(users.getProfilepic()).placeholder(R.drawable.avatar).into(holder.image);
        holder.userName.setText(users.getUserName());
        FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                    String decrypted = encryption.decryptOrNull(lastMsg);
                    Log.d("god", encryption.decryptOrNull(lastMsg));

                    long time = snapshot.child("lastMsgTime").getValue(long.class);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    holder.time.setText(dateFormat.format(new Date(time)));

                    if(decrypted.length()>35){

                        holder.lastMessage.setText(decrypted.substring(0,34)+"...");
                    }
                    else {
                        holder.lastMessage.setText(decrypted);
                    }

                }
                else
                {
                    holder.lastMessage.setText("Tap to chat");
                }

            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        if (ischat) {
            if (users.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }
        else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }
        //
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatDetailActivity.class);
                intent.putExtra("userid", users.getUserId());
                intent.putExtra("profilePic", users.getProfilepic());
                intent.putExtra("userName", users.getUserName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView userName, lastMessage,time;
        private ImageView img_on;
        private ImageView img_off;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userNameList);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            time = itemView.findViewById(R.id.time);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
        }
    }
}
