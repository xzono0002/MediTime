package com.mediteam.meditime.Helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.mediteam.meditime.R;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive (Context context, Intent intent) {
        showNotification(context, "Medication Reminder", "It's time to take your medication!");
    }

    private void showNotification(Context context, String title, String message){
        // Create and show the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.meditime_notif);
        Log.d("Notification", "soundURI: " + soundUri.toString());
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_notif);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "med_channel")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSmallIcon(R.drawable.logo_notif_small)
                .setContentTitle(title)
                .setContentText(message)
                .setLargeIcon(largeIcon)
                .setSound(soundUri)
                .setVibrate(new long[]{500, 500})
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Create a notification channel for Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Medication Channel";
            String description = "Medication Reminder Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            NotificationChannel channel = new NotificationChannel("med_channel", name, importance);
            channel.setDescription(description);
            channel.setSound(soundUri, attributes);

            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(1, builder.build());
    }
}
