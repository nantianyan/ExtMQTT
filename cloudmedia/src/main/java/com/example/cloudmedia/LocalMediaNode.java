package com.example.cloudmedia;

import android.util.Log;
import android.widget.Toast;

import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.P2PMqttRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/10/18.
 */

public class LocalMediaNode{
    private static final String TAG = "LocalMediaNode";
    private P2PMqtt mExtMqttClient;


    private class StartPushMediaHandler extends P2PMqttRequestHandler {
        public String HandleJrpc (JSONObject jrpc){
            try {
                Log.d(TAG, "this is StartPushMediaHandler's topicHandle");
                String method = jrpc.getString("method");
                String params = jrpc.getString("params");
                Log.d(TAG, "method:" + method);
                if(mOnStartPushMediaActor != null) {
                    mOnStartPushMediaActor.onStartPushMedia(params);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "ERROR";
            }

            return "OK";
        }
    }

    private class StopPushMediaHandler extends P2PMqttRequestHandler {
        public String HandleJrpc (JSONObject jrpc){
            try {
                Log.d(TAG, "this is StopPushMediaHandler's topicHandle");
                String method = jrpc.getString("method");
                String params = jrpc.getString("params");
                Log.d(TAG, "method:" + method);
                if(mOnStopPushMediaActor != null) {
                    mOnStopPushMediaActor.onStopPushMedia(params);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "ERROR";
            }

            return "OK";
        }
    }

    LocalMediaNode(P2PMqtt mqttClient){
        mExtMqttClient = mqttClient;

        P2PMqttRequestHandler handler1 = new StartPushMediaHandler();
        mExtMqttClient.installRequestHandler(RemoteMediaNode.REQUEST_START_PUSH_MEDIA, handler1);

        P2PMqttRequestHandler handler2 = new StopPushMediaHandler();
        mExtMqttClient.installRequestHandler(RemoteMediaNode.REQUEST_STOP_PUSH_MEDIA, handler2);
    }

    /**
     * Interface definition for actor to be invoked when received
     * startPushMedia MQTT request
     */
    public interface OnStartPushMedia
    {
        void onStartPushMedia(String params);
    }

    public void setOnStartPushMediaActor(OnStartPushMedia actor){
        mOnStartPushMediaActor = actor;
    }

    private OnStartPushMedia mOnStartPushMediaActor;

    /**
     * Interface definition for actor to be invoked when received
     * stopPushMedia MQTT request
     */
    public interface OnStopPushMedia
    {
        void onStopPushMedia(String params);
    }

    public void setOnStopPushMediaActor(OnStopPushMedia actor){
        mOnStopPushMediaActor = actor;
    }

    private OnStopPushMedia mOnStopPushMediaActor;

    void updateStatus(){
        // nothing yet
    }
}