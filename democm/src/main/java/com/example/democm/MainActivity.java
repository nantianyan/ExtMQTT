package com.example.democm;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cloudmedia.CloudMedia;
import com.example.cloudmedia.LocalMediaNode;
import com.example.cloudmedia.RemoteMediaNode;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "CloudMediaDemo";
    private final Context mContext = this;
    private CloudMedia mCloudMedia;
    private boolean mIsOnline = false;

    private RemoteMediaNode mRemoteMediaNode;
    private LocalMediaNode mLocalMediaNode;

    private String mMyNick;
    private EditText mEtMyNick;
    private Button mButtonOnline;
    private Button mButtonPlay;
    private ListView mListViewNodesOnline;

    private SurfaceView mSurfaceView;
    private PusherController mPusherController;
    private Player mPlayer;

    private SurfaceView mSurfaceView2;
    private Pusher mPusher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEtMyNick = (EditText)findViewById(R.id.etMyNick);

        mButtonOnline = (Button) findViewById(R.id.buttonConnect);
        handleButtonOnOffLine();

        mButtonPlay = (Button)findViewById(R.id.buttonPlay);
        handleButtonPlay();

        mListViewNodesOnline = (ListView) findViewById(R.id.listViewNodesOnline);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView2 = (SurfaceView) findViewById(R.id.surfaceView2);

        int showID = 3;
        ViewGroup.LayoutParams lp;
        switch (showID) {
            case 1:
                mSurfaceView2.setVisibility(View.GONE);
                lp = mSurfaceView.getLayoutParams();
                lp.height = 320; //lp.width = 240;
                mSurfaceView.setLayoutParams(lp);
                break;
            case 2:
                mSurfaceView.setVisibility(View.GONE);
                lp = mSurfaceView2.getLayoutParams();
                lp.height = 320; //lp.width = 240;
                mSurfaceView2.setLayoutParams(lp);
                break;
            case 3:
                break;
        }

        mPlayer = new Player(this, mSurfaceView);
        mPlayer.init();

        mCloudMedia = new CloudMedia(mContext);

        /**
         * install nodes status listener.
         * when nodes on/off or other field update, the listener will be triggered.
         * further more:
         *  the lisener will deal with player
         */
        installNodesStatusListener();

        /**
         * connect to mqtt broker, and change the UI
         */
        connectToBroker();

        /**
         * test install handler for local media node
         */
        installLocalMediaNodeHandler();

        //testPlayer("rtmp://push.yangxudong.com/cloudmedia/CloudMedia_5773026858881", null);

        //testPusher();
    }

    @Override
    protected void onDestroy() {
        if(mPusherController != null)
            mPusherController.onDestroy();
        if(mPusher != null)
            mPusher.destroy();

        if(mCloudMedia != null) {
            mCloudMedia.disconnect();
        }

        super.onDestroy();
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

    private void installNodesStatusListener(){
        mCloudMedia.setNodesStatusChangeListener(new CloudMedia.OnNodesStatusChange() {
            @Override
            public boolean OnNodesStatusChange(CloudMedia.NodesList nodesList) {
                handleListViewNodesOnline(nodesList);
                return true;
            }
        });
    }

    private void connectToBroker(){
        mMyNick = mEtMyNick.getText().toString();
        mCloudMedia.connect(mMyNick, CloudMedia.CMRole.ROLE_PUSHER,
                new CloudMedia.FullActionListener() {
                    @Override
                    public void onSuccess(String params) {
                        Log.i(TAG, "connect onSuccess");
                        mIsOnline = true;

                        mButtonOnline.setText("下线");
                        mButtonOnline.setEnabled(true);
                    }

                    @Override
                    public void onFailure(String params) {
                        Log.e(TAG, "connect onFailure");
                    }
                });
    }

    private void handleListViewNodesOnline(final CloudMedia.NodesList nodesList){
        Log.i(TAG, "call handleListViewNodesOnline");
        for (CloudMedia.NodesList.Node e: nodesList.get()) {
            Log.d(TAG, "nick: " + e.getNick());
            Log.d(TAG, "status: " + e.getStatus());
        }

        mListViewNodesOnline.setAdapter(new ArrayAdapter<String>(mContext,
                android.R.layout.simple_list_item_1,
                (String[])nodesList.mNodesNick.toArray(new  String[nodesList.size()])));

        mListViewNodesOnline.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(TAG, "Clicke item: "+position);
                        view.setBackgroundColor(Color.RED);

                        Log.d(TAG, "the item's ID is:" + nodesList.mNodesID.get(position));
                        final String targetID = nodesList.mNodesID.get(position);
                        Log.d(TAG, "the item's targetID is: " + targetID);

                        mRemoteMediaNode = mCloudMedia.declareRemoteMediaNode(targetID);

                        mRemoteMediaNode.setStatusListener(new RemoteMediaNode.IStatusListener() {
                            @Override
                            public void onStatus(String status) {
                                Log.d(TAG, "onStatus: " + status);
                            }
                        });

                        if(nodesList.mNodesStatus.get(position) == CloudMedia.CMStatus.PUSHING.str()) {
                            mRemoteMediaNode.stopPushMedia(new CloudMedia.SimpleActionListener() {
                                @Override
                                public boolean onResult(String result) {
                                    return true;
                                }
                            });
                        } else {
                            mRemoteMediaNode.startPushMedia(new CloudMedia.SimpleActionListener() {
                                @Override
                                public boolean onResult(String result) {
                                    return true;
                                }
                            });
                        }

                    }
                }
        );
    }

    private void handleButtonOnOffLine(){
        mButtonOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCloudMedia != null) {
                    mMyNick = mEtMyNick.getText().toString();

                    if (mIsOnline) {
                        mCloudMedia.putOffline(mMyNick, CloudMedia.CMRole.ROLE_PUSHER,
                                new CloudMedia.SimpleActionListener() {
                                    @Override
                                    public boolean onResult(String result) {
                                        Log.i(TAG, "offline OK");
                                        mButtonOnline.setText("上线");
                                        mIsOnline = false;
                                        return true;
                                    }
                                });
                    } else {
                        mCloudMedia.putOnline(mMyNick, CloudMedia.CMRole.ROLE_PUSHER,
                                new CloudMedia.SimpleActionListener() {
                                    @Override
                                    public boolean onResult(String result) {
                                        Log.i(TAG, "online OK");
                                        mButtonOnline.setText("下线");
                                        mIsOnline = true;
                                        return true;
                                    }
                                });
                    }
                } else {
                    Log.e(TAG, "CloudMedia has not been initialized.");
                }
            }
        });
    }

    private void handleButtonPlay() {
        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onclick button play");
                if(mRemoteMediaNode != null) {
                    String url = mRemoteMediaNode.getRtmpPlayUrl();
                    //String url = mRemoteMediaNode.getFlvPlayUrl();
                    mPlayer.play(url);
                }
            }
        });
    }

    private void installLocalMediaNodeHandler(){
    mLocalMediaNode = mCloudMedia.declareLocalMediaNode();
    mLocalMediaNode.setOnStartPushMediaActor(new LocalMediaNode.OnStartPushMedia() {
        @Override
        public boolean onStartPushMedia(String params) {
            Toast.makeText(mContext, "start push", Toast.LENGTH_LONG).show();

            String url = "";
            try {
                JSONObject mJParams = new JSONObject(params);
                url = mJParams.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (url == "") {
                Toast.makeText(getApplicationContext(), "RTMP Server IP is NULL!", Toast.LENGTH_SHORT);
                return false;
            } else {
                Log.d(TAG, "url:" + url);
            }

            //if(mPusher == null) {
                mPusher = new Pusher(mContext, mSurfaceView2);
                mPusher.prepareAndPush(url);
                mPusher.startPush(url);
            //} else {
            //    Log.d(TAG, "pusher is working....");
            //    return false;
            //}

            return true;
        }
    });
    mLocalMediaNode.setOnStopPushMediaActor(new LocalMediaNode.OnStopPushMedia() {
        @Override
        public boolean onStopPushMedia(String params) {
            Toast.makeText(mContext, "stop push", Toast.LENGTH_LONG).show();
            return true;
        }
    });
}

    private void testPlayer(String url, String targetID){
        if(mPusherController == null) {
            if(targetID == null) targetID="testPlayer";
            mPusherController = new PusherController(mCloudMedia, targetID);
            mPusherController.initPlayer(mContext, mSurfaceView);
            if(url == null) url = mPusherController.getRtmpPlayUrl();
            mPusherController.testPlayer(url);
        }
    }

    private void testPusher(){
        Pusher mPusher = new Pusher(mContext, mSurfaceView2);
        mPusher.prepareAndPush("rtmp://video-center.alivecdn.com/AppName/StreamName?vhost=push.yangxudong.com");
    }
}
