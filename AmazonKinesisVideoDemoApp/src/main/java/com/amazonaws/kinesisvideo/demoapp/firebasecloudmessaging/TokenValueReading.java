package com.amazonaws.kinesisvideo.demoapp.firebasecloudmessaging;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.amazonaws.kinesisvideo.demoapp.constant.Constants.ACTION.NOTIFICATION_TOKEN_RECEIVED_ACTION;

public class TokenValueReading extends FirebaseInstanceIdService {
    private static final String TAG = TokenValueReading.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendMessage(refreshedToken);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    // Supposing that your value is an integer declared somewhere as: int myInteger;
    private void sendMessage(final String message) {
        // The string "my-integer" will be used to filer the intent
        Intent intent = new Intent(NOTIFICATION_TOKEN_RECEIVED_ACTION);
        // Adding some data
        intent.putExtra("token", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendRegistrationToServer(final String token) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("server/saving-data/IDs");
        // then store your token ID
        ref.push().setValue(token);
    }
}
