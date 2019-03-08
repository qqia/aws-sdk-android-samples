package com.amazonaws.kinesisvideo.demoapp.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.kinesisvideo.demoapp.constant.Constants;
import com.amazonaws.kinesisvideo.demoapp.util.ActivityUtils;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class StartUpActivity extends AppCompatActivity {
    public static final String TAG = StartUpActivity.class.getSimpleName();
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AWSMobileClient auth = AWSMobileClient.getInstance();

        if (auth.isSignedIn()) {
            ActivityUtils.startActivity(this, SimpleNavActivity.class);
        } else {
            auth.showSignIn(this,
                    SignInUIOptions.builder()
                            .nextActivity(SimpleNavActivity.class)
                            .build(),
                    new Callback<UserStateDetails>() {
                        @Override
                        public void onResult(UserStateDetails result) {
                            Log.d(TAG, "onResult: User signed-in " + result.getUserState());
                        }

                        @Override
                        public void onError(final Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e(TAG, "onError: User sign-in error", e);
                                    Toast.makeText(StartUpActivity.this, "User sign-in error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        getPermission();
        LocalBroadcastManager.getInstance(this).registerReceiver((mTokenReceiver),
                new IntentFilter(Constants.ACTION.NOTIFICATION_TOKEN_RECEIVED_ACTION)
        );
    }

    private void getPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(StartUpActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        }
    }

    private BroadcastReceiver mTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String yourString = "Received: " + intent.getStringExtra("token");
            Log.d(TAG, yourString);
            Toast.makeText(context, yourString, Toast.LENGTH_LONG).show();
        }
    };

    private void saveFcmTokenToFile(String content) throws IOException {
        final File file = new File(Environment.getExternalStorageDirectory()+"/token.txt");
        Toast.makeText(StartUpActivity.this, file.getPath(), Toast.LENGTH_LONG).show();
        OutputStream output = null;
        try {
            output = openFileOutput(file.getName(), Context.MODE_PRIVATE);
            output.write(content.getBytes());
        } finally {
            if (null != output) {
                output.close();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(StartUpActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
