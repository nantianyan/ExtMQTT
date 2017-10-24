package com.example.cloudmedia;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.P2PMqttRequest;
import com.example.p2pmqtt.P2PMqttSyncRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/10/18.
 */

public class CloudMedia {
    private static final String TAG = "CloudMedia";
    private Context mContext;
    private P2PMqtt mExtMqttClient;
    private String mBrokerUrl;
    private  String mMyID;

    public CloudMedia(Context context, String myID) {
        mContext = context;
        mMyID = myID;
    }

    public boolean connect(String brokerUrl, final SimpleActionListener listener) {
        assert(brokerUrl != null);
        assert(listener != null);
        mBrokerUrl = brokerUrl;

        if(mMyID == null) {
            //request a dynamic ID
            mMyID = "puller";
        }
        mExtMqttClient = new P2PMqtt(mContext, mMyID, "12345");

        return mExtMqttClient.connect("tcp://139.224.128.15:1883",
                new P2PMqtt.IMqttRpcActionListener() {
            @Override
            public P2PMqtt.ResultCode onResult(JSONObject jrpc) {
                String result = null;
                try {
                    result = jrpc.getString("result");
                    Log.d(TAG, "jrpc's result is: " + result);
                } catch (JSONException e) {
                    Log.d(TAG, "illeagle JSON!");
                    e.printStackTrace();
                }
                if (listener.onResult(result)) {
                    return P2PMqtt.ResultCode.ERROR_None;
                } else {
                    return P2PMqtt.ResultCode.ERROR_Unknown;
                }
            }
        });
    }

    public RemoteMediaNode declareRemoteMediaNode(String whoareyou){
        return RemoteMediaNode.create(mExtMqttClient, whoareyou);
    }

    public LocalMediaNode declareLocalMediaNode() {
        return new LocalMediaNode(mExtMqttClient);
    }

    // not implemented yet.
    // in our original design, LiveServer interface should exposed from MQTT media controller
    // yet another way is tack to cloud live media server directly.
    // further more, client may have no idea about his interface?
    LiveServerNode declareLiveServer(){
        return new LiveServerNode();
    }

    public interface SimpleActionListener {
        boolean onResult(String result);
    }
}
