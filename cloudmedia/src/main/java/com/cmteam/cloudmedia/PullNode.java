package com.cmteam.cloudmedia;

import android.content.Context;
import android.util.Log;

import com.cmteam.p2pmqtt.P2PMqtt;
import com.cmteam.p2pmqtt.MqttTopicHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * PullNode: represents a stream puller(PULL node), which can get stream media from a stream pusher
 */
public class PullNode extends MediaNode {
    private static final String TAG = "PullNode";
    private OnNodesListChange mNodesListChangeLisener;
    private OnStreamException mStreamExceptionListener;
 
    public PullNode(Context context, String id, String nick, String deviceName) {
	    mContext = context;
        mNode = new Node();
        mNode.setRole(CloudMedia.CMRole.ROLE_PULLER.str());
        mNode.setID(id);
        mNode.setNick(nick);
        mNode.setDeviceName(deviceName);
        mNode.setLocation(FIELD_LOCATION_DEFAULT);
        mNode.setStreamStatus(CloudMedia.CMStreamStatus.PULLING_CLOSE);
    }

    @Override
    public boolean connect(final CloudMedia.CMUser user, final CloudMedia.RPCResultListener listener) {
        if (!CloudMedia.CMRole.ROLE_PULLER.str().equals(user.role)) {
            Log.e(TAG, "account role not match");
            return false;
        }
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
                // PULLER must observe remote nodes change
                MqttTopicHandler nodesChangeHandler = new MqttTopicHandler() {
                    @Override
                    public void onMqttMessage(String topic, String jstr) {
                        if (mNodesListChangeLisener != null)
                            mNodesListChangeLisener.onNodesListChange(new NodesList(jstr));
                    }
                };
                mTopicHandler.install(Topic.generate(whoami(),whoisMC(),Topic.Action.NODES_CHANGE), nodesChangeHandler);
                mTopicHandler.install(Topic.generate(whoareyou(mNode.getGroupID(),"*"), whoisMC(),Topic.Action.NODES_CHANGE), nodesChangeHandler);
                mTopicHandler.install(Topic.generate(whoareyou("*","*"), whoisMC(),Topic.Action.NODES_CHANGE), nodesChangeHandler);

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


        return connectResult;
    }

    @Override
    public boolean disconnect() {
        putOffline(null);
        mTopicHandler.uninstall(Topic.generate(whoami(),whoisMC(),Topic.Action.NODES_CHANGE));
        mTopicHandler.uninstall(Topic.generate(whoareyou(CloudMedia.CMRole.ROLE_PULLER.str(),"*"), whoisMC(),Topic.Action.NODES_CHANGE));
        mExtMqttClient.disconnect();
        return  true;
    }

    /**
     * A PULLER calls it to request remote node to push stream
     */
    public boolean startPushMedia(Node pushNode, final CloudMedia.RPCResultListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "target-id", pushNode.whoami());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "expire-time", "100s");
        params = P2PMqtt.MyJsonString.addJsonBrace(params);

        return sendRequest(whoisMC(), RPCMethod.START_PUSH_MEDIA, params, listener);
    }

    /**
     * A PULLER calls it to request remote node to stop pushing stream
     */
    public boolean stopPushMedia(Node pushNode, final CloudMedia.RPCResultListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "target-id", pushNode.whoami());
        params = P2PMqtt.MyJsonString.addJsonBrace(params);

        return sendRequest(whoisMC(), RPCMethod.STOP_PUSH_MEDIA, params, listener);
    }

    /**
     * A listener interface used to observe stream exception event from a PUSH node,
     * a PULL node must register this listener after it calls RemoteMediaNode.startPushMedia
     * with successful RPC result
     */
    public interface OnStreamException{
        void onStreamException(String whoareyou, CloudMedia.CMStreamException exception);
    }

    /**
     * A PULLER calls it to register a listener to observe exception during streaming
     */
    public void setStreamExceptionListener(Node pushNode, OnStreamException listener) {
        String exctopic = Topic.generate("*", pushNode.whoami(), Topic.Action.STREAM_EXCEPTION);
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
                        mStreamExceptionListener.onStreamException(Topic.getFromWho(topic), se);
                    }
                }
            };

            mTopicHandler.install(exctopic, streamExceptionHandler);
        } else if (mStreamExceptionListener != null) {
            mTopicHandler.uninstall(exctopic);
        }

        mStreamExceptionListener = listener;
    }

    /**
     * A listener interface used to return the changed nodes list
     */
    public interface OnNodesListChange {
        void onNodesListChange(NodesList nodesList);
    }

    /**
     * Register a listener to observe remote nodes list change, generally a PULL node must
     * call it to be notified timely when PUSH nodes have some change
     */
    public void setNodesListChangeListener(final OnNodesListChange listener) {
        mNodesListChangeLisener = listener;
    }

}