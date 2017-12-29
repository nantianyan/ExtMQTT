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

}

