package com.amazonaws.kinesisvideo.demoapp.firebasecloudmessaging;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.amazonaws.kinesisvideo.demoapp.constant.Constants.ACTION.NOTIFICATION_MSG_RECEIVED_ACTION;

public class MessageValueReading extends FirebaseMessagingService {
    private static final String TAG = MessageValueReading.class.getSimpleName();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            sendMessage(remoteMessage.getData().toString());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendMessage(remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    // Supposing that your value is an integer declared somewhere as: int myInteger;
    private void sendMessage(final String message) {
        // The string "my-integer" will be used to filer the intent
        Intent intent = new Intent(NOTIFICATION_MSG_RECEIVED_ACTION);
        // Adding some data
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}