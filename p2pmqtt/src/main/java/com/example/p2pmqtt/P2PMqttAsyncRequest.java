package com.example.p2pmqtt;

import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/9/22.
 */

public class P2PMqttAsyncRequest extends P2PMqttRequest {


    public P2PMqttAsyncRequest() {
        super();
    }

    public P2PMqttAsyncRequest(String whoareyou, String methodName, String methodParams) {
        super(whoareyou, methodName, methodParams, false);
    }

    @Override
    synchronized public P2PMqtt.ResultCode onResult(JSONObject jrpc){
        return mListener.onResult(jrpc);
    }
}
