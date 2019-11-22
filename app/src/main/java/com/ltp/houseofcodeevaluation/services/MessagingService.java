package com.ltp.houseofcodeevaluation.services;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ltp.houseofcodeevaluation.ChatRoomsActivity;

/**
 * Not finished...
 */
public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title;
        String body;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("Debug", "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d("Debug", title);
            Log.d("Debug", body);
            Log.d("Debug", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }
}
