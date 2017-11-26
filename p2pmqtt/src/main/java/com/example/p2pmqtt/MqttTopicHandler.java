package com.example.p2pmqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/11/23.
 */

public abstract class MqttTopicHandler {
    public abstract void onMqttMessage(String jstr);
}
