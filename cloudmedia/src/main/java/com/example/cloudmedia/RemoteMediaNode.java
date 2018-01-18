package com.example.cloudmedia;

import android.util.Log;

import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.MqttTopicHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/10/18.
 */

public class RemoteMediaNode{
    private static final String TAG = "RemoteMediaNode";
    private CloudMedia mCloudMedia;
    private CloudMedia.Node mNode;
    private OnStreamException mStreamExceptionListener;

    private RemoteMediaNode(CloudMedia cm, CloudMedia.Node node){
        mNode = node;
        mCloudMedia = cm;
    }

    private String iswho() {
        return mCloudMedia.getVendorID() + "_" + mNode.getGroupID() + "_" + mNode.getID();
    }

    public static RemoteMediaNode create(CloudMedia cm, CloudMedia.Node node){
        return new RemoteMediaNode(cm, node);
    }

    /**
     * A PULLER calls it to request remote node to push stream
     */
    public boolean startPushMedia(final CloudMedia.RPCResultListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "target-id", iswho());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "expire-time", "100s");
        params = P2PMqtt.MyJsonString.addJsonBrace(params);

        return mCloudMedia.sendRequest(mCloudMedia.whoisMC(), RPCMethod.START_PUSH_MEDIA, params, listener);
    }

    /**
     * A PULLER calls it to request remote node to stop pushing stream
     */
    public boolean stopPushMedia(final CloudMedia.RPCResultListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "target-id", iswho());
        params = P2PMqtt.MyJsonString.addJsonBrace(params);

        return mCloudMedia.sendRequest(mCloudMedia.whoisMC(), RPCMethod.STOP_PUSH_MEDIA, params, listener);
    }

    /**
     * Stream exception definition which happens after startPushMedia returns RPC success
     * TIME_EXPIRED: the stream duration given by the PULLER is over
     * PUSHER_ERROR: some error occurs on the PUSHER
     * NETWORK_ERROR: network error occurs, for example the cloudy reports a streaming error
     * UNKNOWN_ERROR: unknown error caused
     */
    public enum StreamException{
        TIME_EXPIRED("time_expired"),
        PUSHER_ERROR("pusher_error"),
        NETWORK_ERROR("network_error"),
        UNKNOWN_ERROR("unknown_error");

        private final String mException;
        StreamException(String excep){
            mException = excep;
        }
        public String str(){
            return mException;
        }
        public static StreamException name(String excep) {
            for (StreamException se : StreamException.values()) {
                if (excep.equals(se.str())) {
                    return se;
                }
            }
            return UNKNOWN_ERROR;
        }
    }

    /**
     * A listener interface used to observe stream exception event from a PUSH node,
     * a PULL node must register this listener after it calls RemoteMediaNode.startPushMedia
     * with successful RPC result
     */
    public interface OnStreamException{
        void onStreamException(String nodeID, StreamException exception);
    }

    /**
     * A PULLER calls it to register a listener to observe exception during streaming
     */
    public void setStreamExceptionListener(OnStreamException listener) {
        String exctopic = Topic.generate("*", iswho(), Topic.Action.STREAM_EXCEPTION);
        if (listener != null) {
            MqttTopicHandler streamExceptionHandler = new MqttTopicHandler() {
                @Override
                public void onMqttMessage(String topic, String jstr) {
                    String str;
                    try {
                        JSONObject jsonObj = new JSONObject(jstr);
                        str = jsonObj.getString("stream_exception");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        str = StreamException.UNKNOWN_ERROR.str();
                    }
                    if (str == null)
                        str = StreamException.UNKNOWN_ERROR.str();
                    StreamException se = StreamException.name(str);

                    if (mStreamExceptionListener != null) {
                        mStreamExceptionListener.onStreamException(mNode.getID(), se);
                    }
                }
            };

            mCloudMedia.getTopicHandler().install(exctopic, streamExceptionHandler);
        } else if (mStreamExceptionListener != null) {
            mCloudMedia.getTopicHandler().uninstall(exctopic);
        }

        mStreamExceptionListener = listener;
    }

}
