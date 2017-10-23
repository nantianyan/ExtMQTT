package com.example.democm;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.example.cloudmedia.CloudMedia;
import com.example.cloudmedia.RemoteMediaNode;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CloudMediaDemo";
    private final Context mContext = this;
    private CloudMedia mCloudMedia;
    private RemoteMediaNode mRemoteMediaNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button buttonConnect = (Button) findViewById(R.id.buttonConnect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCloudMedia = new CloudMedia(mContext);
                //mCloudMedia.connect("tcp://139.224.128.15:1883", "car123");
                mCloudMedia.connect("tcp://139.224.128.15:1883", "car123",
                        new CloudMedia.SimpleActionListener() {
                    @Override
                    public boolean onResult(String result) {
                        Log.i(TAG, "connect result is: " + result);
                        return true;
                    }
                });
                mRemoteMediaNode = mCloudMedia.declareRemoteMediaNode("controller");
            }
        });

        final Button buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mRemoteMediaNode.startPushMedia("rtmp://xxx.xxx.xxx.xxx/app/name");

                mRemoteMediaNode.startPushMedia("rtmp://xxx.xxx.xxx.xxx/app/name",
                        new CloudMedia.SimpleActionListener() {
                    @Override
                    public boolean onResult(String result) {
                        if(result.equalsIgnoreCase("OK")){
                            Log.i(TAG, "start push media is OK");
                        }
                        return true;
                    }
                });
            }
        });

        final Button buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mRemoteMediaNode.stopPushMedia("rtmp://xxx.xxx.xxx.xxx/app/name");

                mRemoteMediaNode.stopPushMedia("rtmp://xxx.xxx.xxx.xxx/app/name",
                        new CloudMedia.SimpleActionListener() {
                    @Override
                    public boolean onResult(String result) {
                        if(result.equalsIgnoreCase("OK")){
                            Log.i(TAG, "stop push media is OK");
                        }
                        return true;
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
