package com.cmteam.cloudmedia;

import android.content.Context;
import android.util.Log;

import com.cmteam.p2pmqtt.P2PMqtt;
import com.cmteam.p2pmqtt.MqttTopicHandler;

/**
 * PullNode: represents a stream puller(PULL node), which can get stream media from a stream pusher
 */
public class PullNode extends MediaNode {
    private static final String TAG = "PullNode";
    private OnNodesListChange mNodesListChangeLisener;
 
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
    public boolean connect(final String user, final String passwd, final String groupID, final String groupNick,
                              final String vendorID, final String vendorNick, final CloudMedia.RPCResultListener listener) {
        // add login later

        mNode.setGroupID(groupID==null ? FIELD_GROUPID_DEFAULT : groupID);
        mNode.setGroupNick(groupNick==null ? FIELD_GROUPNICK_DEFAULT : groupNick);
        mVendorID = vendorID;
        mVendorNick = vendorNick;
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
                mTopicHandler.install(Topic.generate(whoareyou(CloudMedia.CMRole.ROLE_PULLER.str(),"*"), whoisMC(),Topic.Action.NODES_CHANGE), nodesChangeHandler);

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