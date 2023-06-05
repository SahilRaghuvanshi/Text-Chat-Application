package com.compgrp4.textchat.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.compgrp4.textchat.MainActivity;
import com.compgrp4.textchat.Models.Status;
import com.compgrp4.textchat.Models.UserStatus;
import com.compgrp4.textchat.R;
import com.compgrp4.textchat.databinding.FragmentStatusBinding;
import com.devlomi.circularstatusview.CircularStatusView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {

    Context context;
    ArrayList<UserStatus> userStatuses;

    public StatusAdapter(Context context, ArrayList<UserStatus> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
    }

    @NonNull
    @NotNull
    @Override
    public StatusViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.status, parent, false);
        return new StatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull StatusAdapter.StatusViewHolder holder, int position) {
        UserStatus userStatus = userStatuses.get(position);
        if(userStatus.getStatuses().size()!=0) {
            Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size() - 1);
            circularStatusView.setPortionsCount(userStatus.getStatuses().size());
            Glide.with(context).load(lastStatus.getImageUrl()).into(image);
            name.setText(userStatus.getName());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               ArrayList<MyStory> myStories = new ArrayList<>();
                for(Status status : userStatus.getStatuses()) {
                    myStories.add(new MyStory(status.getImageUrl()));
                }
                new StoryView.Builder(((MainActivity)context).getSupportFragmentManager())
                        .setStoriesList(myStories) // Required
                        .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                        .setTitleText(userStatus.getName()) // Default is Hidden
                        .setSubtitleText("") // Default is Hidden
                        .setTitleLogoUrl(userStatus.getProfileimage()) // Default is Hidden
                        .setStoryClickListeners(new StoryClickListeners() {
                            @Override
                            public void onDescriptionClickListener(int position) {
                                //your action
                            }
                            @Override
                            public void onTitleIconClickListener(int position) {
                                //your action
                            }
                        }) // Optional Listeners
                        .build() // Must be called before calling show method
                        .show();
                Log.d("donotdisturb", userStatus.getProfileimage()+"");
            }
        });

    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }
    ImageView image;
    TextView name;
    CircularStatusView circularStatusView;
    public class StatusViewHolder extends RecyclerView.ViewHolder{
        public StatusViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            circularStatusView=itemView.findViewById(R.id.circular_status_view);
            image = itemView.findViewById(R.id.status);
            name = itemView.findViewById(R.id.name);
        }
    }
}
