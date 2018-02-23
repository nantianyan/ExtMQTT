package com.cmteam.cloudmedia;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class CloudMedia {
    private static final String TAG = "CloudMedia";
	private static final String FIELD_UNKNOWN = "unknown";

    private static CloudMedia sCMInstance;

    private CloudMedia() {

    }

	/**
	 * Get the single CloudMedia instance for all nodes used to reach cloud media transaction
	 */
    public static CloudMedia get() {
        if (sCMInstance == null) {
            sCMInstance = new CloudMedia();
        }
        return sCMInstance;
    }

    public boolean registerAccount() {
        return true;
    }

    public boolean login() {
        return true;
    }

    public boolean logout() {
        return true;
    }

    /**
     * Indicates the roles of the client nodes on a media transaction
     */
    public enum CMRole{
        ROLE_ALL("all"),
        ROLE_PULLER("puller"),
        ROLE_PUSHER("pusher"),
        ROLE_MC("media_controller"),
        ROLE_TEST("tester"),
        UNKNOWN(FIELD_UNKNOWN);

        private final String mRoleName;
        CMRole(String roleName) {
            this.mRoleName = roleName;
        }
        public String str() {
            return this.mRoleName;
        }
    }

    /**
     * Indicates the basic properties of a client node
     */
    public enum CMField{
        ID("id"),
        NICK("nick"),
        ROLE("role"),
        DEVICE_NAME("device_name"),
        LOCATION("location"),
        STREAM_STATUS("stream_status"),
        VENDOR_ID("vendor_id"),
        VENDOR_NICK("vendor_nick"),
        GROUP_ID("group_id"),
        GROUP_NICK("group_nick");

        private final String mFiledName;
        CMField(String filedName) {
            this.mFiledName = filedName;
        }
        public String str(){
            return this.mFiledName;
        }
    }

    /**
     * Indicates the streaming status of a client node, the node must update this status
     * to MCS(Media Control Server) when the status changes by calling 
     * updateStreamStatus(CMStreamStatus status, final RPCResultListener listener)
     */
    public enum CMStreamStatus{
        PUSHING("pushing"),
        PUSHING_CLOSE("pushing_close"),
        PUSHING_ERROR("pushing_error"),
        PULLING("pulling"),
        PULLING_CLOSE("pulling_close"),
        PULLING_ERROR("pulling_error"),
        UNKNOWN(FIELD_UNKNOWN);

        private final String mStr;
        CMStreamStatus(String str){
            mStr = str;
        }
        public String str(){
            return mStr;
        }
    }

    /**
     * Stream exception definition which happens after startPushMedia returns RPC success
     * TIME_EXPIRED: the stream duration given by the PULLER is over
     * PUSHER_ERROR: some error occurs on the PUSHER
     * NETWORK_ERROR: network error occurs, for example the cloudy reports a streaming error
     * UNKNOWN_ERROR: unknown error caused
     */
    public enum CMStreamException{
        TIME_EXPIRED("time_expired"),
        PUSHER_ERROR("pusher_error"),
        NETWORK_ERROR("network_error"),
        UNKNOWN_ERROR("unknown_error");

        private final String mException;
        CMStreamException(String excep){
            mException = excep;
        }
        public String str(){
            return mException;
        }
        public static CMStreamException name(String excep) {
            for (CMStreamException se : CMStreamException.values()) {
                if (excep.equals(se.str())) {
                    return se;
                }
            }
            return UNKNOWN_ERROR;
        }
    }

    /**
     * A common listener interface used to return a RPC result
     */
    public interface RPCResultListener {
        public void onSuccess(String params);
        public void onFailure(String params);
    }

    /**
     * A listener interface used to receive data from a peer node,
     * the parameter groupID and nodeID show where the message comes from
     */
    public interface OnMessageListener {
        void onMessage(String peerGroupID, String peerNodeID, String message);
    }

    /**
     * Generate a PUSH node as an actor to respond all stream requests from a PULL node
     */
    public static PushNode declarePushNode(Context context, String nodeNick, String deviceName) {
        String nid = getIDFromServer();
        return new PushNode(context, nid, nodeNick, deviceName);
    }

    /**
     * Generate a PULL node as an stream requestor
     */
    public static PullNode declarePullNode(Context context, String nodeNick, String deviceName) {
        String nid = getIDFromServer();
        return new PullNode(context, nid, nodeNick, deviceName);
    }

    private static String getIDFromServer(){
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
            return "N" + System.nanoTime();
        }
    }

}
