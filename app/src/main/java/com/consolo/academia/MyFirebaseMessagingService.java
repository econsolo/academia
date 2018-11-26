package com.consolo.academia;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationCompat.Builder builder;
    private NotificationManager mNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        criarNotificacao(Objects.requireNonNull(remoteMessage.getNotification()).getBody());
    }

    private void criarNotificacao(String msg) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Academia");
        builder.setContentText(msg);
        builder.setSmallIcon(R.drawable.ic_stat_app_icon);
        mNotificationManager.notify(1, builder.build());
    }
}
