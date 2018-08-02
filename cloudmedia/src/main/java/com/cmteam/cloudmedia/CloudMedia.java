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
    private static String sCMIP = "47.100.125.222";
    private LoginManager mLoginManager;

    private CloudMedia() {
        mLoginManager = new LoginManager();
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

    /**
     * Login to the server with a pair of account and password.
     * This API may be time-consuming, APP should not call it in main thread.
     */
    public boolean login(final String domainName, final String account, final String passwd) {
        return mLoginManager.login(domainName, account, passwd);
    }

    /**
     * Logout from the server
     * This API may be time-consuming, APP should not call it in main thread.
     */
    public boolean logout(String account) {
        return mLoginManager.logout(account);
    }

    /**
     * Get user info associate with the account which is logged in
     */
    public CMUser getUser(String account) {
        return mLoginManager.getUser(account);
    }

    public static String getServerIP() {
        return sCMIP;
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
     * A listener interface used to be notified when the remote server is reset
     */
    public interface OnServerResetListener {
        void onServerReset(String description);
    }

    /**
     * Generate a PUSH node as an actor to respond all stream requests from a PULL node
     */
    public PushNode declarePushNode(Context context, String nodeNick, String deviceName) {
        return new PushNode(context, nodeNick, deviceName);
    }

    /**
     * Generate a PULL node as an stream requestor
     */
    public PullNode declarePullNode(Context context, String nodeNick, String deviceName) {
        return new PullNode(context, nodeNick, deviceName);
    }

}
