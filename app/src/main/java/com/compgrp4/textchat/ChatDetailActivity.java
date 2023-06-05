package com.compgrp4.textchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.compgrp4.textchat.Adapters.ChatAdapter;
import com.compgrp4.textchat.Models.MessageModel;
import com.compgrp4.textchat.Models.Users;
import com.compgrp4.textchat.databinding.ActivityChatDetailBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
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
import com.google.mlkit.common.MlKit;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Callback;
import retrofit2.Response;
import se.simbio.encryption.Encryption;

public class ChatDetailActivity extends BaseActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    ActivityChatDetailBinding binding;
    public static final int CAMERA_REQUEST_CODE = 200;
    public static final int STORAGE_REQUEST_CODE = 400;
    public static final int IMAGE_PICK_GALLERY_CODE = 1000;
    public static final int IMAGE_PICK_CAMERA_CODE = 1001;
    String camerapermission[];
    String storagepermission[];
    Uri image_uri;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String receiveId;
    String senderId;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    String currentPhotoPath;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    String senderName;
    SinchClient sinchClient;
    Call call;
    Encryption encryption;
    boolean value1 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        senderName = firebaseUser.getDisplayName();
        Log.d("sushant", senderName + "");
        storage = FirebaseStorage.getInstance();
        Date date = new Date();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Image Uploading...");
        dialog.setCancelable(false);
        senderId = firebaseUser.getUid();
        receiveId = getIntent().getStringExtra("userid");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");
        binding.userName.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);
        camerapermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        final ArrayList<MessageModel> messageModels = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatDetailActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        binding.ChatRecyclerView.setLayoutManager(linearLayoutManager);
        sinchClient = Sinch.getSinchClientBuilder().context(getApplicationContext()).applicationKey("2ba34112-d686-48f8-a95b-42c5dd045bad").applicationSecret("QUlWUD/eBkyaGbSipT6+WQ==").environmentHost("clientapi.sinch.com").userId(senderId).build();
        sinchClient.setSupportCalling(true);
        sinchClient.start();
        sinchClient.startListeningOnActiveConnection();
        sinchClient.getCallClient().addCallClientListener(new sinchCallClientListener());
        String key = "Sahil";
        String salt = "Sushant";
        byte[] iv = new byte[16];
        encryption = Encryption.getDefault(key, salt, iv);
        database.getReference().child("chats").child(senderId + receiveId).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                messageModels.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    MessageModel model = snapshot1.getValue(MessageModel.class);
                    model.setMessageId(snapshot1.getKey());
                    messageModels.add(model);
                }
                ChatAdapter chatAdapter = new ChatAdapter(messageModels, ChatDetailActivity.this, senderId + receiveId, receiveId + senderId, encryption);
                binding.ChatRecyclerView.setAdapter(chatAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });
        binding.phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (call == null) {
                    Log.d("mitsu", receiveId);
                    call = sinchClient.getCallClient().callUser(receiveId);
                    LayoutInflater inflater = getLayoutInflater();
                    View alertLayout = inflater.inflate(R.layout.activity_calling_someone, null);
                    final ImageView reject = alertLayout.findViewById(R.id.reject);
                    final TextView callname = alertLayout.findViewById(R.id.callname);
                    final TextView callstatus = alertLayout.findViewById(R.id.callstatus);
                    final ImageView callimage = alertLayout.findViewById(R.id.callimage);
                    AlertDialog.Builder alert1 = new AlertDialog.Builder(ChatDetailActivity.this);
                    Dialog d = alert1.setView(alertLayout).create();
                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(dm);
                    int width = dm.widthPixels;
                    int height = dm.heightPixels;
                    Log.d("sap", width + " " + height);
                    // (That new View is just there to have something inside the dialog that can grow big enough to cover the whole screen.)
                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(d.getWindow().getAttributes());
                    lp.width = width;
                    lp.height = height;
                    d.getWindow().setAttributes(lp);
                    callname.setText(userName);
                    Picasso.get().load(profilePic).placeholder(R.drawable.someone).into(callimage);
                    call.addCallListener(new CallListener() {
                        @Override
                        public void onCallProgressing(Call call) {
                            Toast.makeText(getApplicationContext(), "Call is Ringing", Toast.LENGTH_SHORT).show();
                            callstatus.setText("Ringing...");
                        }

                        @Override
                        public void onCallEstablished(Call call) {
                            Toast.makeText(getApplicationContext(), "Call is Established", Toast.LENGTH_SHORT).show();
                            callstatus.setText("Call Established");
                        }

                        @Override
                        public void onCallEnded(Call endedcall) {
                            call = null;
                            Toast.makeText(getApplicationContext(), "Call is Ended", Toast.LENGTH_SHORT).show();
                            d.dismiss();
                            endedcall.hangup();
                            callstatus.setText("Call Ended");
                        }

                        @Override
                        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

                        }
                    });
                    reject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            d.dismiss();
                            call.hangup();
                            call = null;
                        }
                    });
                    d.show();
                }
            }
        });
        binding.backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    finish();
            }
        });
        //
        binding.etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.etMessage.getText().toString().equals("")) {
                    Log.d("dar", "if executed");
                    binding.send.setVisibility(View.GONE);
                    binding.audio.setVisibility(View.VISIBLE);
                } else {
                    binding.send.setVisibility(View.VISIBLE);
                    binding.audio.setVisibility(View.GONE);
                }
            }
        });
        binding.video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getSinchServiceInterface().isStarted()) {
                    getSinchServiceInterface().startClient(senderId+"video");
                } else {
                    Call videocall = getSinchServiceInterface().callUserVideo(receiveId+"video");
                    String callId = videocall.getCallId();
                    Intent callScreen = new Intent(ChatDetailActivity.this, CallScreenActivity.class);
                    callScreen.putExtra(SinchService.CALL_ID, callId);
                    callScreen.putExtra("name", userName);
                    callScreen.putExtra("profilePic", profilePic);
                    startActivity(callScreen);
                }
            }
        });
        //
        binding.audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 10);
                } else {
                    Toast.makeText(ChatDetailActivity.this, "Settings Clicked", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.paperclip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatDetailActivity.this, Popup.class);
                intent.putExtra("toggle", value1);
                startActivityForResult(intent, 50);
            }
        });
        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (value1 == false) {
                    String message = binding.etMessage.getText().toString();
                    String encrypted = encryption.encryptOrNull(message);
                    final MessageModel model = new MessageModel(senderId, encrypted);

                    model.setImageUrl("");
                    model.setLongitude(0);
                    model.setLatitute(0);
                    model.setLanguage("english");
                    model.setTimestamp(new Date().getTime());
                    model.setuId(senderId);
                    binding.etMessage.setText("");

                    String randomkey = database.getReference().push().getKey();

                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                    lastMsgObj.put("lastMsg", model.getMessage());
                    lastMsgObj.put("lastMsgTime", date.getTime());

                    database.getReference().child("chats").child(senderId + receiveId).updateChildren(lastMsgObj);
                    database.getReference().child("chats").child(receiveId + senderId).updateChildren(lastMsgObj);
                    database.getReference().child("chats").child(senderId + receiveId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            database.getReference().child("chats").child(receiveId + senderId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
                                            String currentDateandTime = sdf.format(new Date());
                                            Log.d("mehal", currentDateandTime);
                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("datetime").setValue(currentDateandTime);
                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("id").setValue(receiveId);
                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("datetime").setValue(currentDateandTime);
                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("id").setValue(senderId);
                                        }
                                    }
                            );
                        }
                    });
                } else {
                    MlKit.initialize(getApplicationContext());
                    TranslatorOptions options =
                            new TranslatorOptions.Builder()
                                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                                    .setTargetLanguage(TranslateLanguage.HINDI)
                                    .build();
                    final Translator englishhindiTranslator =
                            Translation.getClient(options);
                    getLifecycle().addObserver(englishhindiTranslator);
                    DownloadConditions conditions = new DownloadConditions.Builder()
                            .build();
                    englishhindiTranslator.downloadModelIfNeeded(conditions)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("christ", "onSuccess: ");
                                    englishhindiTranslator.translate(binding.etMessage.getText().toString()).addOnSuccessListener(new OnSuccessListener<String>() {
                                        @Override
                                        public void onSuccess(String messageTranslated) {
                                            Log.d("christ", "Inside second on success");
                                            String encrypted = encryption.encryptOrNull(messageTranslated);
                                            final MessageModel model = new MessageModel(senderId, encrypted);
                                            model.setLanguage("hindi");
                                            model.setImageUrl("");
                                            model.setLongitude(0);
                                            model.setLatitute(0);
                                            model.setTimestamp(new Date().getTime());
                                            model.setuId(senderId);
                                            binding.etMessage.setText("");

                                            String randomkey = database.getReference().push().getKey();
                                            Date date = new Date();
                                            HashMap<String, Object> lastMsgObj = new HashMap<>();
                                            lastMsgObj.put("lastMsg", model.getMessage());
                                            lastMsgObj.put("lastMsgTime", date.getTime());

                                            database.getReference().child("chats").child(senderId + receiveId).updateChildren(lastMsgObj);
                                            database.getReference().child("chats").child(receiveId + senderId).updateChildren(lastMsgObj);
                                            database.getReference().child("chats").child(senderId + receiveId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    database.getReference().child("chats").child(receiveId + senderId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(
                                                            new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
                                                                    String currentDateandTime = sdf.format(new Date());
                                                                    Log.d("mehal", currentDateandTime);
                                                                    FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("datetime").setValue(currentDateandTime);
                                                                    FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("id").setValue(receiveId);
                                                                    FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("datetime").setValue(currentDateandTime);
                                                                    FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("id").setValue(senderId);
                                                                }
                                                            }
                                                    );
                                                }
                                            });

                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("christ", e + " ");
                                            // Model couldn’t be downloaded or other internal error.
                                            // ...
                                        }
                                    });
                }
            }
        });
    }


    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image to Text");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(ChatDetailActivity.this, storagepermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(ChatDetailActivity.this, camerapermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(ChatDetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(ChatDetailActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        dispatchTakePictureIntent();
                    } else {
                        Toast.makeText(ChatDetailActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }

                break;
            case 50:
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(ChatDetailActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 225:
                call.hangup();
                break;
            case IMAGE_PICK_GALLERY_CODE:
                if (resultCode == RESULT_OK) {
                    CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(ChatDetailActivity.this);
                }
                break;
            case 50:
                if (resultCode == 85) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, 25);
                }
                if (resultCode == 345) {
                    boolean value = false;
                    value1 = data.getBooleanExtra("togglestate", value);
                    Log.d("christ", value1 + "");
                }
                if (resultCode == 346) {
                    boolean value = false;
                    value1 = data.getBooleanExtra("togglestate", value);
                    Log.d("christ", value1 + "");
                }
                if (resultCode == 347) {
                    String message = data.getStringExtra("userName") + " " + data.getStringExtra("phoneNumber");
                    String encrypted = encryption.encryptOrNull(message);
                    final MessageModel model = new MessageModel(senderId, encrypted);
                    model.setImageUrl("");
                    model.setLongitude(0);
                    model.setLatitute(0);
                    model.setLanguage("english");
                    model.setTimestamp(new Date().getTime());
                    model.setuId(senderId);
                    binding.etMessage.setText("");

                    String randomkey = database.getReference().push().getKey();

                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                    lastMsgObj.put("lastMsg", model.getMessage());
                    lastMsgObj.put("lastMsgTime", new Date().getTime());

                    database.getReference().child("chats").child(senderId + receiveId).updateChildren(lastMsgObj);
                    database.getReference().child("chats").child(receiveId + senderId).updateChildren(lastMsgObj);
                    database.getReference().child("chats").child(senderId + receiveId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            database.getReference().child("chats").child(receiveId + senderId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
                                            String currentDateandTime = sdf.format(new Date());
                                            Log.d("mehal", currentDateandTime);
                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("datetime").setValue(currentDateandTime);
                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("id").setValue(receiveId);
                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("datetime").setValue(currentDateandTime);
                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("id").setValue(senderId);
                                        }
                                    }
                            );
                        }
                    });


                }
                if (resultCode == 86) {
                    dispatchTakePictureIntent();
                }

                if (resultCode == 87) {
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            // The next two lines tell the new client that “this” current class will handle connection stuff
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            //fourth line adds the LocationServices API endpoint from GooglePlayServices
                            .addApi(LocationServices.API)
                            .build();
                    // Create the LocationRequest object
                    mLocationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                            .setFastestInterval(1 * 1000);
                    mGoogleApiClient.connect();
                }
                if (resultCode == 90) {
                    String[] items1 = {"Camera", "Gallery"};
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatDetailActivity.this);
                    dialog.setTitle("Select Image");
                    dialog.setItems(items1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                if (!checkCameraPermission()) {
                                    requestCameraPermission();
                                } else {
                                    pickCamera();
                                }
                            }
                            if (which == 1) {
                                if (!checkStoragePermission()) {
                                    requestStoragePermission();
                                } else {
                                    pickGallery();
                                }
                            }
                        }
                    });
                    dialog.create().show();
                }
                break;
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    File f = new File(currentPhotoPath);
                    Log.d("came here", " data.getdata() is not equal to null");
                    Uri selectedimage = Uri.fromFile(f);
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(selectedimage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filepath = uri.toString();
                                        String encrypt = encryption.encryptOrNull("photo");
                                        final MessageModel model = new MessageModel(senderId, encrypt);
                                        model.setLanguage("english");
                                        model.setLatitute(0);
                                        model.setLongitude(0);
                                        model.setImageUrl(filepath);
                                        model.setTimestamp(new Date().getTime());
                                        model.setuId(senderId);

                                        String randomkey = database.getReference().push().getKey();
                                        Date date = new Date();
                                        HashMap<String, Object> lastMsgObj = new HashMap<>();
                                        lastMsgObj.put("lastMsg", model.getMessage());
                                        lastMsgObj.put("lastMsgTime", date.getTime());

                                        database.getReference().child("chats").child(senderId + receiveId).updateChildren(lastMsgObj);
                                        database.getReference().child("chats").child(receiveId + senderId).updateChildren(lastMsgObj);
                                        database.getReference().child("chats").child(senderId + receiveId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                database.getReference().child("chats").child(receiveId + senderId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(
                                                        new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
                                                                String currentDateandTime = sdf.format(new Date());
                                                                Log.d("mehal", currentDateandTime);
                                                                FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("datetime").setValue(currentDateandTime);
                                                                FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("id").setValue(receiveId);
                                                                FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("datetime").setValue(currentDateandTime);
                                                                FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("id").setValue(senderId);
                                                            }
                                                        }
                                                );
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
                break;
            case 25:
                if (data != null) {
                    Log.d("came here", " data is not equal to null");
                    if (data.getData() != null) {
                        Log.d("came here", " data.getdata() is not equal to null");
                        Uri selectedimage = data.getData();
                        Calendar calendar = Calendar.getInstance();
                        StorageReference reference = storage.getReference().child("chats").child(calendar.getTimeInMillis() + "");
                        dialog.show();
                        reference.putFile(selectedimage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                                dialog.dismiss();
                                if (task.isSuccessful()) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String filepath = uri.toString();
                                            String encrypt = encryption.encryptOrNull("photo");
                                            final MessageModel model = new MessageModel(senderId, encrypt);
                                            model.setImageUrl(filepath);
                                            model.setLanguage("english");
                                            model.setLatitute(0);
                                            model.setLongitude(0);
                                            model.setTimestamp(new Date().getTime());
                                            model.setuId(senderId);

                                            String randomkey = database.getReference().push().getKey();
                                            Date date = new Date();
                                            HashMap<String, Object> lastMsgObj = new HashMap<>();
                                            lastMsgObj.put("lastMsg", model.getMessage());
                                            lastMsgObj.put("lastMsgTime", date.getTime());

                                            database.getReference().child("chats").child(senderId + receiveId).updateChildren(lastMsgObj);
                                            database.getReference().child("chats").child(receiveId + senderId).updateChildren(lastMsgObj);
                                            database.getReference().child("chats").child(senderId + receiveId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    database.getReference().child("chats").child(receiveId + senderId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
                                                            String currentDateandTime = sdf.format(new Date());
                                                            Log.d("mehal", currentDateandTime);
                                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("datetime").setValue(currentDateandTime);
                                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("id").setValue(receiveId);
                                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("datetime").setValue(currentDateandTime);
                                                            FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("id").setValue(senderId);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
                break;
            case IMAGE_PICK_CAMERA_CODE:
                if (resultCode == RESULT_OK) {
                    CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(ChatDetailActivity.this);
                }
                break;
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    binding.etMessage.setText(result.get(0));
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    binding.ImageIv.setImageURI(resultUri);
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.ImageIv.getDrawable();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                    if (!textRecognizer.isOperational()) {
                        Toast.makeText(ChatDetailActivity.this, "Error", Toast.LENGTH_SHORT);
                    } else {
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<TextBlock> items = textRecognizer.detect(frame);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < items.size(); i++) {
                            TextBlock myitem = items.valueAt(i);
                            sb.append(myitem.getValue());
                        }
                        binding.etMessage.setText(sb.toString());
                    }

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Toast.makeText(ChatDetailActivity.this, "" + error, Toast.LENGTH_SHORT);
                }
                break;
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void status(String status) {

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        Log.d("tata", "child updated");
        Log.d("tata", status);
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onServiceConnected() {

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");
    }

    @Override
    protected void onResume() {
        Log.d("tata", "onResume");
        super.onResume();
        status("online");
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

//        Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onConnected(@Nullable @org.jetbrains.annotations.Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            Log.d("yadnyesh", "onConnected: ");
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            Log.d("djam", currentLatitude + " WORKS " + currentLongitude + "");
            String encrypt = encryption.encryptOrNull("currentlocation");
            final MessageModel model = new MessageModel(senderId, encrypt);
            model.setTimestamp(new Date().getTime());            model.setuId(senderId);
            model.setLanguage("english");
            model.setLatitute(currentLatitude);
            model.setLongitude(currentLongitude);
            String randomkey = database.getReference().push().getKey();
            Date date = new Date();
            HashMap<String, Object> lastMsgObj = new HashMap<>();
            lastMsgObj.put("lastMsg", model.getMessage());
            lastMsgObj.put("lastMsgTime", date.getTime());
            database.getReference().child("chats").child(senderId + receiveId).updateChildren(lastMsgObj);
            database.getReference().child("chats").child(receiveId + senderId).updateChildren(lastMsgObj);
            database.getReference().child("chats").child(senderId + receiveId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                    database.getReference().child("chats").child(receiveId + senderId).child("messages").child(randomkey).setValue(model).addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
                                    String currentDateandTime = sdf.format(new Date());
                                    Log.d("mehal", currentDateandTime);
                                    FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("datetime").setValue(currentDateandTime);
                                    FirebaseDatabase.getInstance().getReference().child("Chatlist").child(senderId).child(receiveId).child("id").setValue(receiveId);
                                    FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("datetime").setValue(currentDateandTime);
                                    FirebaseDatabase.getInstance().getReference().child("Chatlist").child(receiveId).child(senderId).child("id").setValue(senderId);
                                }
                            }
                    );
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull @NotNull ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
                Log.d("yadnyesh", "onConnectionFailed: ");
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    public class sinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, final com.sinch.android.rtc.calling.Call incomingcall) {
                LayoutInflater inflater = getLayoutInflater();
                View alertLayout = inflater.inflate(R.layout.activity_someone_called, null);
                final ImageView reject = alertLayout.findViewById(R.id.reject2);
                final ImageView reject2 = alertLayout.findViewById(R.id.reject3);
                final ImageView pick = alertLayout.findViewById(R.id.pick2);
                final TextView callname = alertLayout.findViewById(R.id.callname2);
                final TextView callstatus = alertLayout.findViewById(R.id.callstatus2);
                final ImageView callimage2 = alertLayout.findViewById(R.id.callimage2);
                Context mContext = getApplicationContext();
                Activity activity = getActivity();
                AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                Dialog d = alert.setView(alertLayout).create();
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                int width = dm.widthPixels;
                int height = dm.heightPixels;
                Log.d("sap", width + " " + height);
                String callerId = incomingcall.getRemoteUserId();
                Call incomecall=incomingcall;


            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
            Query query = mUserDB.orderByChild("userId").equalTo(callerId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                int i=1;
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("susha", "on data change method started");
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                            Users user = childSnapshot.getValue(Users.class);
                            if (!(user.getMail().equals(firebaseUser.getEmail()))) {
                                if(i%2==1) {
                                    callname.setText(user.getUserName());
                                    Picasso.get().load(user.getProfilepic()).placeholder(R.drawable.someone).into(callimage2);
                                }
                                i++;
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
                // (That new View is just there to have something inside the dialog that can grow big enough to cover the whole screen.)
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.width = width;
                lp.height = height;
                d.getWindow().setAttributes(lp);
                callname.setText(senderName);
                if(incomingcall==null){
                    d.dismiss();
                    incomingcall.hangup();
                }
                reject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.dismiss();
                        incomingcall.hangup();
                    }
                });
            incomingcall.addCallListener(new CallListener() {
                @Override
                public void onCallProgressing(Call call) {
                    Toast.makeText(getApplicationContext(), "Call is Ringing", Toast.LENGTH_SHORT).show();
                    callstatus.setText("Ringing");
                }
                @Override
                public void onCallEstablished(Call call) {
                    Toast.makeText(getApplicationContext(), "Call is Established", Toast.LENGTH_SHORT).show();
                    callstatus.setText("Call Established");
                }
                @Override
                public void onCallEnded(Call endedcall) {
                    callstatus.setText("Call Ended");
                    call = null;
                    Toast.makeText(getApplicationContext(), "Call is Ended", Toast.LENGTH_SHORT).show();
                    d.dismiss();
                    endedcall.hangup();
                }
                @Override
                public void onShouldSendPushNotification(Call call, List<PushPair> list) {

                }
            });
                pick.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reject.setVisibility(View.INVISIBLE);
                        pick.setVisibility(View.INVISIBLE);
                        reject2.setVisibility(View.VISIBLE);
                        incomingcall.answer();
                        Toast.makeText(getApplicationContext(), "Call is started", Toast.LENGTH_SHORT).show();
                    }
                });
                reject2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        d.dismiss();
                        incomingcall.hangup();
                    }
                });
                d.show();
            }
            public Activity getActivity () {
                try {
                    Class activityThreadClass = Class.forName("android.app.ActivityThread");
                    Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
                    Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
                    activitiesField.setAccessible(true);
                    Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
                    if (activities == null)
                        return null;
                    for (Object activityRecord : activities.values()) {
                        Class activityRecordClass = activityRecord.getClass();
                        Field pausedField = activityRecordClass.getDeclaredField("paused");
                        pausedField.setAccessible(true);
                        if (!pausedField.getBoolean(activityRecord)) {
                            Field activityField = activityRecordClass.getDeclaredField("activity");
                            activityField.setAccessible(true);
                            return (Activity) activityField.get(activityRecord);
                        }
                    }
                    return null;
                } catch (Exception e) {
                    return null;
                }
            }
        }
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }
}
