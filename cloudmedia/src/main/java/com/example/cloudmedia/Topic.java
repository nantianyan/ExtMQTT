package com.example.cloudmedia;

public class Topic {
    public class Action {
        public static final String REQUEST = "request";
        public static final String REPLY = "reply";
        public static final String NODES_CHANGE = "nodes_change";
        public static final String EXCHANGE_MSG = "exchange_msg";

    }

    public static String generate(String toWho, String fromWho, String action) {
        return toWho + "/" + fromWho + "/" + action;
    }

    public static String getToWho(String topic) {
        String[] arrays = topic.split("/");
        if (arrays.length != 3)
            return null;

        return arrays[0];
    }

    public static String getFromWho(String topic) {
        String[] arrays = topic.split("/");
        if (arrays.length != 3)
            return null;

        return arrays[1];
    }

    public static String getAction(String topic) {
        String[] arrays = topic.split("/");
        if (arrays.length != 3)
            return null;

        return arrays[2];
    }

}

