package com.example.p2pmqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/9/12.
 */

public abstract class P2PMqttRequestHandler {
    P2PMqtt mP2PMqtt;
    private String mMqttTopic;
    private MqttMessage mMqttMessage;
    private JSONObject mJrpc;

    public abstract void HandleJrpc(JSONObject jrpc);

    public void onMqttMessage(P2PMqtt p2pmqtt, String topic, MqttMessage message) {
        mP2PMqtt = p2pmqtt;
        mMqttTopic = topic;
        mMqttMessage = message;

        try {
            mJrpc = new JSONObject(message.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        HandleJrpc(mJrpc);
    }

    public void sendReply(String result) {
        int id = 0;
        try {
            id = mJrpc.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String replyTopic = mMqttTopic.split("/")[1] + "/" + mMqttTopic.split("/")[0] + "/reply";

        String payload = "{";
        payload = payload + "\"jsonrpc\":\"2.0\",";
        payload = payload + "\"result\":" + result + ",";
        payload = payload + "\"id\":" + id;
        payload = payload + "}";

        mP2PMqtt.MqttPublish(replyTopic, payload, 2, false);
    }
}
