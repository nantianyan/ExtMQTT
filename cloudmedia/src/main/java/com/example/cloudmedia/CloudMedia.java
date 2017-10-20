package com.example.cloudmedia;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.P2PMqttRequest;
import com.example.p2pmqtt.P2PMqttSyncRequest;

/**
 * Created by 阳旭东 on 2017/10/18.
 */

public class CloudMedia {
    private Context mContext;
    private P2PMqtt mExtMqttClient;
    private String mBrokerUrl;
    private  String mMyID;

    public CloudMedia(Context context, String brokerUrl) {
        mContext = context;
        mBrokerUrl = brokerUrl;
        mMyID = null;
    }

    public CloudMedia(Context context, String brokerUrl, String myID) {
        mContext = context;
        mBrokerUrl = brokerUrl;
        mMyID = myID;
    }

    public boolean init() {
        if(mMyID == null) {
            //request a dynamic ID
            mMyID = "puller";
        }
        mExtMqttClient = new P2PMqtt(mContext, mMyID, "12345");
        return mExtMqttClient.connect("tcp://139.224.128.15:1883");
    }

    RemoteMediaNode declareRemoteMediaNode(String whoareyou){
        return RemoteMediaNode.create(mExtMqttClient, whoareyou);
    }

    LocalMediaNode declareLocalMediaNode() {
        return new LocalMediaNode(mExtMqttClient);
    }

    // not implemented yet.
    // in our original design, LiveServer interface should exposed from MQTT media controller
    // yet another way is tack to cloud live media server directly.
    // further more, client may have no idea about his interface?
    LiveServerNode declareLiveServer(){
        return new LiveServerNode();
    }
}
