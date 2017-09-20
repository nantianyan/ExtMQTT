package com.example.p2pmqtt;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/9/20.
 */

public class P2PMqttSimpleRequest implements P2PMqtt.IMqttRpcActionListener{
    private static final String TAG = "P2PMqttSimpleRequest";
    private final Object mRequestLock = new Object();
    private boolean mIsSyncRequest;
    private String mWhoareyou;
    private String mMethodName;
    private String mMethodParams;
    private String mRequestID;
    private int mTimeOutMs = 5000;
    private boolean mIsComplete = false;
    private P2PMqtt.ResultCode mResult = P2PMqtt.ResultCode.ERROR_None;

    public P2PMqttSimpleRequest(String whoareyou, String methodName, String methodParams, Boolean sync) {
        mWhoareyou = whoareyou;
        mMethodName = methodName;
        mMethodParams = methodParams;
        mIsSyncRequest = sync;
        mRequestID = Long.toString(System.nanoTime());
    }

    public String getWhoareyou() {
        return mWhoareyou;
    }

    public String getMethodName(){
        return mMethodName;
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

    // sub class can override this
    public P2PMqtt.ResultCode onResult(JSONObject jrpc) {
        Log.d(TAG, "onResult");
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

        } else {
            Log.i(TAG, "\t Fail!");
            mResult = P2PMqtt.ResultCode.ERROR_Unknown;
        }
        notifyComplete();
        return  mResult;
    }
    //@hide, package private
    private void notifyComplete(){
        Log.d(TAG, "notify complete");
        mIsComplete = true;
        synchronized (this.mRequestLock) {
            this.mRequestLock.notifyAll();
        }
    }

    //@hide
    boolean waitComplete(){
        int waitCount = 0;
        while(true) {
            synchronized (this.mRequestLock) {
                try {
                    Log.d(TAG, "start waiting request to be completed.");
                    this.mRequestLock.wait(mTimeOutMs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(mIsComplete) {
                Log.d(TAG, "request completed.");
                mIsComplete = false;  // reset

                if (mResult == P2PMqtt.ResultCode.ERROR_None)
                    return true;
                else {
                    return  false;
                }
            }
            waitCount++;
            if(waitCount == 10) {
                //throw new RuntimeException("request time out !");
                return false;
            }
        }
    }
}
