package com.example.cloudmedia;

import android.util.Log;

import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.P2PMqttAsyncRequest;
import com.example.p2pmqtt.P2PMqttRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 阳旭东 on 2017/10/18.
 */

public class RemoteMediaNode{
    private static final String TAG = "RemoteMediaNode";
    private String mWhoareyou;
    private CloudMedia mCloudMedia;
    private String mRtmpPublishUrl;
    private int mTestServer = 0;

    public static final String REQUEST_START_PUSH_MEDIA = "startPushMedia";
    public static final String REQUEST_STOP_PUSH_MEDIA = "stopPushMedia";

    private RemoteMediaNode(CloudMedia cm, String whoareyou){
        mWhoareyou = whoareyou;
        mCloudMedia = cm;
        mRtmpPublishUrl = generateRtmpPublishUrl();
    }

    public static RemoteMediaNode create(CloudMedia cm, String whoareyou){
        return new RemoteMediaNode(cm, whoareyou);
    }


    private String generateRtmpPublishUrl(){
        // { mWhoareyou + System.nanoTime() }
        switch (mTestServer){
            case 0:
                return "rtmp://video-center.alivecdn.com/cloudmedia/" + mWhoareyou + "?vhost=push.yangxudong.com";
            case 1:
                return "rtmp://192.168.199.56:1935/live/livestream";
            default:
                return "rtmp://video-center.alivecdn.com/cloudmedia/" + mWhoareyou + "?vhost=push.yangxudong.com";
        }
    }

    public void setTestTestServer(int type){
        mTestServer = type;
    }

    public String getRtmpPlayUrl() {
        switch (mTestServer){
            case 0:
                return "rtmp://push.yangxudong.com/cloudmedia/" + mWhoareyou;
            case 1:
                return "rtmp://192.168.199.56:1935/live/livestream";
            default:
                return "rtmp://push.yangxudong.com/cloudmedia/" + mWhoareyou;
        }
    }

    public String getFlvPlayUrl() {
        switch (mTestServer){
            case 0:
                return "http://push.yangxudong.com/cloudmedia/" + mWhoareyou + ".flv";
            case 1:
                return "http://192.168.199.56:1935/live/livestream.flv";
            default:
                return "http://push.yangxudong.com/cloudmedia/" + mWhoareyou + ".flv";
        }
    }

    public String getHlsPlayUrl() {
        switch (mTestServer){
            case 0:
                return "http://push.yangxudong.com/cloudmedia/" + mWhoareyou + ".m3u8";
            case 1:
                return "http://192.168.199.56:1935/live/livestream.m3u8";
            default:
                return "http://push.yangxudong.com/cloudmedia/" + mWhoareyou + ".m3u8";
        }
    }

    public boolean startPushMedia(final CloudMedia.SimpleActionListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "url", mRtmpPublishUrl);
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "expire_time", "100s");
        params = P2PMqtt.MyJsonString.addJsonBrace(params);

        return mCloudMedia.sendRequest(mWhoareyou, REQUEST_START_PUSH_MEDIA, params, listener);
    }

    public boolean stopPushMedia(final CloudMedia.SimpleActionListener listener){
        String params = "";
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "url", mRtmpPublishUrl);
        params = P2PMqtt.MyJsonString.makeKeyValueString(params, "expire_time", "100s");
        params = P2PMqtt.MyJsonString.addJsonBrace(params);
        return mCloudMedia.sendRequest(mWhoareyou, REQUEST_START_PUSH_MEDIA, params, listener);
    }

    public boolean setPushParams(String params){
        return true;
    }

    public String[] getPushParams(String[] keys){
        return null;
    }

}