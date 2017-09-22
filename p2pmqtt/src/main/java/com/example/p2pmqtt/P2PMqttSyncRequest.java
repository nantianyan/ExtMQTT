package com.example.p2pmqtt;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/9/20.
 */

public class P2PMqttSyncRequest extends P2PMqttRequest{
    private static final String TAG = "SimpleSyncRequest";

    private final Object mRequestLock = new Object();
    private boolean mIsComplete = false;
    private P2PMqtt.ResultCode mResult = P2PMqtt.ResultCode.ERROR_None;

    public P2PMqttSyncRequest() {
        super();
    }

    public P2PMqttSyncRequest(String whoareyou, String methodName, String methodParams) {
        super(whoareyou, methodName, methodParams, true);
    }

    @Override
    public P2PMqtt.ResultCode onResult(JSONObject jrpc) {
        mResult = mListener.onResult(jrpc);
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

    @Override
    public boolean waitComplete(){
        int waitCount = 0;
        while(true) {
            synchronized (this.mRequestLock) {
                try {
                    Log.d(TAG, "start waiting request to be completed.");
                    this.mRequestLock.wait(this.getTimeOutMs());
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
