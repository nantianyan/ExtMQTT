package com.example.cloudmedia;

import android.util.Log;
import android.widget.Toast;

import com.example.p2pmqtt.P2PMqttRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/10/18.
 */

public class LocalMediaNode{
    private static final String TAG = "LocalMediaNode";
    private CloudMedia mCloudMedia;

    public LocalMediaNode(CloudMedia cm) {
        mCloudMedia = cm;
        cm.handleRequest(RPCMethod.START_PUSH_MEDIA, new StartPushMediaHandler());
        cm.handleRequest(RPCMethod.STOP_PUSH_MEDIA, new StopPushMediaHandler());
    }

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
                        return  CloudMedia.RPCSuccess;
                    } else {
                        return CloudMedia.RPCFailure;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return CloudMedia.RPCFailure;
            }

            return CloudMedia.RPCSuccess;
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
                        return CloudMedia.RPCSuccess;
                    }else {
                        return CloudMedia.RPCFailure;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return CloudMedia.RPCFailure;
            }

            return CloudMedia.RPCSuccess;
        }
    }

    /**
     * Interface definition for actor to be invoked when received
     * startPushMedia RPC request
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
     * stopPushMedia RPC request
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
