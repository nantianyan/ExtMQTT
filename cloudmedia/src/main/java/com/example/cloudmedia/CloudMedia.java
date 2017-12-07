package com.example.cloudmedia;

import android.content.Context;
import android.util.Log;

import com.example.p2pmqtt.MqttTopicHandler;
import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.P2PMqttAsyncRequest;
import com.example.p2pmqtt.P2PMqttRequest;

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
    private String mRole = CMRole.ROLE_NONE.str();
    public static final String TOPIC_NODES_ONLINE = "/nodes_online/cm"; // role +
    public static final String TOPIC_PUSHER_ONLINE = CMRole.ROLE_PUSHER.str() + TOPIC_NODES_ONLINE;

    private Context mContext;
    private P2PMqtt mExtMqttClient;
    private String mBrokerUrl;
    private  String mMyID;

    private FullActionListener mConnectLisenser;
    private OnNodesStatusChange mNodeStatusLisener;

    public enum CMStatus{
        PUSHING,
        PULLING;
    }

    public enum CMRole{
        ROLE_ALL("all"),
        ROLE_PULLER("puller"),
        ROLE_PUSHER("pusher"),
        ROLE_TEST("tester"),
        ROLE_NONE("none");

        private final String mRoleName;
        CMRole(String roleName) {
            this.mRoleName = roleName;
        }
        public String str() {
            return this.mRoleName;
        }
    }

    public enum CMField{
        ID("whoami"),
        TIME("time"),
        LOCATION("location"),
        NICK("nick"),
        ROLE("role"),
        STATUS("status");

        private final String mFiledName;
        CMField(String filedName) {
            this.mFiledName = filedName;
        }
        public String str(){
            return this.mFiledName;
        }
    }

    /**
     * get uniqure ID from server.
     * NOTE: this function cannot be called from main thread!
     * @return uniqure id managed by a cloud server
     */
    private String getIDFromServer(){
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
            return "CloudMedia_" + System.nanoTime();
        }
    }

    private String getBrokerUrlFromServer(){
        return "tcp://139.224.128.15:1883";
    }

    public boolean sendRequest(String targetID, String method,
                                String params, final CloudMedia.SimpleActionListener listener) {
        Log.d(TAG, "sendRequest to: " + targetID +
                ", calling: " + method +
                ", params: " + params);

        P2PMqttAsyncRequest request = new P2PMqttAsyncRequest();
        request.setWhoareyou(targetID);
        request.setMethodName(method);
        request.setMethodParams(params);
        if(listener == null) {
            request.setListener(P2PMqttRequest.SIMPLE_LISTENER);
        } else {
            request.setListener(new P2PMqtt.IMqttRpcActionListener() {
                @Override
                public P2PMqtt.ResultCode onResult(JSONObject jrpc) {
                    String result = null;
                    try {
                        result = jrpc.getString("result");
                        Log.d(TAG, "jrpc's result is: " + result);

                        if (listener.onResult(result)) {
                            return P2PMqtt.ResultCode.ERROR_None;
                        } else {
                            return P2PMqtt.ResultCode.ERROR_Unknown;
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "illeagle JSON!");
                        e.printStackTrace();
                    }

                    return P2PMqtt.ResultCode.ERROR_None;
                }
            });
        }

        return mExtMqttClient.sendRequest(request);
    }

    public boolean putOnline(String nickName, CMRole role, final CloudMedia.SimpleActionListener listener) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());
            // String time = curDate.toString();
            String strTime = formatter.format(curDate);

            String params = "";
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.ID.str(), mMyID);
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.TIME.str(), strTime);
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.LOCATION.str(), "none");
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.NICK.str(), nickName);
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.ROLE.str(), role.str());
            params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.STATUS.str(), "online");
            params = P2PMqtt.MyJsonString.addJsonBrace(params);

            return sendRequest("controller", "online", params, listener);
    }

    public boolean putOffline(String nickName, CMRole role, final CloudMedia.SimpleActionListener listener) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        // String time = curDate.toString();
        String strTime = formatter.format(curDate);

        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.ID.str(), mMyID);
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.TIME.str(), strTime);
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.LOCATION.str(), "none");
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.NICK.str(), nickName);
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.ROLE.str(), role.str());
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, CMField.STATUS.str(), "offline");
        params = P2PMqtt.MyJsonString.addJsonBrace(params);

        return sendRequest("controller", "offline", params, listener);
    }

    public boolean updateMyStatus(CMStatus status, final SimpleActionListener listener) {
        updateCMField(CMField.STATUS.str(), status.toString().toLowerCase(), listener);
        return true;
    }

    private boolean updateCMField(String filed, String newValue, final SimpleActionListener listener) {
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "whoami", mMyID);
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "field", filed);
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "value", newValue);
        params = P2PMqtt.MyJsonString.addJsonBrace(params);
        return sendRequest("controller", "nodes_update", params, listener);
    }

    public boolean findNodeInfo(String nodeID, final CloudMedia.SimpleActionListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "whoami", nodeID);
        params = P2PMqtt.MyJsonString.addJsonBrace(params);
        return findCMNodes(params, listener);
    }

    public boolean findRolesOnline(CMRole role, final CloudMedia.SimpleActionListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "role", role.str());
        params = P2PMqtt.MyJsonString.addJsonBrace(params);
        return findCMNodes(params, listener);
    }

    private boolean findCMNodes(String params, final CloudMedia.SimpleActionListener listener) {
        return sendRequest("controller", "nodes_find", params, listener);
    }

    public boolean sendText(String mWhoareyou, String text) {
        String topic = mWhoareyou + "/" + mMyID + "/text";
        mExtMqttClient.MqttPublish(topic, text, 1, false);
        return true;
    }

    public CloudMedia(Context context) {
        mContext = context;
    }

    public boolean connect(final String nick, final CMRole role, final FullActionListener listener) {
        mBrokerUrl = getBrokerUrlFromServer();
        mMyID = getIDFromServer();
        mExtMqttClient = new P2PMqtt(mContext, mMyID, "12345");

        setNodesStatusChangeListener(mNodeStatusLisener);

        mExtMqttClient.connect(mBrokerUrl, new P2PMqtt.IFullActionListener() {
            @Override
            public void onSuccess(String params) {

                putOnline(nick, role, new SimpleActionListener() {
                    @Override
                    public boolean onResult(String result) {
                        listener.onSuccess("OK");
                        return true;
                    }
                });
            }

            @Override
            public void onFailure(String params) {
                listener.onFailure("ERROR");
            }
        });

        return true;
    }

    public boolean disconnect() {
        // to close all local media nodes ?

        mExtMqttClient.disconnect();
        return  true;
    }

    public RemoteMediaNode declareRemoteMediaNode(String whoareyou){
        return RemoteMediaNode.create(this, whoareyou);
    }

    public LocalMediaNode declareLocalMediaNode() {
        return new LocalMediaNode(mExtMqttClient);
    }

    // not implemented yet.
    // in our original design, LiveServer interface should exposed from MQTT media controller
    // yet another way is tack to cloud live media server directly.
    // further more, client may have no idea about his interface?
    LiveServerNode declareLiveServer(){
        return new LiveServerNode();
    }

    public interface SimpleActionListener {
        boolean onResult(String result);
    }

    public interface FullActionListener {
        public void onSuccess(String params);
        public void onFailure(String params);
    }

    /**
     * Interface definition for listener of nodes status changes
     * such as online/offline
     */
    public interface OnNodesStatusChange{
        boolean OnNodesStatusChange(NodesList nodesList);
    }

    public final class NodesList{
        public final class Node{
            private String mWhoami;
            private String mNick;
            private String mLocation;
            private String mLastUpdateTime;

            Node(JSONObject jnode){
                try {
                    mWhoami = jnode.getString("whoami");
                    mNick = jnode.getString("nick");
                    mLocation = jnode.getString("location");
                    mLastUpdateTime = jnode.getString("time");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            void log(){
                Log.d(TAG, "whoami: " + mWhoami);
                Log.d(TAG, "nick: " + mNick);
                Log.d(TAG, "location: " + mLocation);
                Log.d(TAG, "time: " + mLastUpdateTime);
            }
        }

        private List<Node> mNodesList = new ArrayList<Node>();
        public List<String> mNodesID = new ArrayList<String>();
        public List<String> mNodesNick = new ArrayList<String>();

        public NodesList(String jsonStr) {
            try {
                JSONArray jsonNodes = new JSONArray(jsonStr);
                for (int i = 0; i < jsonNodes.length(); i++) {
                    JSONObject jnode = jsonNodes.getJSONObject(i);
                    Node node = new Node(jnode);
                    node.log();
                    mNodesList.add(node);
                    mNodesID.add(node.mWhoami);
                    mNodesNick.add(node.mNick);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public int size() {
            return mNodesList.size();
        }

        public void clear(){
            mNodesID.clear();
            mNodesNick.clear();
            mNodesList.clear();
        }

        public List<Node> get(){
            return mNodesList;
        }
        public void log(){
            for (Node e:mNodesList) {
                e.log();
            }
        }
    }

    public void setNodesStatusChangeListener(final OnNodesStatusChange listener) {
        if(listener == null){
            return;
        }

        if(mExtMqttClient == null) {
            mNodeStatusLisener = listener;
        } else {
            mExtMqttClient.installTopicHandler(TOPIC_PUSHER_ONLINE, new MqttTopicHandler() {
                @Override
                public void onMqttMessage(String jstr) {
                    mNodeStatusLisener.OnNodesStatusChange(new NodesList(jstr));
                }
            });
        }
    }

    public interface OnTextMessage{
        boolean OnTextMessage(String text);
    }

    public void setTextMessageListener(final OnTextMessage listener) {
        mExtMqttClient.installTopicHandler(mMyID + "/+/text", new MqttTopicHandler() {
            @Override
            public void onMqttMessage(String text) {
                listener.OnTextMessage(text);
            }
        });
    }
}
