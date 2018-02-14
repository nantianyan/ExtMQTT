package com.cmteam.cloudmedia;

import android.util.Log;

import com.cmteam.p2pmqtt.P2PMqtt;
import com.cmteam.p2pmqtt.MqttTopicHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * NodeProxy: represents a proxy to a remote node(PUSH node or PULL node)
 */
public class NodeProxy extends MediaNode {
    private static final String TAG = "NodeProxy";
    private OnStreamException mStreamExceptionListener;

    public NodeProxy(Node remoteNode){
        mNode = remoteNode;
    }

    /**
     * A PULLER calls it to request remote node to push stream
     */
    public boolean startPushMedia(final CloudMedia.RPCResultListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "target-id", whoami());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "expire-time", "100s");
        params = P2PMqtt.MyJsonString.addJsonBrace(params);

        return sendRequest(whoisMC(), RPCMethod.START_PUSH_MEDIA, params, listener);
    }

    /**
     * A PULLER calls it to request remote node to stop pushing stream
     */
    public boolean stopPushMedia(final CloudMedia.RPCResultListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "target-id", whoami());
        params = P2PMqtt.MyJsonString.addJsonBrace(params);

        return sendRequest(whoisMC(), RPCMethod.STOP_PUSH_MEDIA, params, listener);
    }

    /**
     * A listener interface used to observe stream exception event from a PUSH node,
     * a PULL node must register this listener after it calls RemoteMediaNode.startPushMedia
     * with successful RPC result
     */
    public interface OnStreamException{
        void onStreamException(String nodeID, CloudMedia.CMStreamException exception);
    }

    /**
     * A PULLER calls it to register a listener to observe exception during streaming
     */
    public void setStreamExceptionListener(OnStreamException listener) {
        String exctopic = Topic.generate("*", whoami(), Topic.Action.STREAM_EXCEPTION);
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
                        str = CloudMedia.CMStreamException.UNKNOWN_ERROR.str();
                    }
                    if (str == null)
                        str = CloudMedia.CMStreamException.UNKNOWN_ERROR.str();
                    CloudMedia.CMStreamException se = CloudMedia.CMStreamException.name(str);

                    if (mStreamExceptionListener != null) {
                        mStreamExceptionListener.onStreamException(mNode.getID(), se);
                    }
                }
            };

            mTopicHandler.install(exctopic, streamExceptionHandler);
        } else if (mStreamExceptionListener != null) {
            mTopicHandler.uninstall(exctopic);
        }

        mStreamExceptionListener = listener;
    }

    @Override
    public boolean connect(final String user, final String passwd, final String groupID, final String groupNick,
                              final String vendorID, final String vendorNick, final CloudMedia.RPCResultListener listener) {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }

}
