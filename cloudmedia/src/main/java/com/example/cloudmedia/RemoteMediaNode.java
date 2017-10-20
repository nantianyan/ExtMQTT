package com.example.cloudmedia;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.P2PMqttAsyncRequest;
import com.example.p2pmqtt.P2PMqttRequest;
import com.example.p2pmqtt.P2PMqttSyncRequest;

/**
 * Created by 阳旭东 on 2017/10/18.
 */

public class RemoteMediaNode{
    private static final String TAG = "RemoteMediaNode";
    private String mWhoareyou;
    private P2PMqtt mExtMqttClient; //TODO: cycle reference

    public static final String REQUEST_START_PUSH_MEDIA = "startPushMedia";
    public static final String REQUEST_STOP_PUSH_MEDIA = "stopPushMedia";

    private RemoteMediaNode(P2PMqtt mqttClient, String whoareyou){
        mWhoareyou = whoareyou;
        mExtMqttClient = mqttClient;
    }

    public static RemoteMediaNode create(P2PMqtt mqttClient, String whoareyou){
        return new RemoteMediaNode(mqttClient, whoareyou);
    }

    public boolean startPushMedia(String url){
        P2PMqttAsyncRequest request = new P2PMqttAsyncRequest();
        request.setWhoareyou(mWhoareyou);
        request.setMethodName(REQUEST_START_PUSH_MEDIA);
        request.setMethodParams(url);
        request.setListener(P2PMqttRequest.SIMPLE_LISTENER);

        if(mExtMqttClient.sendRequest(request)) {
            Log.d(TAG, "startPushMedia is called succussfull!");
        } else {
            Log.d(TAG, "startPushMedia is failed!");
        }
        return true;
    }

    public boolean stopPushMedia(String url){
        P2PMqttAsyncRequest request = new P2PMqttAsyncRequest();
        request.setWhoareyou(mWhoareyou);
        request.setMethodName(REQUEST_STOP_PUSH_MEDIA);
        request.setMethodParams(url);
        request.setListener(P2PMqttRequest.SIMPLE_LISTENER);

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