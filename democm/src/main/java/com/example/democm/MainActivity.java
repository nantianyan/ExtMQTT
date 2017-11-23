package com.example.democm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cloudmedia.CloudMedia;
import com.example.cloudmedia.LocalMediaNode;
import com.example.cloudmedia.RemoteMediaNode;
import com.example.p2pmqtt.P2PMqttRequest;
import com.example.p2pmqtt.P2PMqttSyncRequest;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CloudMediaDemo";
    private final Context mContext = this;
    private CloudMedia mCloudMedia;
    private RemoteMediaNode mRemoteMediaNode;
    private LocalMediaNode mLocalMediaNode;

    private String mMyNick;
    private EditText mEtMyNick;
    private String mYourID;
    private EditText mEtYourID;
    private Button mButtonConnect;
    private Button mButtonStart;
    private Button mButtonStop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEtMyNick = (EditText)findViewById(R.id.etMyNick);
        mEtYourID = (EditText)findViewById(R.id.etYourID);

        mButtonConnect = (Button) findViewById(R.id.buttonConnect);
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMyNick = mEtMyNick.getText().toString();
                mCloudMedia = new CloudMedia(mContext, mMyNick);

                mCloudMedia.connect(new CloudMedia.SimpleActionListener() {
                    @Override
                    public boolean onResult(String result) {
                        Log.i(TAG, "connect result is: " + result);
                        mButtonStart.setEnabled(true);
                        mButtonConnect.setEnabled(false);
                        return true;
                    }
                });

                mYourID = mEtYourID.getText().toString();
                mRemoteMediaNode = mCloudMedia.declareRemoteMediaNode(mYourID);

                mLocalMediaNode = mCloudMedia.declareLocalMediaNode();
                mLocalMediaNode.setOnStartPushMediaActor(new LocalMediaNode.OnStartPushMedia() {
                    @Override
                    public void onStartPushMedia(String params) {
                        Toast.makeText(mContext, "start push", Toast.LENGTH_LONG).show();
                    }
                });
                mLocalMediaNode.setOnStopPushMediaActor(new LocalMediaNode.OnStopPushMedia() {
                    @Override
                    public void onStopPushMedia(String params) {
                        Toast.makeText(mContext, "stop push", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        mButtonStart = (Button) findViewById(R.id.buttonStart);
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRemoteMediaNode.startPushMedia(new CloudMedia.SimpleActionListener() {
                    @Override
                    public boolean onResult(String result) {
                        if(result.equalsIgnoreCase("OK")){
                            Log.i(TAG, "start push media is OK");
                        }
                        return true;
                    }
                });

                mButtonStart.setEnabled(false);
                if(!mButtonStop.isEnabled())
                    mButtonStop.setEnabled(true);
            }
        });

        mButtonStop = (Button) findViewById(R.id.buttonStop);
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRemoteMediaNode.stopPushMedia(new CloudMedia.SimpleActionListener() {
                    @Override
                    public boolean onResult(String result) {
                        if(result.equalsIgnoreCase("OK")){
                            Log.i(TAG, "stop push media is OK");
                        }
                        return true;
                    }
                });

                mButtonStop.setEnabled(false);
                if(!mButtonStart.isEnabled())
                    mButtonStart.setEnabled(true);
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
