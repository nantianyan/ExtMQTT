package com.example.cloudmedia;

import android.util.Log;

import com.example.p2pmqtt.MqttTopicHandler;
import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.P2PMqttAsyncRequest;
import com.example.p2pmqtt.P2PMqttRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/10/18.
 */

public class RemoteMediaNode{
    private static final String TAG = "RemoteMediaNode";
    private String mWhoareyou;
    private CloudMedia mCloudMedia;

    private RemoteMediaNode(CloudMedia cm, String whoareyou){
        mWhoareyou = whoareyou;
        mCloudMedia = cm;
    }

    public static RemoteMediaNode create(CloudMedia cm, String whoareyou){
        return new RemoteMediaNode(cm, whoareyou);
    }

    public boolean startPushMedia(final CloudMedia.SimpleActionListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "target-id", mWhoareyou);
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "expire-time", "100s");
        params = P2PMqtt.MyJsonString.addJsonBrace(params);

        return mCloudMedia.sendRequest(mCloudMedia.whoisMC(), RPCMethod.START_PUSH_MEDIA, params, listener);
    }

    public boolean stopPushMedia(final CloudMedia.SimpleActionListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "target-id", mWhoareyou);
        params = P2PMqtt.MyJsonString.addJsonBrace(params);

        return mCloudMedia.sendRequest(mCloudMedia.whoisMC(), RPCMethod.STOP_PUSH_MEDIA, null, listener);
    }

}
