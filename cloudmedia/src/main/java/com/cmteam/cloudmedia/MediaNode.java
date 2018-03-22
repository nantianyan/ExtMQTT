package com.cmteam.cloudmedia;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cmteam.p2pmqtt.MqttTopicHandler;
import com.cmteam.p2pmqtt.P2PMqtt;
import com.cmteam.p2pmqtt.P2PMqttAsyncRequest;
import com.cmteam.p2pmqtt.P2PMqttRequest;
import com.cmteam.p2pmqtt.P2PMqttRequestHandler;

public abstract class MediaNode {
    private static final String TAG = "MediaNode";

    protected static final String FIELD_GROUPID_DEFAULT = "G00000000";
    protected static final String FIELD_GROUPNICK_DEFAULT = "Default Group";
    protected static final String FIELD_VENDORID_DEFAULT = "V00000000";
    protected static final String FIELD_VENDORNICK_DEFAULT = "CM Team";
    protected static final String FIELD_LOCATION_DEFAULT = "Location Unknown";
    protected static final String INVALID_NODE = "invalid_node";
    protected static final String RPCSuccess = "OK";
    protected static final String RPCFailure = "ERROR";

    protected Context mContext;
    protected P2PMqtt mExtMqttClient;
    protected TopicHandler mTopicHandler;
    protected Node mNode;

    /**
     * A node calls it to connect to MCS before doing any media transaction
     */
    public abstract boolean connect(final CloudMedia.CMUser user, final CloudMedia.RPCResultListener listener);

    /**
     * A node calls it to disconnect from MCS when it doesn't do media transaction any more
     */
    public abstract boolean disconnect();

    /**
     * A node calls it to notify a new stream status to MCS,
     * this method must be called whenever the node's stream status change
     */
    public boolean updateStreamStatus(CloudMedia.CMStreamStatus status, final CloudMedia.RPCResultListener listener) {
        return updateCMField(CloudMedia.CMField.STREAM_STATUS, status.str(), listener);
    }

    /**
     * A node calls it to exchange data with a peer node,
     * the application can define the data format within the message,
     * the parameter groupID and nodeID show where the message is sent to
     */
    public boolean sendMessage(String peerGroupID,String peerNodeID, String message) {
        Log.d(TAG, "sendMessage: groupID=" + peerGroupID + ",nodeID=" + peerNodeID + ",message=" + message);
        String topic = Topic.generate(whoareyou(peerGroupID, peerNodeID),whoami(),Topic.Action.EXCHANGE_MSG);
        mExtMqttClient.MqttPublish(topic, message, 2, false);
        return true;
    }

    /**
     * Set a message listener used to receive data from a peer node
     */
    public void setMessageListener(final CloudMedia.OnMessageListener listener) {
        mTopicHandler.install(Topic.generate(whoami(),"+",Topic.Action.EXCHANGE_MSG), new MqttTopicHandler() {
            @Override
            public void onMqttMessage(String topic, String message) {
                String fromWho = Topic.getFromWho(topic);
                String[] arrays = fromWho.split("_");
                String groupID = null;
                String nodeID = null;
                if (arrays.length == 3) {
                    groupID = arrays[1];
                    nodeID = arrays[2];
                }
                Log.d(TAG, "onMessage: groupID=" + groupID + ",nodeID=" + nodeID + ",message=" + message);
                listener.onMessage(groupID, nodeID, message);
            }
        });
    }

    protected static String getBrokerUrl() {
        return "tcp://139.224.128.15:1883";
    }

    protected boolean sendRequest(String whoareyou, String method, String params, final CloudMedia.RPCResultListener listener) {
        Log.d(TAG, "sendRequest to: " + whoareyou +
                ", calling: " + method +
                ", params: " + params);

        P2PMqttAsyncRequest request = new P2PMqttAsyncRequest();
        request.setWhoareyou(whoareyou);
        request.setMethodName(method);
        request.setMethodParams(params);
        if(listener == null) {
            request.setListener(P2PMqttRequest.SIMPLE_LISTENER);
        } else {
            request.setListener(new P2PMqtt.IMqttRpcActionListener() {
                @Override
                public P2PMqtt.ResultCode onResult(JSONObject jrpc) {
                    try {
                        Log.d(TAG, "jrpc: " + jrpc.toString());
                        // normal
                        if (jrpc.has("result")) {
                            listener.onSuccess(jrpc.getString("result"));
                            return P2PMqtt.ResultCode.ERROR_None;
                        } else if (jrpc.has("error")){
                            listener.onFailure(jrpc.getString("error"));
                            return P2PMqtt.ResultCode.ERROR_None;
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "illeagle JSON!");
                        e.printStackTrace();
                    }

                    return P2PMqtt.ResultCode.ERROR_BadData;
                }
            });
        }

        return mExtMqttClient.sendRequest(request);
    }

    protected void handleRequest(String method, P2PMqttRequestHandler handler) {
        mExtMqttClient.installRequestHandler(method, handler);
    }

    protected boolean putOnline(final CloudMedia.RPCResultListener listener) {
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CloudMedia.CMField.ID.str(), mNode.getID());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CloudMedia.CMField.NICK.str(), mNode.getNick());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CloudMedia.CMField.ROLE.str(), mNode.getRole());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CloudMedia.CMField.DEVICE_NAME.str(), mNode.getDeviceName());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CloudMedia.CMField.LOCATION.str(), mNode.getLocation());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CloudMedia.CMField.STREAM_STATUS.str(), mNode.getStreamStatus());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CloudMedia.CMField.VENDOR_ID.str(), mNode.getVendorID());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CloudMedia.CMField.VENDOR_NICK.str(), mNode.getVendorNick());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CloudMedia.CMField.GROUP_ID.str(), mNode.getGroupID());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CloudMedia.CMField.GROUP_NICK.str(), mNode.getGroupNick());

        params = P2PMqtt.MyJsonString.addJsonBrace(params);

        return sendRequest(whoisMC(), RPCMethod.ONLINE, params, listener);
    }

    protected boolean putOffline(final CloudMedia.RPCResultListener listener) {
        return sendRequest(whoisMC(), RPCMethod.OFFLINE, null, listener);
    }

    protected String whoisMC() {
        return CloudMedia.CMRole.ROLE_MC.str();
    }

    protected String whoami() {
        if (mNode == null)
            return INVALID_NODE;
        return mNode.whoami();
    }

    protected String whoareyou(String groupID, String nodeID) {
        return mNode.getVendorID() + "_" + groupID + "_" + nodeID;
    }

    protected boolean updateCMField(CloudMedia.CMField filed, String newValue, final CloudMedia.RPCResultListener listener) {
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "field", filed.str());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "value", newValue);
        params = P2PMqtt.MyJsonString.addJsonBrace(params);
        return sendRequest(whoisMC(), RPCMethod.UPDATE_FIELD, params, listener);
    }

}
