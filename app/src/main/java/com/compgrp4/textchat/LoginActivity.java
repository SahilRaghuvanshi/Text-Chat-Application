package com.compgrp4.textchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.compgrp4.textchat.Models.Users;
import com.compgrp4.textchat.databinding.ActivityLoginBinding;
import com.compgrp4.textchat.databinding.ActivitySignInBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    private EditText userName, phoneNumber, OTP, mail, password;
    TextView already;
    FirebaseDatabase database;
    private Button signUp;
    public static final String fstring = "com.example.mohaiminur.com.textchat.UserFragment";
    public static final String fcode = "codee";
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    int emailandpassword = 0;
    int usernameandphone = 0;
    String mVerificationId;
    ProgressDialog progressDialog;
    private FirebaseAuth auth;
    String id, phoneno;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);
        database = FirebaseDatabase.getInstance();
        FirebaseApp.initializeApp(this);
        getCountryISO();
        String ISOPrefix = getCountryISO();
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Creating Account");
        progressDialog.setMessage("We are creating your account");
        String Ccode = ISOPrefix;
        SharedPreferences sp = getSharedPreferences(fstring, Context.MODE_PRIVATE);
        sp.edit().putString(fcode, Ccode).commit();
        auth = FirebaseAuth.getInstance();
        //auth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        userName = findViewById(R.id.etUsername);
        phoneNumber = findViewById(R.id.phoneNumber);
        phoneNumber.setText(Ccode);
        OTP = findViewById(R.id.OTP);
        mail = findViewById(R.id.email);
        password = findViewById(R.id.password);
        already = findViewById(R.id.tvalreadyAccount);
        signUp = findViewById(R.id.btn_SignUp);
        already.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVerificationId != null)
                    verifyPhoneNumberWithCode();
                else
                    try {
                        sendOtp(phoneNumber.getText().toString());
                    }
                    catch (NumberParseException e){
                        Toast.makeText(LoginActivity.this, "Invalid Number", Toast.LENGTH_SHORT).show();
                    }
                Log.d("swati", phoneNumber.getText().toString());
            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.d("swati", e + "");
            }


            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);

                mVerificationId = verificationId;
                phoneNumber.setVisibility(View.VISIBLE);
                userName.setVisibility(View.VISIBLE);
                OTP.setVisibility(View.VISIBLE);
                signUp.setText("Sign Up");
            }
        };

    }

    private String getCountryISO() {

        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null)
            if (!telephonyManager.getNetworkCountryIso().toString().equals(""))
                iso = telephonyManager.getNetworkCountryIso().toString();

        return CountryToPhonePrefix.getPhone(iso);
    }

    private void verifyPhoneNumberWithCode() {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, OTP.getText().toString());
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        auth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                String userid = task.getResult().getUser().getUid();
                Log.d("sahil", "userid" + userid);
                if (task.isSuccessful()) {
                    if (userid != null) {
                        final DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
                        mUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    phoneno = task.getResult().getUser().getPhoneNumber().toString();
                                    auth.createUserWithEmailAndPassword(mail.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@androidx.annotation.NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                String user2 = task.getResult().getUser().getUid();
                                                emailandpassword = 1;
                                                usernameandphone = 0;
                                                progressDialog.dismiss();
                                                Users user = new Users(user2, userName.getText().toString(), mail.getText().toString(), password.getText().toString(), phoneno, "offline");
                                                Toast.makeText(LoginActivity.this, "Sign Up Completed Successfully", Toast.LENGTH_SHORT).show();
                                                database.getReference().child("Users").child(user2).setValue(user);
                                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                                } else {
                                    Toast.makeText(LoginActivity.this, "Phone Number already registered...", Toast.LENGTH_SHORT).show();
                                    mVerificationId = null;
                                    signUp.setText("Submit");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    private void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            return;
        }
    }

    private void sendOtp(String phonenumber) throws NumberParseException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        PhoneNumber numberProto = phoneUtil.parse(phonenumber, "US");
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164))       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}
