package com.example.cloudmedia;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.P2PMqttAsyncRequest;
import com.example.p2pmqtt.P2PMqttRequest;
import com.example.p2pmqtt.P2PMqttSyncRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 阳旭东 on 2017/10/18.
 */

public class CloudMedia {
    private static final String TAG = "CloudMedia";
    public static final String ROLE_ALL = "all";
    public static final String ROLE_PULLER = "puller";
    public static final String ROLE_PUSHER = "pusher";
    public static final String ROLE_TEST = "tester";
    public static final String ROLE_NONE = "none";
    private String mRole = ROLE_NONE;
    private Context mContext;
    private P2PMqtt mExtMqttClient;
    private String mBrokerUrl;
    private  String mMyID;
    private  String mMyNickName;

    /**
     * get uniqure ID from server.
     * NOTE: this function cannot be called from main thread!
     * @return uniqure id managed by a cloud server
     */
    private String getIDFromServer(){
        if(false) {
            URL url = null;
            HttpURLConnection conn = null;
            try {
                url = new URL("http://139.224.128.15:8085/getID");
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                //conn.setRequestProperty("action", "getID");
                conn.setUseCaches(false);
                conn.setReadTimeout(8000);
                conn.setConnectTimeout(8000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream in = conn.getInputStream();
                    BufferedReader bufReader = new BufferedReader(new InputStreamReader(in));

                    StringBuilder response = new StringBuilder();
                    String line = null;
                    while ((line = bufReader.readLine()) != null) {
                        response.append(line);
                    }
                    Log.i(TAG, "get id from server: " + response.toString());

                    return response.toString();
                } else {
                    Log.i(TAG, "http response error");
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            return null;
        } else {
            return mMyNickName + System.nanoTime();
        }
    }

    private String getBrokerUrlFromServer(){
        return "tcp://139.224.128.15:1883";
    }


    private boolean sendRequest(String targetID, String method,
                                String params, final CloudMedia.SimpleActionListener listener) {
        Log.d(TAG, "sendRequest to: " + targetID +
                ", calling: " + method +
                ", params: " + params);

        P2PMqttAsyncRequest request = new P2PMqttAsyncRequest();
        request.setWhoareyou(targetID);
        request.setMethodName(method);
        request.setMethodParams(params);
        if(listener == null) {
            request.setListener(P2PMqttRequest.SIMPLE_LISTENER);
        } else {
            request.setListener(new P2PMqtt.IMqttRpcActionListener() {
                @Override
                public P2PMqtt.ResultCode onResult(JSONObject jrpc) {
                    String result = null;
                    try {
                        result = jrpc.getString("result");
                        Log.d(TAG, "jrpc's result is: " + result);

                        if (listener.onResult(result)) {
                            return P2PMqtt.ResultCode.ERROR_None;
                        } else {
                            return P2PMqtt.ResultCode.ERROR_Unknown;
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "illeagle JSON!");
                        e.printStackTrace();
                    }

                    return P2PMqtt.ResultCode.ERROR_None;
                }
            });
        }

        return mExtMqttClient.sendRequest(request);
    }

    public boolean putOnline(final CloudMedia.SimpleActionListener listener) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());
            // String time = curDate.toString();
            String strTime = formatter.format(curDate);
            String params = "{\"whoami\":\"" + mMyID + "\"," +
                    "\"time\":\"" + strTime + "\"," +
                    "\"location\":\"longi lati\"," +
                    "\"nick\":\"" + mMyNickName + "\"," +
                    "\"role\":\"" + mRole + "\"}";

            return sendRequest("controller", "online", params, listener);
    }

    public boolean putOffline(final CloudMedia.SimpleActionListener listener) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        // String time = curDate.toString();
        String strTime = formatter.format(curDate);
        String params = "{\"whoami\":\"" + mMyID + "\"," +
                "\"time\":\"" + strTime + "\"," +
                "\"location\":\"longi lati\"," +
                "\"nick\":\"" + mMyNickName + "\"," +
                "\"role\":\"" + mRole + "\"}";

        return sendRequest("controller", "offline", params, listener);
    }

    public boolean getNodesOnline(String role, final CloudMedia.SimpleActionListener listener){
        String parmas = "{\"role\":\"" + role + "\"}";
        return sendRequest("controller", "get_nodes_online", parmas, listener);
    }

    public CloudMedia(Context context) {
        mContext = context;
        mMyNickName = "Nick";
    }

    public CloudMedia(Context context, String nickName) {
        mContext = context;
        mMyNickName = nickName;
    }

    public CloudMedia(Context context, String nickName, String role) {
        mContext = context;
        mMyNickName = nickName;
        mRole = role;  // better to user enum
    }

    public boolean connect(final SimpleActionListener listener) {
        assert(listener != null);
        mBrokerUrl = getBrokerUrlFromServer();
        mMyID = getIDFromServer();

        mExtMqttClient = new P2PMqtt(mContext, mMyID, "12345", mMyNickName);

        return mExtMqttClient.connect(mBrokerUrl,
                new P2PMqtt.IMqttRpcActionListener() {
            @Override
            public P2PMqtt.ResultCode onResult(JSONObject jrpc) {
                String result = null;
                try {
                    result = jrpc.getString("result");
                    Log.d(TAG, "jrpc's result is: " + result);
                } catch (JSONException e) {
                    Log.d(TAG, "illeagle JSON!");
                    e.printStackTrace();
                }
                if (listener.onResult(result)) {
                    return P2PMqtt.ResultCode.ERROR_None;
                } else {
                    return P2PMqtt.ResultCode.ERROR_Unknown;
                }
            }
        });
    }

    public RemoteMediaNode declareRemoteMediaNode(String whoareyou){
        return RemoteMediaNode.create(mExtMqttClient, whoareyou);
    }

    public LocalMediaNode declareLocalMediaNode() {
        return new LocalMediaNode(mExtMqttClient);
    }

    // not implemented yet.
    // in our original design, LiveServer interface should exposed from MQTT media controller
    // yet another way is tack to cloud live media server directly.
    // further more, client may have no idea about his interface?
    LiveServerNode declareLiveServer(){
        return new LiveServerNode();
    }

    public interface SimpleActionListener {
        boolean onResult(String result);
    }
}
