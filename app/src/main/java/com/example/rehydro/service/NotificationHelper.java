package com.example.rehydro.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.rehydro.R;
import com.example.rehydro.ui.MainActivity;

public class NotificationHelper {

    private static final String WATER_CHANNEL_ID      = "water_reminder_channel";
    private static final int    WATER_NOTIFICATION_ID  = 2001;
    private static final String LOCATION_CHANNEL_ID   = "location_alert_channel";
    private static final int    LOCATION_NOTIFICATION_ID = 2002;

    public static void createNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                WATER_CHANNEL_ID,
                context.getString(R.string.notif_water_title),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(context.getString(R.string.notif_water_text));
        NotificationManager manager =
                context.getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }

    public static void createLocationNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                LOCATION_CHANNEL_ID,
                context.getString(R.string.notif_location_title),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(context.getString(R.string.notif_location_text));
        NotificationManager manager =
                context.getSystemService(NotificationManager.class);
        if (manager != null) manager.createNotificationChannel(channel);
    }

    public static void sendWaterReminder(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, WATER_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_arrow_back)
                        .setContentTitle(context.getString(R.string.notif_water_title))
                        .setContentText(context.getString(R.string.notif_water_text))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context)
                    .notify(WATER_NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void cancelWaterReminder(Context context) {
        NotificationManagerCompat.from(context).cancel(WATER_NOTIFICATION_ID);
    }

    public static void sendLocationAlert(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, LOCATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_arrow_back)
                        .setContentTitle(context.getString(R.string.notif_location_title))
                        .setContentText(context.getString(R.string.notif_location_text))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context)
                    .notify(LOCATION_NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}