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
    private CloudMedia mCloudMedia;
    private P2PMqtt mExtMqttClient;

    private class StartPushMediaHandler extends P2PMqttRequestHandler {
        public String HandleJrpc (final JSONObject jrpc){
            try {
                Log.d(TAG, "this is StartPushMediaHandler's topicHandle");
                String method = jrpc.getString("method");
                String params = jrpc.getString("params");
                String id = jrpc.getString("id");
                Log.d(TAG, "method:" + method);
                Log.d(TAG, "params:" + params);
                Log.d(TAG, "id:" + id);
                if(mOnStartPushMediaActor != null) {
                    if(mOnStartPushMediaActor.onStartPushMedia(params)) {
                        return  "OK";
                    } else {
                        return "ERROR";
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "ERROR";
            }

            return "OK";
        }
    }

    private class StopPushMediaHandler extends P2PMqttRequestHandler {
        public String HandleJrpc (final JSONObject jrpc){
            try {
                Log.d(TAG, "this is StopPushMediaHandler's topicHandle");
                String method = jrpc.getString("method");
                String params = jrpc.getString("params");
                Log.d(TAG, "method:" + method);
                if(mOnStopPushMediaActor != null) {
                    if(mOnStopPushMediaActor.onStopPushMedia(params)){
                        return "OK";
                    }else {
                        return "ERROR";
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "ERROR";
            }

            return "OK";
        }
    }

    LocalMediaNode(CloudMedia cm){
        mCloudMedia = cm;
        mExtMqttClient = cm.getMqtt();

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
        boolean onStartPushMedia(String params);
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
        boolean onStopPushMedia(String params);
    }

    public void setOnStopPushMediaActor(OnStopPushMedia actor){
        mOnStopPushMediaActor = actor;
    }

    private OnStopPushMedia mOnStopPushMediaActor;

}
