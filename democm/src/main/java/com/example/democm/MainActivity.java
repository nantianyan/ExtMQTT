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

    private String mMyID;
    private String mYourID;
    private EditText mEtMyID;
    private EditText mEtYourID;
    private Button mButtonConnect;
    private Button mButtonStart;
    private Button mButtonStop;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private static final int HTTP_REQUEST = 200;
    private static final int HTTP_REQUEST_ID = HTTP_REQUEST + 1;

    private Handler mMainHandler;
    private static final int UI_REQUEST = 100;
    private static final int UI_REQUEST_SET_MY_ID = UI_REQUEST + 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEtMyID = (EditText)findViewById(R.id.etMyID);
        mEtYourID = (EditText)findViewById(R.id.etYourID);


        mHandlerThread = new HandlerThread("IDManager");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == HTTP_REQUEST) {
                    switch (msg.arg1) {
                        case HTTP_REQUEST_ID:
                            String myID = CloudMedia.getIDFromServer();
                            Log.i(TAG, "getIDFromeServer:" + myID);
                            // note: you can not set UI widget from this thread. like:
                            // mEtMyID.setText(myID);
                            Message m = mMainHandler.obtainMessage(UI_REQUEST, UI_REQUEST_SET_MY_ID, 0, myID);
                            mMainHandler.sendMessage(m);
                            break;
                    }

                }

            }
        };

        mMainHandler = new Handler(getMainLooper()) {
          @Override
          public void handleMessage(Message msg) {
              super.handleMessage(msg);
              if(msg.what == UI_REQUEST) {
                  switch (msg.arg1) {
                      case UI_REQUEST_SET_MY_ID:
                          mEtMyID.setText((String)msg.obj);
                          break;
                  }

              }
          }
        };

        Button mButtonGetID = (Button) findViewById(R.id.buttonGetID);
        mButtonGetID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message m = mHandler.obtainMessage(HTTP_REQUEST, HTTP_REQUEST_ID, 0, null);
                mHandler.sendMessage(m);
            }
        });

        mButtonConnect = (Button) findViewById(R.id.buttonConnect);
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMyID = mEtMyID.getText().toString();
                Log.i(TAG, "myID: " + mMyID);

                mCloudMedia = new CloudMedia(mContext, mMyID);

                mCloudMedia.connect("tcp://139.224.128.15:1883",
                        new CloudMedia.SimpleActionListener() {
                    @Override
                    public boolean onResult(String result) {
                        Log.i(TAG, "connect result is: " + result);
                        mButtonStart.setEnabled(true);
                        mButtonConnect.setEnabled(false);
                        return true;
                    }
                });

                mYourID = mEtYourID.getText().toString();
                Log.i(TAG, "Your ID: " + mYourID);

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

                mButtonStart.setEnabled(false);
                if(!mButtonStop.isEnabled())
                    mButtonStop.setEnabled(true);
            }
        });

        mButtonStop = (Button) findViewById(R.id.buttonStop);
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
