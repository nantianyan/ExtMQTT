package com.example.cloudmedia;

import android.util.Log;

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
    private P2PMqtt mExtMqttClient; //TODO: cycle reference
    private String mRtmpPublishUrl;

    public static final String REQUEST_START_PUSH_MEDIA = "startPushMedia";
    public static final String REQUEST_STOP_PUSH_MEDIA = "stopPushMedia";

    private RemoteMediaNode(P2PMqtt mqttClient, String whoareyou){
        mWhoareyou = whoareyou;
        mExtMqttClient = mqttClient;
        mRtmpPublishUrl = generateRtmpPublishUrl();
    }

    public static RemoteMediaNode create(P2PMqtt mqttClient, String whoareyou){
        return new RemoteMediaNode(mqttClient, whoareyou);
    }

    private String generateRtmpPublishUrl(){
        String streamName = mWhoareyou + System.nanoTime();
        return "rtmp://video-center.alivecdn.com/cloudmedia/" + streamName + "?vhost=push.yangxudong.com";
    }

    public boolean startPushMedia(final CloudMedia.SimpleActionListener listener){
        String url = mRtmpPublishUrl;

        P2PMqttAsyncRequest request = new P2PMqttAsyncRequest();
        request.setWhoareyou(mWhoareyou);
        request.setMethodName(REQUEST_START_PUSH_MEDIA);
        request.setMethodParams(url);
        if(listener == null)
            request.setListener(P2PMqttRequest.SIMPLE_LISTENER);
        else {
            request.setListener(new P2PMqtt.IMqttRpcActionListener() {
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

        if(mExtMqttClient.sendRequest(request)) {
            Log.d(TAG, "startPushMedia is called succussfull!");
            return  true;
        } else {
            Log.d(TAG, "startPushMedia is failed!");
            return false;
        }
    }

    public boolean stopPushMedia(final CloudMedia.SimpleActionListener listener){
        String url = mRtmpPublishUrl;

        P2PMqttAsyncRequest request = new P2PMqttAsyncRequest();
        request.setWhoareyou(mWhoareyou);
        request.setMethodName(REQUEST_STOP_PUSH_MEDIA);
        request.setMethodParams(url);
        if(listener == null) {
            request.setListener(P2PMqttRequest.SIMPLE_LISTENER);
        } else {
            request.setListener(new P2PMqtt.IMqttRpcActionListener() {
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

        if(mExtMqttClient.sendRequest(request)) {
            Log.d(TAG, "stopPushMedia is called succussfull!");
        } else {
            Log.d(TAG, "stopPushMedia is failed!");
        }
        return true;
    }

    public boolean setPushParams(String params){
        return true;
    }

    public String[] getPushParams(String[] keys){
        return null;
    }

}