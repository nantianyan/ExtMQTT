package com.cmteam.cloudmedia;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * A client node definition, for both the PUSH node and PULL node
 */
public final class Node {
    private static final String TAG = "Node";
	private String mID;
	private String mNick;
	private String mRole;
	private String mDeviceName;
	private String mLocation;
	private String mStreamStatus;
	private String mGroupID;
	private String mGroupNick;

	Node(JSONObject jnode) {
		try {
			mID= jnode.getString(CloudMedia.CMField.ID.str());
			mNick = jnode.getString(CloudMedia.CMField.NICK.str());
			mRole = jnode.getString(CloudMedia.CMField.ROLE.str());
			mDeviceName = jnode.getString(CloudMedia.CMField.DEVICE_NAME.str());
			mLocation = jnode.getString(CloudMedia.CMField.LOCATION.str());
			mStreamStatus = jnode.getString(CloudMedia.CMField.STREAM_STATUS.str());
			mGroupID = jnode.getString(CloudMedia.CMField.GROUP_ID.str());
			mGroupNick = jnode.getString(CloudMedia.CMField.GROUP_NICK.str());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	Node() {
	}

	Node(String id, String nick, CloudMedia.CMRole role, String deviceName, String location, String groupID, String groupNick) {
		mID = id;
		mNick = nick;
		mRole = role.str();
		mDeviceName = deviceName;
		mLocation = location;
		if (mRole.equals(CloudMedia.CMRole.ROLE_PUSHER.str()))
			mStreamStatus = CloudMedia.CMStreamStatus.PUSHING_CLOSE.str();
		else if (mRole.equals(CloudMedia.CMRole.ROLE_PULLER.str()))
			mStreamStatus = CloudMedia.CMStreamStatus.PULLING_CLOSE.str();
		else
			mStreamStatus = CloudMedia.CMStreamStatus.UNKNOWN.str();
		mGroupID = groupID;
		mGroupNick = groupNick;
	}

	void print(){
		Log.d(TAG, "id: " + mID);
		Log.d(TAG, "nick: " + mNick);
		Log.d(TAG, "role: " + mRole);
		Log.d(TAG, "location: " + mLocation);
		Log.d(TAG, "stream_status: " + mStreamStatus);
		Log.d(TAG, "group_id: " + mGroupID);
		Log.d(TAG, "group_nick: " + mGroupNick);
	}

	public String getID(){
		return mID;
	}

	public String getNick(){
		return mNick;
	}

	public String getRole(){
		return mRole;
	}

	public String getDeviceName(){
		return mDeviceName;
	}

	public String getLocation(){
		return mLocation;
	}

	public String getStreamStatus(){
		return mStreamStatus;
	}

	public String getGroupID(){
		return mGroupID;
	}

	public String getGroupNick(){
		return mGroupNick;
	}

	public void setID(String id) {
		mID = id;
	}

	public void setNick(String nick) {
		mNick = nick;
	}

	public void setRole(String role){
		mRole = role;
	}

	public void setDeviceName(String deviceName) {
		mDeviceName = deviceName;
	}

	public void setLocation(String location){
		mLocation = location;
	}

	public void setStreamStatus(CloudMedia.CMStreamStatus status){
		mStreamStatus = status.str();
	}

	public void setGroupID(String id){
		mGroupID = id;
	}

	public void setGroupNick(String nick){
		mGroupNick = nick;
	}

}

