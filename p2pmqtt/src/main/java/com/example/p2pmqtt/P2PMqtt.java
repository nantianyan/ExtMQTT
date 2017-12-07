package com.example.p2pmqtt;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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

import java.util.HashMap;

/**
 * Created by 阳旭东 on 2017/9/7.
 */

public class P2PMqtt {
    public enum ResultCode {
        ERROR_None,
        ERROR_TimeOut,
        ERROR_BadData,
        ERROR_Unknown
    }

    public interface IMqttRpcActionListener {
        ResultCode onResult(JSONObject jrpc);
    }

    public interface IFullActionListener {
        /**
         * This method is invoked when an action has completed successfully.
         * @param
         */
        public void onSuccess(String params);
        /**
         * This method is invoked when an action fails.
         * @param
         */
        public void onFailure(String params);
    }

    private static final String TAG = "P2PMqtt";
    private Context mContext;
    private MqttAndroidClient mClient;
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
            mIsConnected = false;
        }
    };

    private MqttConnectOptions mConnectOptions;
    private String mHost;
    private String mUserName = "admin";
    private String mPassWord = "password";
    private String mClientId;

    private HashMap<String, P2PMqttRequestHandler> mRequestHandler = new HashMap<>();
    private HashMap<String, IMqttRpcActionListener> mActionListener = new HashMap<>();
    private HashMap<String, MqttTopicHandler> mTopicHandler = new HashMap<>();

    private String mWhoami;
    private String mWhoamiPwd;
    private boolean mIsConnected = false;

    public P2PMqtt(Context context) {
        this(context, "tester", "12345");
    }

    public P2PMqtt(Context context, String whoami, String password) {
        //TODO:  regist whoami and password in specific module, not here
        mContext = context;
        mWhoami = whoami;
        mWhoamiPwd = password;
    }

    public boolean connect(String host) {
        return connect(host, null);
    }

    public boolean connect(String host, final IFullActionListener onlineCallback) {
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
                //TODO: connect host with whoami and whami's password or in putOnline function?
                //mConnectOptions.setUserName(userName);
                //mConnectOptions.setPassword(passWord.toCharArray());
                //mConnectOptions.setAutomaticReconnect(true);
                mConnectOptions.setWill("cm/nodes_will", mWhoami.getBytes(), 2, false);

                mClient.connect(mConnectOptions, this, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken arg0) {
                        Log.i(TAG, "connect success ");
                        mIsConnected = true;

                        String topic = mWhoami + "/+/request";
                        MqttSubscribe(topic, 2);

                        topic = mWhoami + "/+/reply";
                        MqttSubscribe(topic, 2);

                        for(String key: mTopicHandler.keySet()) {
                            MqttSubscribe(key, 2);
                        }

                        onlineCallback.onSuccess(null);
                    }

                    @Override
                    public void onFailure(IMqttToken arg0, Throwable arg1) {
                        Log.i(TAG, "connect fail ");
                        onlineCallback.onFailure(null);
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
                // return false ??
            }
        }

        return  true;
    }

    public boolean disconnect(){
        try {
            mClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean sendRequest(P2PMqttRequest request) {
        Log.d(TAG, "sendRequest 1");
        sendRequest(request.getWhoareyou(), request.getMethodName(), request.getMethodParams(), request);
        return request.waitComplete();
    }

    public void sendRequest(String whoareyou, String methodName, String methodParam, IMqttRpcActionListener listener) {
        Log.d(TAG, "sendRequest 2");
        if(mIsConnected == false) {
            String info = "client is not online, so request:" + methodName + " is failed to send!";
            Toast.makeText(mContext, info, Toast.LENGTH_LONG).show();
        } else {
            sendRequest(whoareyou, methodName, methodParam, listener, false);
        }
    }

    public void sendRequest(String whoareyou, String methodName, String methodParam){
        sendRequest(whoareyou, methodName, methodParam, null);
    }

    private void sendRequest(String whoareyou, String methodName, String methodParam, IMqttRpcActionListener listener, boolean force){
        Log.d(TAG, "sendRequest");
        Log.d(TAG, "to: " + whoareyou + ", method: " + methodName + ", params: " + methodParam);
        if (mClient.isConnected()) {
            String id = Long.toString(System.nanoTime());
            if(listener != null) {
                Log.d(TAG, "add listener for request id: " + id);
                mActionListener.put(id, listener);
            }

            /*
            String payload = "{";
            payload = payload + "\"jsonrpc\":\"2.0\",";
            payload = payload + "\"method\":\"" + methodName + "\",";
            payload = payload + "\"params\":" + methodParam + ",";
            payload = payload + "\"id\":" + id;
            payload = payload + "}";
            */
            String payload = MyJsonString.makeJrpcString(methodName, methodParam, id);

            String topic = whoareyou + "/" + mWhoami + "/request";

            MqttPublish(topic, payload, 2, false);
        } else {
            String info = "client is not connected, so request:" + methodName + " is failed to send!";
            Log.e(TAG, info);
            Toast.makeText(mContext, info, Toast.LENGTH_LONG).show();
        }
    }

    public void installRequestHandler(String method, P2PMqttRequestHandler handler) {
        mRequestHandler.put(method, handler);
    }

    public void installTopicHandler(String topic, MqttTopicHandler handler) {
        Log.i(TAG, "install topic handler for:" + topic);
        mTopicHandler.put(topic, handler);
        if(mIsConnected) {
            MqttSubscribe(topic, 2);
        } // else the topic is subscribed when connect success.
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
                String strID = id; // we force id to use String rather int. id.toString();
                if(mActionListener.containsKey(strID)){
                    mActionListener.get(strID).onResult(jrpc);
                    mActionListener.remove("strID");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mTopicHandler.containsKey(topic)) {
            mTopicHandler.get(topic).onMqttMessage(message.toString());
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

    /**
     * this is a util tool to make String about json rpc
     */
    public static class MyJsonString{

        public static String makeJrpcString(String method, String params, String id) {
            String dst = "";
            dst = makeKeyValueString(dst, "method", method);
            dst = makeKeyValueString(dst, "params", params);
            dst = makeKeyValueString(dst, "id", id);
            return addJsonBrace(dst);
        }

        public static String makeKeyValueString(String dst, String key, String value) {
            if(value.charAt(0) == '{') {
                return putJsonString(dst, key, value);
            } else {
                return putString(dst, key, value);
            }
        }

        private static String putString(String dst, String key, String value) {
            if(dst.length() == 0) {
                return "\"" + key + "\":\"" + value + "\"";
            } else {
                return dst + ",\"" + key + "\":\"" + value + "\"";
            }
        }

        private static String putJsonString(String dst, String key, String value) {
            if(dst.length() == 0) {
                return "\"" + key + "\":" + value;
            } else {
                return dst + ",\"" + key + "\":" + value;
            }
        }

        public static String addJsonBrace(String dst){
            return "{" + dst + "}";
        }
    }
}
