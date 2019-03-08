package com.amazonaws.kinesisvideo.demoapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.amazonaws.kinesisvideo.demoapp.R;
import com.amazonaws.kinesisvideo.demoapp.fragment.StreamConfigurationFragment;
import com.amazonaws.kinesisvideo.demoapp.fragment.StreamingFragment;
import com.amazonaws.kinesisvideo.demoapp.constant.Constants;
import com.amazonaws.kinesisvideo.demoapp.motiondetection.MotionDetectionService;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;

import java.io.IOException;

public class SimpleNavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = SimpleNavActivity.class.getSimpleName();
    private StreamConfigurationFragment mConfigFragment = null;
    private StreamingFragment mStreamingFragment = null;
    private boolean mIfRemoteControlAllowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        this.startConfigFragment();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMotionDetectionReceiver),
                new IntentFilter(Constants.ACTION.MOTION_DETECTED_ACTION)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((mNotificationReceiver),
                new IntentFilter(Constants.ACTION.NOTIFICATION_MSG_RECEIVED_ACTION)
        );
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.simple_nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            try {
                startConfigFragment();
            } catch (Exception e) {
                Log.e("", "Failed to initialize streaming demo fragment.");
                e.printStackTrace();
            }
        } else if (id == R.id.nav_logout) {
            AWSMobileClient.getInstance().signOut();
            AWSMobileClient.getInstance().showSignIn(this, new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails result) {
                    Log.d(TAG, "onResult: User sign-in " + result.getUserState());
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "onError: User sign-in", e);
                }
            });
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_simple, fragment).commit();
    }

    public void startStreamingFragment(Bundle extras) {
        try {
            mStreamingFragment = StreamingFragment.newInstance(this);
            mStreamingFragment.setArguments(extras);
            this.startFragment(mStreamingFragment);
        } catch (Exception e) {
            Log.e("", "Failed to start streaming fragment.");
            e.printStackTrace();
        }
    }

    public void startConfigFragment() {
        try {
            mConfigFragment = StreamConfigurationFragment.newInstance(this);
            this.startFragment(mConfigFragment);
        } catch (Exception e) {
            Log.e("", "Failed to go back to configure stream.");
            e.printStackTrace();
        }
    }

    public void startBackgroundMotionDetectionService() {
        Intent startIntent = new Intent(SimpleNavActivity.this, MotionDetectionService.class);
        startIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(startIntent);
    }

    public void stopBackgroundMotionDetectionService() {
        Intent stopIntent = new Intent(SimpleNavActivity.this, MotionDetectionService.class);
        stopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        startService(stopIntent);
    }

    public void onMotionDetected() {
        Log.i(TAG, "Motion detected");
        stopBackgroundMotionDetectionService();
        if (mConfigFragment != null) {
            mConfigFragment.startStreamingActivity();
        }
    }

    public void setRemoteControlEnable(final boolean value) {
        mIfRemoteControlAllowed = value;
    }

    public void onNotificationReceived(final String msg) {
        Log.i(TAG, "onNotificationMessageReceived: " + msg);
        if (!mIfRemoteControlAllowed) {
            Log.i(TAG, "Remote control disabled");
            return;
        }
        if (Constants.NOTIFICATION_COMMAND.START_STREAMING.equalsIgnoreCase(msg)) {
            if (mConfigFragment != null) {
                mConfigFragment.startStreamingActivity();
            }
        } else if (Constants.NOTIFICATION_COMMAND.STOP_STREAMING.equalsIgnoreCase(msg)) {
            if (mStreamingFragment != null) {
                mStreamingFragment.pauseStreaming();
                startConfigFragment();
            }
        } else if (Constants.NOTIFICATION_COMMAND.START_ARMING.equalsIgnoreCase(msg)) {
            startBackgroundMotionDetectionService();
        }
    }

    private BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String yourString = "Received: " + intent.getStringExtra("message");
            Log.d(TAG, yourString);
            Toast.makeText(context, yourString, Toast.LENGTH_LONG).show();
            onNotificationReceived(intent.getStringExtra("message"));
        }
    };

    private BroadcastReceiver mMotionDetectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onMotionDetected();
        }
    };
}
