package com.example.cloudmedia;

public class RPCMethod {
	/* Param pairs: <CMFiled KEY>:<CMFiled VALUE> */
    public static final String ONLINE = "Online";
	/* Param pairs: null */
    public static final String OFFLINE = "Offline";
	/* Param pairs: "field":<CMFiled NAME>, "value":<CMField VALUE> */
    public static final String UPDATE_FIELD = "UpdateField";

    // Below are forwarded RPC
	/* Param pairs: "target-id":<ID VALUE>, "url":<URL>, "expaire-time":"<TIME VALUE>" */
    public static final String START_PUSH_MEDIA = "StartPushMedia";
	/* Param pairs: "target-id":<ID VALUE> */
    public static final String STOP_PUSH_MEDIA = "StopPushMedia";

}
