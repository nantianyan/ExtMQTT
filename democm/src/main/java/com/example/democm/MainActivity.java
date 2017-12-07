package com.example.democm;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.alivc.live.pusher.AlivcFpsEnum;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushError;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushNetworkListener;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.alivc.live.pusher.AlivcResolutionEnum;
import com.example.cloudmedia.CloudMedia;
import com.example.cloudmedia.LocalMediaNode;
import com.example.cloudmedia.RemoteMediaNode;

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
    private Button mButtonGetOnlineNodes;
    private ListView mListViewNodesOnline;

    private SurfaceView mSurfaceView;
    private PusherController mCurrentPusher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEtMyNick = (EditText)findViewById(R.id.etMyNick);
        mListViewNodesOnline = (ListView) findViewById(R.id.listViewNodesOnline);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);

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
         * button to trigger the test about on off line media node
         */
        handleButtonOnOffLine();

        /**
         * test install handler for local media node
         */
        installLocalMediaNodeHandler();

        //testPlayer("rtmp://push.yangxudong.com/cloudmedia/CloudMedia_79128575502638");

        //testPusher();
    }

    @Override
    protected void onDestroy() {
        mCurrentPusher.onDestroy();

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
                showOnlineNodes(nodesList);
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

    private void showOnlineNodes(final CloudMedia.NodesList nodesList){
        Log.i(TAG, "call showOnlineNodes");

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
                        String targetID = nodesList.mNodesID.get(position);

                        /*
                        if(mCurrentPusher == null) {
                            mCurrentPusher = new PusherController(mCloudMedia, targetID);
                            mCurrentPusher.initPlayer(mContext, mSurfaceView);
                        }

                        if(mCurrentPusher.getWhoareyou() != targetID) {
                            // TODO: stop the original pusher
                            mCurrentPusher = new PusherController(mCloudMedia,targetID);
                            mCurrentPusher.initPlayer(mContext, mSurfaceView);
                        }
                        */

                        if(mCurrentPusher != null) {
                            mCurrentPusher.onDestroy();
                            mCurrentPusher = null;
                        }
                        mCurrentPusher = new PusherController(mCloudMedia, targetID);
                        mCurrentPusher.initPlayer(mContext, mSurfaceView);

                        if(mCurrentPusher.getStatus() == PusherController.PlayerStatus.STOPED){
                            mCurrentPusher.startPushMedia(new CloudMedia.SimpleActionListener() {
                                @Override
                                public boolean onResult(String result) {
                                    return true;
                                }
                            });
                        }
                        if (mCurrentPusher.getStatus() == PusherController.PlayerStatus.PLAYING) {
                            mCurrentPusher.stopPushMedia(new CloudMedia.SimpleActionListener() {
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
        mButtonOnline = (Button) findViewById(R.id.buttonConnect);
        mButtonOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCloudMedia != null) {
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

    private void installLocalMediaNodeHandler(){
        mLocalMediaNode = mCloudMedia.declareLocalMediaNode();
        mLocalMediaNode.setOnStartPushMediaActor(new LocalMediaNode.OnStartPushMedia() {
            @Override
            public boolean onStartPushMedia(String params) {
                Toast.makeText(mContext, "start push", Toast.LENGTH_LONG).show();
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

    private void testPlayer(String url){
        if(mCurrentPusher == null) {
            mCurrentPusher = new PusherController(mCloudMedia, "123");
            mCurrentPusher.initPlayer(mContext, mSurfaceView);
            mCurrentPusher.testPlayer(url);
        }
    }

    private void testPusher(){
        Pusher mPusher = new Pusher(mContext, mSurfaceView);
        mPusher.initPusher();
    }
}
