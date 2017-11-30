package com.example.democm;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEtMyNick = (EditText)findViewById(R.id.etMyNick);
        mListViewNodesOnline = (ListView) findViewById(R.id.listViewNodesOnline);

        mCloudMedia = new CloudMedia(mContext);

        /**
         * install nodes status listener.
         * when nodes on/off or other field update, the listener will be triggered
         */
        testNodesStatusListener();

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


        /**
         * button to trigger the test about on off line media node
         */
        testCloudMedia_OnOffLine();

        /**
         * test install handler for local media node
         */
        testLocalMediaNode();
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
                        testRemoteMediaNode(targetID);
                    }
                }
        );
    }

    private void testRemoteMediaNode(String targetNodeID){
        // to use singleton ?
        mRemoteMediaNode = mCloudMedia.declareRemoteMediaNode(targetNodeID);

        mRemoteMediaNode.startPushMedia(new CloudMedia.SimpleActionListener() {
            @Override
            public boolean onResult(String result) {
                //if(result.equalsIgnoreCase("OK")){
                Log.i(TAG, "start push media is OK");
                /*
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(mRemoteMediaNode.getFlvPlayUrl()), "video/flv");
                startActivity(intent);
                */
                //}
                return true;
            }
        });
    }

    private void testLocalMediaNode(){
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

    private void testNodesStatusListener(){
        mCloudMedia.setNodesStatusChangeListener(new CloudMedia.OnNodesStatusChange() {
            @Override
            public boolean OnNodesStatusChange(CloudMedia.NodesList nodesList) {
                showOnlineNodes(nodesList);
                return true;
            }
        });
    }

    private void testCloudMedia_OnOffLine(){
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

}
