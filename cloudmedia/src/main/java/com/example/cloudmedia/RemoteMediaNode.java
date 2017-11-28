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
        return "rtmp://video-center.alivecdn.com/cloudmedia/" + mWhoareyou + "?vhost=push.yangxudong.com";
        //return "rtmp://192.168.199.56:1935/live/livestream";
    }

    public String getRtmpPlayUrl() {
        return "rtmp://www.yangxudong.com/cloudmedia/" + mWhoareyou;
        //return "rtmp://192.168.199.56:1935/live/livestream";
    }

    public String getFlvPlayUrl() {
        return "http://www.yangxudong.com/cloudmedia/" + mWhoareyou + ".flv";
    }

    public String getHlsPlayUrl() {
        return "http://www.yangxudong.com/cloudmedia/" + mWhoareyou + ".m3u8";
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