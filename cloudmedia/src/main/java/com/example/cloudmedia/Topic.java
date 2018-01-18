package com.example.cloudmedia;

public class Topic {
    public class Action {
        /**
         * RPC request
         * Payload: <Json RPC>
         */
        public static final String REQUEST = "request";

        /**
         * RPC reply
         * Payload: <Json RPC>
         */
        public static final String REPLY = "reply";

        /**
         * nodes change notify
         * Payload: {"all_online":<Json Array>,"new_online":<Json Array>,"new_offline":<Json Array>,"new_update":<Json Array>}
         */
        public static final String NODES_CHANGE = "nodes_change";

        /**
         * streaming exception notify
         * Payload: {"stream_exception":<Stream Exception>}, <Stream Exception> may be "time_expired","pusher_error","network_error"
         * or "unknown_error"
         */
        public static final String STREAM_EXCEPTION = "stream_exception";

        /**
         * exchange data between two nodes for user extended
         * Payload: user self-defined format
         */
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
            return topic;

        return arrays[2];
    }

}

