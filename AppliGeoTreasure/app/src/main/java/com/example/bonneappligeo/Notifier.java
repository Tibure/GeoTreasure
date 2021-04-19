package com.example.bonneappligeo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notifier {
    private Context context;
    private static final String CHANNEL_ID = "42";
    private static final CharSequence CHANNEL_NAME = "Notification";
    private static final String CHANNEL_DESCRIPTION = "NotificationDescription";

    public Notifier(Context context) {
        this.context = context;
        createChannel();
    }

    public void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int channelImportance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, channelImportance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void notify(String title, String text, int icon) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(text);
        // verifier le format pour l'icon
        notificationBuilder.setSmallIcon(icon);
        // notificationBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, notificationBuilder.build());
    }
}