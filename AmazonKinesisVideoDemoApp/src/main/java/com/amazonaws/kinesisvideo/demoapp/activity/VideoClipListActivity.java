package com.amazonaws.kinesisvideo.demoapp.activity;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.amazonaws.kinesisvideo.demoapp.R;

public class VideoClipListActivity extends Activity {
    // Array of strings...
    String[] mobileArray = {"Clip1","Clip2","Clip3","Clip4"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_clip_list_main);

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_video_clip_list_listview, mobileArray);

        ListView listView = (ListView) findViewById(R.id.clip_list);
        listView.setAdapter(adapter);
    }
}