package com.cmteam.cloudmedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A whole node list definition which contains changed nodes list
 * of all online, new online, new offline and new update. A node can
 * observe and sniff remote nodes change from NodeList if it registered
 * a OnNodesListChange listener
 */
public final class NodesList {
	private static final String CHANGE_ALL_ONLINE = "all_online";
	private static final String CHANGE_NEW_ONLINE = "new_online";
	private static final String CHANGE_NEW_OFFLINE = "new_offline";
	private static final String CHANGE_NEW_UPDATE = "new_update";

	private List<Node> mAllOnlineList = new ArrayList<Node>();
	private List<Node> mNewOnlineList = new ArrayList<Node>();
	private List<Node> mNewOfflineList = new ArrayList<Node>();
	private List<Node> mNewUpdateList = new ArrayList<Node>();

	public NodesList(String jsonStr) {
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			if (jsonObj.has(CHANGE_ALL_ONLINE)) {
				jsonArrayToList(jsonObj.getJSONArray(CHANGE_ALL_ONLINE), mAllOnlineList);
			} else {
				if (jsonObj.has(CHANGE_NEW_ONLINE)) {
					jsonArrayToList(jsonObj.getJSONArray(CHANGE_NEW_ONLINE), mNewOnlineList);
				}
				if (jsonObj.has(CHANGE_NEW_OFFLINE)) {
					jsonArrayToList(jsonObj.getJSONArray(CHANGE_NEW_OFFLINE), mNewOfflineList);
				}
				if (jsonObj.has(CHANGE_NEW_UPDATE)) {
					jsonArrayToList(jsonObj.getJSONArray(CHANGE_NEW_UPDATE), mNewUpdateList);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public List<Node> getAllOnlineList() {
		return mAllOnlineList;
	}

	public List<Node> getNewOnlineList() {
		return mNewOnlineList;
	}

	public List<Node> getNewOfflineList() {
		return mNewOfflineList;
	}

	public List<Node> getNewUpdateList() {
		return mNewUpdateList;
	}

	public void clear(){
		mAllOnlineList.clear();
		mNewOnlineList.clear();
		mNewOfflineList.clear();
		mNewUpdateList.clear();
	}

	private void jsonArrayToList(JSONArray jarray, List<Node> nodeList) {
		try {
			for (int i = 0; i < jarray.length(); i++) {
				JSONObject jnode = jarray.getJSONObject(i);
				Node node = new Node(jnode);
				nodeList.add(node);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}

