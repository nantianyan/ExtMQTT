package com.example.cloudmedia;

import android.content.Context;
import android.util.Log;

import com.example.p2pmqtt.MqttTopicHandler;
import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.P2PMqttAsyncRequest;
import com.example.p2pmqtt.P2PMqttRequest;
import com.example.p2pmqtt.P2PMqttRequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by 阳旭东 on 2017/10/18.
 */

public class CloudMedia {
    private static final String TAG = "CloudMedia";
    private static final String FIELD_UNKNOWN = "unknown";
    private static final String FIELD_GROUPID_DEFAULT = "00000000";
    private static final String FIELD_GROUPNICK_DEFAULT = "Default Group";
    private static final String FIELD_VENDORID_DEFAULT = "00000000";
    private static final String FIELD_VENDORNICK_DEFAULT = "CM Team";

    private Context mContext;
    private P2PMqtt mExtMqttClient;
    private String mBrokerUrl;
    private Node mMyNode;
    private String mMyVendorID;
    private String mMyVendorNick;

    private OnNodesListChange mNodesListChangeLisener;

    public static final String RPCSuccess = "OK";
    public static final String RPCFailure = "ERROR";

    /**
     * Indicates the streaming status of a client node, the node must update this status
     * to MCS(Media Control Server) when the status changes by calling 
     * updateStreamStatus(CMStreamStatus status, final RPCResultListener listener)
     */
    public enum CMStreamStatus{
        PUSHING("pushing"),
        PUSHING_CLOSE("pushing_close"),
        PULLING("pulling"),
        PULLING_CLOSE("pulling_close"),
        UNKNOWN(FIELD_UNKNOWN);

        private final String mStr;
        CMStreamStatus(String str){
            mStr = str;
        }
        public String str(){
            return mStr;
        }
    }

    /**
     * Indicates the roles of the client nodes on a media transaction
     */
    public enum CMRole{
        ROLE_ALL("all"),
        ROLE_PULLER("puller"),
        ROLE_PUSHER("pusher"),
        ROLE_MC("media_controller"),
        ROLE_TEST("tester"),
        UNKNOWN(FIELD_UNKNOWN);

        private final String mRoleName;
        CMRole(String roleName) {
            this.mRoleName = roleName;
        }
        public String str() {
            return this.mRoleName;
        }
    }

    /**
     * Indicates the basic properties of a client node
     */
    public enum CMField{
        ID("id"),
        NICK("nick"),
        ROLE("role"),
        LOCATION("location"),
        STREAM_STATUS("stream_status"),
        VENDOR_ID("vendor_id"),
        VENDOR_NICK("vendor_nick"),
        GROUP_ID("group_id"),
        GROUP_NICK("group_nick");

        private final String mFiledName;
        CMField(String filedName) {
            this.mFiledName = filedName;
        }
        public String str(){
            return this.mFiledName;
        }
    }

    /**
     * A CloudMedia object for all nodes used to reach cloud media transaction
     */
    public CloudMedia(Context context) {
        mContext = context;
        mMyVendorID = FIELD_VENDORID_DEFAULT;
        mMyVendorNick = FIELD_VENDORNICK_DEFAULT;
    }

    /**
     * Indicates a remote client node as a proxy used to send all media requests to this node
     */
    public RemoteMediaNode declareRemoteMediaNode(Node remoteNode){
        return RemoteMediaNode.create(this, whoareyou(remoteNode.getGroupID(), remoteNode.getID()));
    }

    /**
     * Indicates a local client node as an actor to respond all media requests from a remote node
     */
    public LocalMediaNode declareLocalMediaNode() {
        return new LocalMediaNode(this);
    }

    /**
     * A node calls it to connect to MCS before doing any media transaction
     */
    public boolean connect(final String nick, final CMRole role, final RPCResultListener listener) {
        mBrokerUrl = getBrokerUrlFromServer();
        String myID = getIDFromServer();
        mMyNode = new Node(myID, nick, role, FIELD_GROUPID_DEFAULT, FIELD_GROUPNICK_DEFAULT);
        mExtMqttClient = new P2PMqtt(mContext, whoami(), "12345");

        mExtMqttClient.connect(mBrokerUrl, new P2PMqtt.IFullActionListener() {
            @Override
            public void onSuccess(String params) {
                // PULLER must observe remote nodes change
                if (mMyNode.getRole().equals(CMRole.ROLE_PULLER.str())) {
                    MqttTopicHandler nodesChangeHandler = new MqttTopicHandler() {
                        @Override
                        public void onMqttMessage(String jstr) {
                            if (mNodesListChangeLisener != null)
                                mNodesListChangeLisener.OnNodesListChange(new NodesList(jstr));
                        }
                    };
                    mExtMqttClient.installTopicHandler(Topic.generate(whoami(),whoisMC(),Topic.Action.NODES_CHANGE), nodesChangeHandler);
                    mExtMqttClient.installTopicHandler(Topic.generate(whoareyou(CMRole.ROLE_PULLER.str(),"*"), whoisMC(),Topic.Action.NODES_CHANGE), nodesChangeHandler);
                }

                putOnline(new RPCResultListener() {
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


        return true;
    }

    /**
     * A node calls it to disconnect from MCS when it doesn't do media transaction any more
     */
    public boolean disconnect() {
        putOffline(null);
        mExtMqttClient.disconnect();
        return  true;
    }

    /**
     * A node calls it to notify a new stream status to MCS,
     * this method must be called whenever the node's stream status change
     */
    public boolean updateStreamStatus(CMStreamStatus status, final RPCResultListener listener) {
        return updateCMField(CMField.STREAM_STATUS, status.str(), listener);
    }

    /**
     * Register a listener to observe remote nodes list change, generally a PULL node must
     * call it to be notified timely when PUSH nodes have some change
     */
    public void setNodesListChangeListener(final OnNodesListChange listener) {
        mNodesListChangeLisener = listener;
    }

    /**
     * A common listener interface used to return a RPC result
     */
    public interface RPCResultListener {
        public void onSuccess(String params);
        public void onFailure(String params);
    }

    /**
     * A listener interface used to return the changed nodes list
     */
    public interface OnNodesListChange{
        boolean OnNodesListChange(NodesList nodesList);
    }

    static private String getIDFromServer(){
        if(false) {
            URL url = null;
            HttpURLConnection conn = null;
            try {
                url = new URL("http://139.224.128.15:8085/getID");
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                //conn.setRequestProperty("action", "getID");
                conn.setUseCaches(false);
                conn.setReadTimeout(8000);
                conn.setConnectTimeout(8000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader bufReader = new BufferedReader(new InputStreamReader(in));

                    StringBuilder response = new StringBuilder();
                    String line = null;
                    while ((line = bufReader.readLine()) != null) {
                        response.append(line);
                    }
                    Log.i(TAG, "get id from server: " + response.toString());

                    return response.toString();
                } else {
                    Log.i(TAG, "http response error");
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return null;
        } else {
            return "N" + System.nanoTime();
        }
    }

    static private String getBrokerUrlFromServer(){
        return "tcp://139.224.128.15:1883";
    }

    public boolean sendRequest(String whoareyou, String method,
                                String params, final RPCResultListener listener) {
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

    public void handleRequest(String method, P2PMqttRequestHandler handler) {
        mExtMqttClient.installRequestHandler(method, handler);
    }

    public String whoisMC() {
        return CMRole.ROLE_MC.str();
    }

    private String whoami() {
        return mMyVendorID + "_" + mMyNode.getGroupID() + "_" + mMyNode.getID();
    }

    private String whoareyou(String groupID, String nodeID) {
        return mMyVendorID + "_" + groupID + "_" + nodeID;
    }

    private boolean putOnline(final RPCResultListener listener) {
            String params = "";
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.ID.str(), mMyNode.getID());
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.NICK.str(), mMyNode.getNick());
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.ROLE.str(), mMyNode.getRole());
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.LOCATION.str(), mMyNode.getLocation());
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.STREAM_STATUS.str(), mMyNode.getStreamStatus());
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.VENDOR_ID.str(), mMyVendorID);
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.VENDOR_NICK.str(), mMyVendorNick);
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.GROUP_ID.str(), mMyNode.getGroupID());
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.GROUP_NICK.str(), mMyNode.getGroupNick());

            params = P2PMqtt.MyJsonString.addJsonBrace(params);

            return sendRequest(whoisMC(), RPCMethod.ONLINE, params, listener);
    }

    private boolean putOffline(final RPCResultListener listener) {
        return sendRequest(whoisMC(), RPCMethod.OFFLINE, null, listener);
    }

    private boolean updateCMField(CMField filed, String newValue, final RPCResultListener listener) {
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "field", filed.str());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "value", newValue);
        params = P2PMqtt.MyJsonString.addJsonBrace(params);
        return sendRequest(whoisMC(), RPCMethod.UPDATE_FIELD, params, listener);
    }

    /**
     * A client node definition, for both the PUSH node and PULL node
     */
    public final class Node {
        private String mID;
        private String mNick;
        private String mRole;
        private String mLocation;
        private String mStreamStatus;
        private String mGroupID;
        private String mGroupNick;

        Node(JSONObject jnode){
            try {
                mID= jnode.getString(CMField.ID.str());
                mNick = jnode.getString(CMField.NICK.str());
                mRole = jnode.getString(CMField.ROLE.str());
                mLocation = jnode.getString(CMField.LOCATION.str());
                mStreamStatus = jnode.getString(CMField.STREAM_STATUS.str());
                mGroupID = jnode.getString(CMField.GROUP_ID.str());
                mGroupNick = jnode.getString(CMField.GROUP_NICK.str());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Node(String id, String nick, CMRole role, String groupID, String groupNick) {
            mID = id;
            mNick = nick;
            mRole = role.str();
            mLocation = FIELD_UNKNOWN;
            if (mRole.equals(CMRole.ROLE_PUSHER.str()))
                mStreamStatus = CMStreamStatus.PUSHING_CLOSE.str();
            else if (mRole.equals(CMRole.ROLE_PULLER.str()))
                mStreamStatus = CMStreamStatus.PULLING_CLOSE.str();
            else
                mStreamStatus = CMStreamStatus.UNKNOWN.str();
            mGroupID = groupID;
            mGroupNick = groupNick;
        }

        void print(){
            Log.d(TAG, "id: " + mID);
            Log.d(TAG, "nick: " + mNick);
            Log.d(TAG, "role: " + mRole);
            Log.d(TAG, "location: " + mLocation);
            Log.d(TAG, "stream_status: " + mStreamStatus);
            Log.d(TAG, "group_id: " + mGroupID);
            Log.d(TAG, "group_nick: " + mGroupNick);
        }

        public String getID(){
            return mID;
        }

        public String getNick(){
            return mNick;
        }

        public String getRole(){
            return mRole;
        }

        public String getLocation(){
            return mLocation;
        }

        public String getStreamStatus(){
            return mStreamStatus;
        }

        public String getGroupID(){
            return mGroupID;
        }

        public String getGroupNick(){
            return mGroupNick;
        }

        public String setLocation(String location){
            return mLocation = location;
        }

        public String setStreamStatus(CMStreamStatus status){
            return mStreamStatus = status.str();
        }

        public String setGroupID(String id){
            return mGroupID = id;
        }

        public String setGroupNick(String nick){
            return mGroupNick = nick;
        }

    }

    /**
     * A whole node list definition which contains changed nodes list
     * of all online, new online, new offline and new update. A node can
     * observe and sniff remote nodes change from NodeList if it registered
     * a OnNodesListChange listener
     */
    public final class NodesList {
        private static final String CHANGE_ALL_ONLINE = "all_online";
        private static final String CHANGE_NEW_ONLINE = "new_online";
        private static final String CHANGE_NEW_OFFLINE = "new_offline";
        private static final String CHANGE_NEW_UPDATE = "new_update";

        private List<Node> mAllOnlineList = new ArrayList<Node>();
        private List<Node> mNewOnlineList = new ArrayList<Node>();
        private List<Node> mNewOfflineList = new ArrayList<Node>();
        private List<Node> mNewUpdateList = new ArrayList<Node>();

        public NodesList(String jsonStr) {
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                if (jsonObj.has(CHANGE_ALL_ONLINE)) {
                    jsonArrayToList(jsonObj.getJSONArray(CHANGE_ALL_ONLINE), mAllOnlineList);
                } else {
                    if (jsonObj.has(CHANGE_NEW_ONLINE)) {
                        jsonArrayToList(jsonObj.getJSONArray(CHANGE_NEW_ONLINE), mNewOnlineList);
                    }
                    if (jsonObj.has(CHANGE_NEW_OFFLINE)) {
                        jsonArrayToList(jsonObj.getJSONArray(CHANGE_NEW_OFFLINE), mNewOfflineList);
                    }
                    if (jsonObj.has(CHANGE_NEW_UPDATE)) {
                        jsonArrayToList(jsonObj.getJSONArray(CHANGE_NEW_UPDATE), mNewUpdateList);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public List<Node> getAllOnlineList() {
            return mAllOnlineList;
        }

        public List<Node> getNewOnlineList() {
            return mNewOnlineList;
        }

        public List<Node> getNewOfflineList() {
            return mNewOfflineList;
        }

        public List<Node> getNewUpdateList() {
            return mNewUpdateList;
        }

        public void clear(){
            mAllOnlineList.clear();
            mNewOnlineList.clear();
            mNewOfflineList.clear();
            mNewUpdateList.clear();
        }

        private void jsonArrayToList(JSONArray jarray, List<Node> nodeList) {
            try {
                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject jnode = jarray.getJSONObject(i);
                    Node node = new Node(jnode);
                    nodeList.add(node);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public interface OnTextMessage{
        boolean OnTextMessage(String text);
    }

    public boolean sendText(String groupID,String nodeID, String text) {
        String topic = Topic.generate(whoareyou(groupID, nodeID),whoami(),Topic.Action.EXCHANGE_MSG);
        mExtMqttClient.MqttPublish(topic, text, 1, false);
        return true;
    }

    public void setTextMessageListener(final OnTextMessage listener) {
        mExtMqttClient.installTopicHandler(Topic.generate(whoami(),"+",Topic.Action.EXCHANGE_MSG), new MqttTopicHandler() {
            @Override
            public void onMqttMessage(String text) {
                listener.OnTextMessage(text);
            }
        });
    }

}
