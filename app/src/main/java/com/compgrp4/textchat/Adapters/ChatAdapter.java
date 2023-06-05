package com.compgrp4.textchat.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.compgrp4.textchat.Models.MessageModel;
import com.compgrp4.textchat.R;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import se.simbio.encryption.Encryption;

public class ChatAdapter extends RecyclerView.Adapter{
    ArrayList<MessageModel> messageModels;
    Context context;
    int SENDER_VIEW_TYPE=1;
    int RECEIVER_VIEW_TYPE=2;
    int i=-1;
    TextToSpeech textToSpeech;
    String senderRoom, recieverRoom;
    Encryption encryption;
    int k=0;

    public ChatAdapter(ArrayList<MessageModel> messageModels, Context context,String senderRoom,String recieverRoom,Encryption encryption) {
        this.messageModels = messageModels;
        this.context = context;
        this.senderRoom=senderRoom;
        this.recieverRoom=recieverRoom;
        this.encryption=encryption;
    }

    public ChatAdapter() {
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if(viewType==SENDER_VIEW_TYPE)
        {
            View view= LayoutInflater.from(context).inflate(R.layout.sample_sender,parent,false);
            textToSpeech = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status==TextToSpeech.SUCCESS)
                    {
                        int lang = textToSpeech.setLanguage(Locale.ENGLISH);
                    }
                }
            });
            return new SenderViewHolder(view);
        }
        else
            {
                View view= LayoutInflater.from(context).inflate(R.layout.sample_receiver,parent,false);
                textToSpeech = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status==TextToSpeech.SUCCESS)
                        {
                            int lang = textToSpeech.setLanguage(Locale.ENGLISH);
                        }
                    }
                });
                return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(messageModels.get(position).getuId().equals(FirebaseAuth.getInstance().getUid()))
        {
                return SENDER_VIEW_TYPE;
        }
        else
        {
                return RECEIVER_VIEW_TYPE;
        }
    }
    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel = messageModels.get(position);
        String decrypted = encryption.decryptOrNull(messageModel.getMessage());
        holder.setIsRecyclable(false);
        int[] reactions = {
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions).build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            Log.d("reaction", "pos is "+ pos);
            if(holder.getClass()==SenderViewHolder.class) {
                ((SenderViewHolder)holder).feeling.setVisibility(View.VISIBLE);
                if(pos==0){
                    ((SenderViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_like);
                }
                else if(pos==1){
                    ((SenderViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_love);
                }
                else if(pos==2){
                    ((SenderViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_laugh);
                }
                else if(pos==3){
                    ((SenderViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_wow);
                }
                else if(pos==4){
                    ((SenderViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_sad);
                }
                else if(pos==5){
                    ((SenderViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_angry);
                }
                else
                {
                }
            }
            else
            {
                ((ReceiverViewHolder)holder).feeling.setVisibility(View.VISIBLE);
                if(pos==0){
                    ((ReceiverViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_like);
                }
                else if(pos==1){
                    ((ReceiverViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_love);
                }
                else if(pos==2){
                    ((ReceiverViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_laugh);
                }
                else if(pos==3){
                    ((ReceiverViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_wow);
                }
                else if(pos==4){
                    ((ReceiverViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_sad);
                }
                else if(pos==5){
                    ((ReceiverViewHolder)holder).feeling.setImageResource(R.drawable.ic_fb_angry);
                }
                else
                {
                }
            }
            messageModel.setFeeling(pos);
            FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).child("messages").child(messageModel.getMessageId()).setValue(messageModel);
            FirebaseDatabase.getInstance().getReference().child("chats").child(recieverRoom).child("messages").child(messageModel.getMessageId()).setValue(messageModel);
            return true;
        });


        if(holder.getClass()==SenderViewHolder.class)
        {
            if(decrypted.equals("currentlocation")&&(messageModel.getLatitute()!=0)&&(messageModel.getLongitude()!=0))
            {
                Log.d("checker", decrypted);
                ((SenderViewHolder)holder).currentlocation.setVisibility(View.VISIBLE);
                ((SenderViewHolder)holder).sendermessage123.setVisibility(View.GONE);
                Glide.with(context).load(R.drawable.googlemaps).placeholder(R.drawable.googlemaps).into(((SenderViewHolder)holder).currentlocation);
                ((SenderViewHolder)holder).currentlocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=loc:%f,%f", messageModel.getLatitute(),messageModel.getLongitude());
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        context.startActivity(intent);
                    }
                });
            }
            else if(decrypted.equals("photo")&&(messageModel.getImageUrl()!=""))
            {
                Log.d("checker", decrypted);
                ((SenderViewHolder)holder).image.setVisibility(View.VISIBLE);
                ((SenderViewHolder)holder).sendermessage123.setVisibility(View.GONE);
                Glide.with(context).load(messageModel.getImageUrl()).placeholder(R.drawable.imageph).into(((SenderViewHolder)holder).image);
            }
            else
            {
                ((SenderViewHolder)holder).image.setVisibility(View.GONE);
                ((SenderViewHolder)holder).currentlocation.setVisibility(View.GONE);
                ((SenderViewHolder)holder).sendermessage123.setVisibility(View.VISIBLE);
            }
            ((SenderViewHolder)holder).senderMsg.setText(decrypted);
            long time = messageModel.getTimestamp();
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            ((SenderViewHolder)holder).senderTime.setText(dateFormat.format(new Date(time)));
            if(messageModel.getFeeling()>=0){
                ((SenderViewHolder)holder).feeling.setImageResource(reactions[messageModel.getFeeling()]);
                ((SenderViewHolder)holder).feeling.setVisibility(View.VISIBLE);
            }
            else {
                ((SenderViewHolder)holder).feeling.setVisibility(View.INVISIBLE);
            }
            if(messageModel.getLanguage().equals("english")) {
                ((SenderViewHolder) holder).play.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(View v) {
                        ((SenderViewHolder) holder).play.setVisibility(View.GONE);
                        ((SenderViewHolder) holder).pause.setVisibility(View.VISIBLE);
                        String s = ((SenderViewHolder) holder).senderMsg.getText().toString();
                        class Waiter extends AsyncTask<Void,Void,Void> {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                while (textToSpeech.isSpeaking()){
                                    try{Thread.sleep(1000);}catch (Exception e){}
                                }
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                //TTS has finished speaking. WRITE YOUR CODE HERE
                                ((SenderViewHolder) holder).pause.setVisibility(View.GONE);
                                ((SenderViewHolder) holder).play.setVisibility(View.VISIBLE);
                            }
                        }
                        int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                        new Waiter().execute();

                    }
                });
                ((SenderViewHolder) holder).pause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((SenderViewHolder) holder).pause.setVisibility(View.GONE);
                        ((SenderViewHolder) holder).play.setVisibility(View.VISIBLE);
                        int speech = textToSpeech.stop();
                    }
                });
            }
            else{
                ((SenderViewHolder) holder).pause.setVisibility(View.GONE);
                ((SenderViewHolder) holder).play.setVisibility(View.GONE);
            }
            ((SenderViewHolder)holder).sendersmessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Alert !");
                    alertDialog.setMessage("Do you want delete this message?");
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).child("messages").child(messageModel.getMessageId()).removeValue();
                        }
                    });
                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                    return false;
                }
            });
            ((SenderViewHolder)holder).image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    k=25;
                    Log.d("photourl", "onClick: ");
                    String[] urls = new String[100];
                    List<String> url = new ArrayList<>();
                    final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).child("messages");
                    String encrypt = encryption.encryptOrNull("photo");
                    Query query = mUserDB.orderByChild("message").equalTo(encrypt);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                    i=0;
                                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                        MessageModel messageModel1 = childSnapshot.getValue(MessageModel.class);
                                        urls[i] = messageModel1.getImageUrl();
                                        Log.d("photourl", urls[i]);
                                        url.add(messageModel1.getImageUrl());
                                        i++;
                                    }
                                    int posi = -1;
                                    Log.d("photourl", "length is " + (i - 1) + "");
                                    for (int j = 0; j < i; j++) {
                                        if (messageModel.getImageUrl().equals(urls[j])) {
                                            posi = j;
                                        }
                                    }
                                    Log.d("photourl", posi + "");
                                        new ImageViewer.Builder(context, url).setStartPosition(posi).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            });
            }
        else {
            ((ReceiverViewHolder)holder).receiverMsg.setText(decrypted);
            ((ReceiverViewHolder)holder).receiversmessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Alert !");
                    alertDialog.setMessage("Do you want delete this message?");
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).child("messages").child(messageModel.getMessageId()).removeValue();
                        }
                    });
                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                    return false;
                }
            });
            if(decrypted.equals("currentlocation")&&(messageModel.getLatitute()!=0)&&(messageModel.getLongitude()!=0))
            {
                Log.d("checker", decrypted);
                ((ReceiverViewHolder)holder).currentlocation.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder)holder).receivermessage.setVisibility(View.GONE);
                Glide.with(context).load(R.drawable.googlemaps).placeholder(R.drawable.googlemaps).into(((ReceiverViewHolder)holder).currentlocation);
                ((ReceiverViewHolder)holder).currentlocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=loc:%f,%f", messageModel.getLatitute(),messageModel.getLongitude());
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        context.startActivity(intent);
                    }
                });
            }
            else if(decrypted.equals("photo")&&(messageModel.getImageUrl()!=""))
            {
                Log.d("checker1", decrypted);
                ((ReceiverViewHolder)holder).image.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder)holder).receivermessage.setVisibility(View.GONE);
                Glide.with(context).load(messageModel.getImageUrl()).placeholder(R.drawable.imageph).into(((ReceiverViewHolder)holder).image);
                ((ReceiverViewHolder)holder).image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("photourl", "onClick: ");
                        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        String[] urls = new String[100];
                        List<String> url = new ArrayList<>();
                        final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).child("messages");
                        String encrypt = encryption.encryptOrNull("photo");
                        Query query = mUserDB.orderByChild("message").equalTo(encrypt);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    i=0;
                                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                                        MessageModel messageModel1 = childSnapshot.getValue(MessageModel.class);
                                        urls[i]=messageModel1.getImageUrl();
                                        Log.d("photourl",urls[i]);
                                        url.add(messageModel1.getImageUrl());
                                        i++;
                                    }
                                    int posi =-1;
                                    Log.d("photourl","length is "+(i-1)+"");
                                    for (int j =0;j<=i;j++){
                                        if(messageModel.getImageUrl().equals(urls[j])){
                                            posi = j;
                                        }
                                    }
                                    Log.d("photourl",posi+"");
                                    new ImageViewer.Builder(context,url).setStartPosition(posi).show();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
            else
            {
                ((ReceiverViewHolder)holder).image.setVisibility(View.GONE);
                ((ReceiverViewHolder)holder).currentlocation.setVisibility(View.GONE);
                ((ReceiverViewHolder)holder).receivermessage.setVisibility(View.VISIBLE);
            }

            long time = messageModel.getTimestamp();
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
            ((ReceiverViewHolder) holder).receiverTime.setText(dateFormat.format(new Date(time)));
            if(messageModel.getFeeling()>=0){
                ((ReceiverViewHolder)holder).feeling.setImageResource(reactions[messageModel.getFeeling()]);
                ((ReceiverViewHolder)holder).feeling.setVisibility(View.VISIBLE);
            }
            else {
                ((ReceiverViewHolder)holder).feeling.setVisibility(View.INVISIBLE);
            }
            ((ReceiverViewHolder)holder).receiverMsg.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
            if(messageModel.getLanguage().equals("english")){
                ((ReceiverViewHolder) holder).play.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ReceiverViewHolder) holder).play.setVisibility(View.GONE);
                        ((ReceiverViewHolder) holder).pause.setVisibility(View.VISIBLE);
                        String s = ((ReceiverViewHolder) holder).receiverMsg.getText().toString();
                        class Waiter1 extends AsyncTask<Void,Void,Void> {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                while (textToSpeech.isSpeaking()){
                                    try{Thread.sleep(1000);}catch (Exception e){}
                                }
                                return null;
                            }
                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                //TTS has finished speaking. WRITE YOUR CODE HERE
                                ((ReceiverViewHolder) holder).pause.setVisibility(View.GONE);
                                ((ReceiverViewHolder) holder).play.setVisibility(View.VISIBLE);
                            }
                        }
                        int speech = textToSpeech.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                        new Waiter1().execute();
                    }
                });
                ((ReceiverViewHolder) holder).pause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ReceiverViewHolder) holder).pause.setVisibility(View.GONE);
                        ((ReceiverViewHolder) holder).play.setVisibility(View.VISIBLE);
                        String s = ((ReceiverViewHolder) holder).receiverMsg.getText().toString();
                        int speech = textToSpeech.stop();
                    }
                });
            }
            else{
                ((ReceiverViewHolder) holder).pause.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).play.setVisibility(View.GONE);
            }
            }
        }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiverMsg,receiverTime;
        ImageView play,pause,feeling,image,currentlocation;
        ConstraintLayout receivermessage;
        ConstraintLayout receiversmessage;

        public ReceiverViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            receiverMsg=itemView.findViewById(R.id.receiverText);
            receiverTime=itemView.findViewById(R.id.receiverTime);
            play=itemView.findViewById(R.id.play);
            pause=itemView.findViewById(R.id.pause);
            feeling=itemView.findViewById(R.id.feeling2);
            image=itemView.findViewById(R.id.image2);
            currentlocation=itemView.findViewById(R.id.currentlocation2);
            receivermessage = itemView.findViewById(R.id.receivermessage);
            receiversmessage=itemView.findViewById(R.id.receiversmessage);

        }
    }
    public class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMsg,senderTime;
        ImageView play,pause,feeling,image,currentlocation;
        ConstraintLayout sendermessage123;
        ConstraintLayout sendersmessage;

        public SenderViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            senderMsg=itemView.findViewById(R.id.senderText);
            senderTime=itemView.findViewById(R.id.senderTime);
            play=itemView.findViewById(R.id.play);
            pause=itemView.findViewById(R.id.pause);
            feeling=itemView.findViewById(R.id.feeling);
            sendersmessage=itemView.findViewById(R.id.sendersmessage);
            image=itemView.findViewById(R.id.image);
            currentlocation=itemView.findViewById(R.id.currentlocation);
            sendermessage123 = itemView.findViewById(R.id.sendermessage123);
        }
    }
}
