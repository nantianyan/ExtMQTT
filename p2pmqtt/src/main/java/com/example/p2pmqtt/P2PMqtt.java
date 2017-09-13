package com.example.p2pmqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;

/**
 * Created by 阳旭东 on 2017/9/7.
 */

public class P2PMqtt {
    public interface IMqttRpcActionListener {
        public void onResult(JSONObject jrpc);
    }

    public static final String TAG = "P2PMqtt";
    private Context mContext;
    private static MqttAndroidClient mClient;
    private MqttCallback mMqttCallback = new MqttCallback() {
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "=> messageArrived:");
            Log.i(TAG, "\t topic:" + topic);
            Log.i(TAG, "\t message:" + message.toString());
            //Log.i(TAG, "\t payload:" + message.getPayload().toString()); // spurious
            Log.i(TAG, "\t qos:" + message.getQos());
            Log.i(TAG, "\t retained:" + message.isRetained());
            onMqttMessage(topic, message);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            Log.i(TAG, "deliveryComplete");
        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.i(TAG, "connectionLost");
        }
    };

    private MqttConnectOptions mConnectOptions;
    private String mHost;
    private String mUserName = "admin";
    private String mPassWord = "password";
    private String mClientId;

    private HashMap<String, P2PMqttRequestHandler> mRequestHandler = new HashMap<String, P2PMqttRequestHandler>();
    private HashMap<String, IMqttRpcActionListener> mActionListener = new HashMap<String, IMqttRpcActionListener>();

    private String mWhoami;
    private boolean mIsConnected = false;

    public P2PMqtt(Context context) {
        //TODO:  maybe fetch whoami from a server.
        this(context, "tester");
    }

    public P2PMqtt(Context context, String whoami) {
        mContext = context;
        mWhoami = whoami;
    }

    //TODO: register should be extended by subclass
    public void register(/*String params*/) {
        String params =
                "{\"plate\": \"A0001\", \"longi\":46.23, \"lati\":46.67, \"time\": 123456}";
        sendRequest("controller", "register", params);
    }

    public boolean connect(String host) {
        mHost = host;
        mClientId = MqttClient.generateClientId();
        mClient = new MqttAndroidClient(mContext, mHost, mClientId);
        mClient.setCallback(mMqttCallback);
        if (!mClient.isConnected()) {
            Log.d(TAG, "not connected");
            try {
                Log.d(TAG, "try to connect");
                mConnectOptions = new MqttConnectOptions();
                mConnectOptions.setCleanSession(true);
                mConnectOptions.setConnectionTimeout(10); // 10s
                mConnectOptions.setKeepAliveInterval(20);
                //mConnectOptions.setUserName(userName);
                //mConnectOptions.setPassword(passWord.toCharArray());
                //mConnectOptions.setAutomaticReconnect(true);
                mClient.connect(mConnectOptions, this, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken arg0) {
                        Log.i(TAG, "connect success ");
                        mIsConnected = true;

                        String topic = mWhoami + "/+/request";
                        MqttSubscribe(topic, 2);

                        topic = mWhoami + "/+/reply";
                        MqttSubscribe(topic, 2);

                        //TODO: if this device already registed, then
                        putOnline();
                    }

                    @Override
                    public void onFailure(IMqttToken arg0, Throwable arg1) {
                        Log.i(TAG, "connect fail ");
                        arg1.printStackTrace();
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
                // return false ??
            }
        }

        return true;
    }

    public boolean isConnected() {
        if (mClient == null)
            return false;
        return mClient.isConnected();
    }

    public void sendRequest(String whoareyou, String methodName, String methodParam){
        sendRequest(whoareyou, methodName, methodParam, null);
    }

    public void sendRequest(String whoareyou, String methodName, String methodParam, IMqttRpcActionListener listener){
        Log.d(TAG, "sendRequest");
        Log.d(TAG, "to: " + whoareyou + ", method: " + methodName + ", params: " + methodParam);
        if (mClient.isConnected()) {
            String id = Long.toString(System.nanoTime());
            if(listener != null) {
                mActionListener.put(id, listener);
            }

            if(!methodParam.startsWith("{")) {
                methodParam = "\"" + methodParam + "\"";
            }

            String payload = "{";
            payload = payload + "\"jsonrpc\":\"2.0\",";
            payload = payload + "\"method\":\"" + methodName + "\",";
            payload = payload + "\"params\":" + methodParam + ",";
            payload = payload + "\"id\":" + id;
            payload = payload + "}";

            String topic = whoareyou + "/" + mWhoami + "/request";

            MqttPublish(topic, payload, 2, false);
        } else {
            Log.e(TAG, "client is not connected, so request:" + methodName + " is failed to send!");

        }
    }

    public void sendReply(String topic, JSONObject jrpc, String result) {
        int id = 0;
        try {
            id = jrpc.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String replyTopic = topic.split("/")[1] + "/" + topic.split("/")[0] + "/reply";

        String payload = "{";
        payload = payload + "\"jsonrpc\":\"2.0\",";
        payload = payload + "\"result\":" + result + ",";
        payload = payload + "\"id\":" + id;
        payload = payload + "}";

        MqttPublish(replyTopic, payload, 2, false);
    }

    public void installRequestHandler(String method, P2PMqttRequestHandler handler) {
        mRequestHandler.put(method, handler);
    }

    private void putOnline(/*String params*/) {
        SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyy年MM月dd日   HH:mm:ss");
        Date curDate =  new Date(System.currentTimeMillis());
        // String time = curDate.toString();
        String strTime = formatter.format(curDate);
        String params = "{\"whoami\":\"" + mWhoami + "\"," +
                "\"time\":\"" + strTime + "\"," +
                "\"location\":\"longi lati\"}";

        sendRequest("controller", "online", params);
    }

    private void onMqttMessage(String topic, MqttMessage message) {
        if(topic.contains("/request")){
            // TODO: validate the json object
            try {
                JSONObject jrpc = new JSONObject(message.toString());
                if(jrpc.has("method")) {
                    String method = jrpc.getString("method");
                    Log.d(TAG, "request method:" + method);
                    if (mRequestHandler.containsKey(method)) {
                        mRequestHandler.get(method).onMqttMessage(this, topic, message);
                    } else {
                        Log.e(TAG, "no handler for method:" + method + "!!!");
                    }
                } else {
                    Log.e(TAG, "request hasn't method at all !!!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(topic.contains("/reply")) {
            try {
                // BACK: message.getPayload wont work, spurious
                // Log.i(TAG, "\t " + message.getPayload().toString());
                JSONObject jrpc = new JSONObject(message.toString());
                String id = jrpc.getString("id");
                Log.d(TAG, "  reply result: " + jrpc.getString("result"));
                Log.d(TAG, "  reply id: " + id);
                String strID = id.toString();
                if(mActionListener.containsKey(strID)){
                    mActionListener.get(strID).onResult(jrpc);
                }
                /*
                JSONArray jsonArray = new JSONArray(srpc);
                Log.i(TAG, "Number of entries " + jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Log.i(TAG, jsonObject.getString("method"));
                }
                */
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void MqttPublish(String topic, String payload, int qos, boolean retain) {
        Log.d(TAG, "<= MqttPublish:");
        Log.d(TAG, "\t topic:" + topic);
        Log.d(TAG, "\t payload:" + payload);
        Log.d(TAG, "\t qos:" + qos);
        Log.d(TAG, "\t retian:" + retain);
        try {
            mClient.publish(topic, payload.getBytes(), qos, retain);
        } catch (MqttException e) {
            Log.e(TAG, "reason " + e.getReasonCode());
            Log.e(TAG, "msg " + e.getMessage());
            Log.e(TAG, "loc " + e.getLocalizedMessage());
            Log.e(TAG, "cause " + e.getCause());
            Log.e(TAG, "excep " + e);
            e.printStackTrace();
        }
    }

    public void MqttSubscribe(String topic, int qos) {
        Log.d(TAG, "<= MqttSubscribe:");
        Log.d(TAG, "\t topic:" + topic);
        Log.d(TAG, "\t qos:" + qos);
        try {
            mClient.subscribe(topic, qos);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void MqttDisconnect() {
        Log.d(TAG, "<= MqttDisconnect:");
        try {
            mClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
