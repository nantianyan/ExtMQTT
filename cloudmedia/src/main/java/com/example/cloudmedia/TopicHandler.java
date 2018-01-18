package com.example.cloudmedia;

import java.util.HashMap;

import android.util.Log;

import com.example.p2pmqtt.MqttTopicHandler;
import com.example.p2pmqtt.P2PMqtt;

public class TopicHandler extends MqttTopicHandler {
    private static final String TAG = "TopicHandler";
    private HashMap<String, MqttTopicHandler> mTopicHandlers = new HashMap<>();
    private P2PMqtt mMqtt;

    public TopicHandler(P2PMqtt mqtt) {
        mMqtt = mqtt;
    }

    @Override
    public void onMqttMessage(String topic, String message) {
        String action = Topic.getAction(topic);
        if (mTopicHandlers.containsKey(action)) {
            mTopicHandlers.get(action).onMqttMessage(topic, message.toString());
        }
    }

    public void install(String topic, MqttTopicHandler handler) {
        Log.i(TAG, "install topic handler for:" + topic);
        mMqtt.MqttSubscribe(topic, 2);

        String action = Topic.getAction(topic);
        if (mTopicHandlers.containsKey(action)) {
            Log.w(TAG, "repeated topic handler install");
            return;
        }

        mTopicHandlers.put(action, handler);
    }

    public void uninstall(String topic) {
        Log.i(TAG, "uninstall topic handler for:" + topic);
        mMqtt.MqttUnsubscribe(topic);

        String action = Topic.getAction(topic);
        if (!mTopicHandlers.containsKey(action)) {
            Log.w(TAG, "handler has not installed for:" + topic);
            return;
        }

        mTopicHandlers.remove(action);
    }

}
