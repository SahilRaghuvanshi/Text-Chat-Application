package com.compgrp4.textchat;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDexApplication;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.squareup.picasso.Picasso;

public class App extends MultiDexApplication {
    private Activity mCurrentActivity = null;
    @Override
    public void onCreate() {
        super.onCreate();
        Picasso.setSingletonInstance(new Picasso.Builder(this).build());
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);
    }
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }
}