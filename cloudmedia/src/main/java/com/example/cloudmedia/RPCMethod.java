package com.example.cloudmedia;

public class RPCMethod {
    /**
     * Param pairs: <CMFiled KEY>:<CMFiled VALUE>
     * Return: "OK" or "ERROR"
     */
    public static final String ONLINE = "Online";
    /**
     * Param pairs: null
     * Return: "OK" or "ERROR"
     */
    public static final String OFFLINE = "Offline";
    /**
     * Param pairs: "field":<CMFiled NAME>, "value":<CMField VALUE>
     * Return: "OK" or "ERROR"
     */
    public static final String UPDATE_FIELD = "UpdateField";

    // Below are forwarded RPC
    /**
     * PULLER -> MC:
     *   Param pairs: "target-id":<ID VALUE>, "expaire-time":"<TIME VALUE>"
     *   Return: "url":<URL> or "ERROR"
     * MC -> PUSHER:
     *   Param pairs: "url":<URL>, "expaire-time":"<TIME VALUE>"
     *   Return: "OK" or "ERROR"
     */
    public static final String START_PUSH_MEDIA = "StartPushMedia";
    /**
     * PULLER -> MC:
     *   Param pairs: "target-id":<ID VALUE>
     *   Return: "OK" or "ERROR"
     * MC -> PUSHER
     *   Param pairs: null
     *   Return: "OK" or "ERROR"
     */
    public static final String STOP_PUSH_MEDIA = "StopPushMedia";

}
