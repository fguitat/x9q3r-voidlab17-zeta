package com.example.tfg;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("respuesta");
        int notificationId = intent.getIntExtra("notification_id", -1);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
        Intent serviceIntent = new Intent(context, NotificationService.class);
        serviceIntent.setAction("RNOTIFICACION");
        serviceIntent.putExtra("respuesta", action);
        serviceIntent.putExtra("notification_id", notificationId);
        context.startService(serviceIntent);
    }
}
