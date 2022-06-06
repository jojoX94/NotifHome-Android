package com.example.imageproject.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationApp extends Application {
    public static final String CHANNEl_ID = "upload";

    @Override
    public void onCreate() {
        super.onCreate();
        this.createNotificationChannels();
    }

    private void createNotificationChannels()  {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEl_ID,
                    "Channel upload",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is channel for uploading file test");


            NotificationManager manager = this.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }


}
