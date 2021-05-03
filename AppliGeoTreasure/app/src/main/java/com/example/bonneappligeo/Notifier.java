package com.example.bonneappligeo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class Notifier {
    private Context context;
    private static final String CHANNEL_ID = "42";
    private static final CharSequence CHANNEL_NAME = "Notification";
    private static final String CHANNEL_DESCRIPTION = "NotificationDescription";
    static int notificationId = 2;
    private final static int FOREGROUND_NOTIFICATION_ID = 1;

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
        Intent notifyIntent = new Intent(context, TreasureMapsActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(context, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(text);
        notificationBuilder.setSmallIcon(icon);
        notificationBuilder.setColor(ContextCompat.getColor(context, R.color.yellow));
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationBuilder.setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(notificationId, notificationBuilder.build());
        notificationId++;
    }

    public void createForegroundService(Service service, Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(context, "42")
                    .setContentTitle("GeoTreasure")
                    .setContentText("Vous avez commencé à jouer")
                    .setSmallIcon(R.drawable.chest)
                    .build();
            service.startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        }
    }
}
