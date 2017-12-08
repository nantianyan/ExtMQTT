package com.example.p2pmqtt;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/9/12.
 */

public abstract class P2PMqttRequestHandler {
    private static final String TAG = "P2PMqttRequestHandler";
    private P2PMqtt mP2PMqtt;
    private volatile String mMqttTopic;
    private volatile MqttMessage mMqttMessage;
    private volatile JSONObject mJrpc;

    public abstract String HandleJrpc(final JSONObject jrpc);

    public void onMqttMessage(P2PMqtt p2pmqtt, String topic, MqttMessage message) {
        mP2PMqtt = p2pmqtt;
        mMqttTopic = topic;
        mMqttMessage = message;

        Log.d(TAG, "onMqttMessage:");
        Log.d(TAG, "\t topic:" + mMqttTopic);
        Log.d(TAG, "\t message:" + mMqttMessage);

        try {
            mJrpc = new JSONObject(message.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String result = HandleJrpc(mJrpc);
        sendReply(result);
    }

    private void sendReply(String result) {
        Log.d(TAG, "sendReply:");
        Log.d(TAG, "\t result:" + result);

        String id = "";
        try {
            id = mJrpc.getString("id");
            Log.d(TAG, "\t id:" + id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String replyTopic = mMqttTopic.split("/")[1] + "/" + mMqttTopic.split("/")[0] + "/reply";

        String payload = "";
        payload = P2PMqtt.MyJsonString.makeKeyValueString(payload,"jsonrpc", "2.0");
        payload = P2PMqtt.MyJsonString.makeKeyValueString(payload,"result", result);
        payload = P2PMqtt.MyJsonString.makeKeyValueString(payload,"id", "" + id);
        payload = P2PMqtt.MyJsonString.addJsonBrace(payload);

        mP2PMqtt.MqttPublish(replyTopic, payload, 2, false);
    }
}
