package com.example.democm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    private List<String> mNodesOnline = new ArrayList<String>();

    private void _putOnline() {
        Log.d(TAG, "_putOnline");
        mCloudMedia.putOnline(new CloudMedia.SimpleActionListener() {
            @Override
            public boolean onResult(String result) {
                Log.i(TAG, "offline OK");
                mButtonOnline.setText("下线");
                mIsOnline = true;
                return true;
            }
        });
    }
    private void _putOffline() {
        Log.d(TAG, "_putOffline");
        mCloudMedia.putOffline(new CloudMedia.SimpleActionListener() {
            @Override
            public boolean onResult(String result) {
                Log.i(TAG, "offline OK");
                mButtonOnline.setText("上线");
                mIsOnline = false;
                return true;
            }
        });
    }

    private void initCloudMedia(){
        mEtMyNick = (EditText)findViewById(R.id.etMyNick);
        mMyNick = mEtMyNick.getText().toString();
        mCloudMedia = new CloudMedia(mContext, mMyNick, CloudMedia.ROLE_PUSHER);

        mCloudMedia.connect(new CloudMedia.SimpleActionListener() {
            @Override
            public boolean onResult(String result) {
                Log.i(TAG, "connect result is: " + result);
                mButtonOnline.setText("上线");
                mButtonOnline.setEnabled(true);
                mButtonGetOnlineNodes.setEnabled(true);
                return true;
            }
        });

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCloudMedia();

        mListViewNodesOnline = (ListView) findViewById(R.id.listViewNodesOnline);

        mButtonOnline = (Button) findViewById(R.id.buttonConnect);
        mButtonOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCloudMedia != null) {
                    if (mIsOnline) {
                        _putOffline();
                    } else {
                        _putOnline();
                    }
                } else {
                    Log.e(TAG, "CloudMedia has not been initialized.");
                }
            }
        });

        mButtonGetOnlineNodes = (Button) findViewById(R.id.buttonGetOnlineNodes);
        mButtonGetOnlineNodes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }

                mCloudMedia.getNodesOnline(CloudMedia.ROLE_PUSHER, new CloudMedia.SimpleActionListener(){
                    @Override
                    public boolean onResult(String result) {
                        Log.i(TAG, "getNodesOnline:");
                        Log.i(TAG, ">>> " + result);
                        try {
                            JSONArray jsonNodes = new JSONArray(result);
                            mNodesOnline.clear();
                            for(int i=0; i<jsonNodes.length(); i++){
                                JSONObject node = jsonNodes.getJSONObject(i);
                                String whoami = node.getString("whoami");
                                mNodesOnline.add(whoami);
                                Log.d(TAG, "whoami:" + whoami);
                            }

                            mListViewNodesOnline.setAdapter(new ArrayAdapter<String>(mContext,
                                    android.R.layout.simple_list_item_1,
                                    (String[])mNodesOnline.toArray(new String[mNodesOnline.size()])));

                            mListViewNodesOnline.setOnItemClickListener(
                                    new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Log.d(TAG, "您点击了第"+position+"个项目");
                                            Log.d(TAG, "whoami is:" + mNodesOnline.get(position));
                                            String yourID = mNodesOnline.get(position);

                                            view.setBackgroundColor(Color.RED);

                                            // to use singleton ?
                                            mRemoteMediaNode = mCloudMedia.declareRemoteMediaNode(yourID);

                                            mRemoteMediaNode.startPushMedia(new CloudMedia.SimpleActionListener() {
                                                @Override
                                                public boolean onResult(String result) {
                                                    //if(result.equalsIgnoreCase("OK")){
                                                        Log.i(TAG, "start push media is OK");

                                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                                        intent.setData(Uri.parse(mRemoteMediaNode.getFlvPlayUrl()));
                                                        startActivity(intent);
                                                    //}
                                                    return true;
                                                }
                                            });
                                            /*
                                            mRemoteMediaNode.stopPushMedia(new CloudMedia.SimpleActionListener() {
                                                @Override
                                                public boolean onResult(String result) {
                                                    if(result.equalsIgnoreCase("OK")){
                                                        Log.i(TAG, "stop push media is OK");
                                                    }
                                                    return true;
                                                }
                                            });
                                            */
                                        }
                                    }
                            );
                        } catch (JSONException e) {
                            e.printStackTrace();
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
