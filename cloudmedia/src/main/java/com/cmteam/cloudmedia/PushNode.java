package com.cmteam.cloudmedia;

import android.content.Context;
import android.util.Log;

import com.cmteam.p2pmqtt.P2PMqtt;
import com.cmteam.p2pmqtt.P2PMqttRequestHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * PushNode: represents a stream pusher(PUSH node) with one camera on it,
 * one media device may mount several pushing nodes
 */
public class PushNode extends MediaNode {
    private static final String TAG = "PushNode";
    private OnStopPushMedia mOnStopPushMediaActor;
    private OnStartPushMedia mOnStartPushMediaActor;

    public PushNode(Context context, String nick, String deviceName) {
	    mContext = context;
        mNode = new Node();
        mNode.setRole(CloudMedia.CMRole.ROLE_PUSHER.str());
        mNode.setNick(nick);
        mNode.setDeviceName(deviceName);
        mNode.setLocation(FIELD_LOCATION_DEFAULT);
        mNode.setStreamStatus(CloudMedia.CMStreamStatus.PUSHING_CLOSE);
    }

    @Override
    public boolean connect(final CloudMedia.CMUser user, final CloudMedia.RPCResultListener listener) {
        if (!CloudMedia.CMRole.ROLE_PUSHER.str().equals(user.role)) {
            Log.e(TAG, "account role not match");
            return false;
        }
        mNode.setID(user.nodeID);
        mNode.setGroupID(user.groupID);
        mNode.setGroupNick(user.groupNick);
        mNode.setVendorID(user.vendorID);
        mNode.setVendorNick(user.vendorNick);

        mExtMqttClient = new P2PMqtt(mContext, whoami(), "12345");
        mTopicHandler = new TopicHandler(mExtMqttClient);
        mExtMqttClient.installTopicHandler(mTopicHandler);

        String brokerUrl = getBrokerUrl();
        if (brokerUrl == null) {
            Log.e(TAG, "get broker URL failed");
			listener.onFailure(RPCFailure);
            return false;
        }

        boolean connectResult = mExtMqttClient.connect(brokerUrl, new P2PMqtt.IFullActionListener() {
            @Override
            public void onSuccess(String params) {
                putOnline(new CloudMedia.RPCResultListener() {
                    @Override
                    public void onSuccess(String params) {
                        listener.onSuccess(RPCSuccess);
                    }
                    @Override
                    public void onFailure(String params) {listener.onFailure(RPCFailure);}
                });
            }

            @Override
            public void onFailure(String params) {
                listener.onFailure(RPCFailure);
            }
        });

        if (connectResult) {
            handleRequest(RPCMethod.START_PUSH_MEDIA, new StartPushMediaHandler());
            handleRequest(RPCMethod.STOP_PUSH_MEDIA, new StopPushMediaHandler());
        }

        return connectResult;
    }

    @Override
    public boolean disconnect() {
        putOffline(null);
        mExtMqttClient.disconnect();
        return  true;
    }


    /**
     * Interface definition for actor to be invoked when received
     * startPushMedia RPC request
     */
    public interface OnStartPushMedia
    {
        boolean onStartPushMedia(String params);
    }

    public void setOnStartPushMediaActor(OnStartPushMedia actor) {
        mOnStartPushMediaActor = actor;
    }

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
                        return  RPCSuccess;
                    } else {
                        return RPCFailure;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return RPCFailure;
            }

            return RPCSuccess;
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
                        return RPCSuccess;
                    }else {
                        return RPCFailure;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return RPCFailure;
            }

            return RPCSuccess;
        }
    }

}
