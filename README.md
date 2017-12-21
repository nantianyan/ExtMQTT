# ExtMQTT
This java library provided a request/reply mode interface based on MQTT lib.
request/reply mode is also called P2P method or RPC sometimes.

as showed in the demo activity, the main interface is like the following:

        // connect with the broker
        mMqttClient.connect("tcp://broker's ip:1883");

        // install handler for incoming request
        P2PMqttRequestHandler handler = new HelloHandler();
        mMqttClient.installRequestHandler("hello0", handler);

        // send request to other remote mqtt node.
        // each mqtt node must have a unique ID.
        final Button buttonPublish = (Button) findViewById(R.id.buttonPublish);
        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMqttClient.sendRequest("controller", "hello", "this is hello's param");
            }
        });

TEST!
