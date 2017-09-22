package com.example.p2pmqtt;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/9/22.
 */

public abstract class P2PMqttRequest implements P2PMqtt.IMqttRpcActionListener {
    private static final String TAG = "P2PMqttRequest";
    private boolean mIsSyncRequest;
    private String mWhoareyou;
    private String mMethodName;
    private String mMethodParams;
    private String mRequestID;
    private int mTimeOutMs = 5000;
    protected P2PMqtt.IMqttRpcActionListener mListener;

    public P2PMqttRequest() {
        mWhoareyou = "";
        mMethodName = "";
        mMethodParams = "";
        mIsSyncRequest = false;
        mRequestID = Long.toString(System.nanoTime());
    }

    public P2PMqttRequest(String whoareyou, String methodName, String methodParams, Boolean sync) {
        mWhoareyou = whoareyou;
        mMethodName = methodName;
        mMethodParams = methodParams;
        mIsSyncRequest = sync;
        mRequestID = Long.toString(System.nanoTime());
    }

    public abstract P2PMqtt.ResultCode onResult(JSONObject jrpc);

    public boolean waitComplete() {return true;}

    public void setWhoareyou(String whoareyou) {
        mWhoareyou = whoareyou;
    }
    public String getWhoareyou() {
        return mWhoareyou;
    }

    public void setMethodName(String methodName) {
        mMethodName = methodName;
    }
    public String getMethodName(){
        return mMethodName;
    }

    public void setMethodParams(String methodParams) {
        mMethodParams = methodParams;
    }
    public String getMethodParams(){
        return mMethodParams;
    }

    public Boolean IsSyncRequest() {
        return mIsSyncRequest;
    }

    public void setTimeOutMs(int timeOutMs) {
        assert (timeOutMs > 0);
        mTimeOutMs = timeOutMs;
    }
    public int getTimeOutMs(){return mTimeOutMs;}

    public void setListener(P2PMqtt.IMqttRpcActionListener listener) {
        mListener = listener;
    }

    public static final P2PMqtt.IMqttRpcActionListener SIMPLE_LISTENER = new P2PMqtt.IMqttRpcActionListener() {
        @Override
        public P2PMqtt.ResultCode onResult(JSONObject jrpc) {
            String result = null;
            try {
                result = jrpc.getString("result");
            } catch (JSONException e) {
                Log.d(TAG, "illeagle JSON!");
                e.printStackTrace();
            }
            Log.d(TAG, "jrpc's result is: " + result);
            if (result.equalsIgnoreCase("OK")) {
                Log.i(TAG, "\t OK!");
                return P2PMqtt.ResultCode.ERROR_None;
            } else {
                Log.i(TAG, "\t Fail!");
                return P2PMqtt.ResultCode.ERROR_Unknown;
            }
        }
    };
}
