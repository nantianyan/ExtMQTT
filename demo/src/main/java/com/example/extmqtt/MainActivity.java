package com.example.extmqtt;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.P2PMqttAsyncRequest;
import com.example.p2pmqtt.P2PMqttRequest;
import com.example.p2pmqtt.P2PMqttRequestHandler;
import com.example.p2pmqtt.P2PMqttSyncRequest;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "demo";

    private final Context mContext = this;
    private final P2PMqtt mMqttClient = new P2PMqtt(this);

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private static final int REQUEST = 1;
    private static final int REQUEST_HELLO = 0;

    private class HelloHandler extends P2PMqttRequestHandler {
        public void HandleJrpc (JSONObject jrpc){
            try {
                Log.d(TAG, "this is hello's topicHandle");
                String method = jrpc.getString("method");
                String params = jrpc.getString("params");
                Log.d(TAG, "method:" + method);
                Toast.makeText(mContext, "some body called hello", Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sendReply("OK");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMqttClient.connect("tcp://139.224.128.15:1883");

        //==============================================================
        // after install the handler, remote node can call my function hello0
        P2PMqttRequestHandler handler = new HelloHandler();
        mMqttClient.installRequestHandler("hello0", handler);

        //==============================================================
        mHandlerThread = new HandlerThread("worker");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == REQUEST) {
                    switch (msg.arg1) {
                        case REQUEST_HELLO:
                            Log.d(TAG, "@handler message -- request hello");
                            /* simple request will wait the reply,
                               so it can not be called in main thread.
                               since mqtt service exist in mail thread, so such waiting will be deadlock
                            */
                            P2PMqttSyncRequest request = new P2PMqttSyncRequest();
                            request.setWhoareyou("controller");
                            request.setMethodName("hello");
                            request.setMethodParams("how are you");
                            request.setListener(P2PMqttRequest.SIMPLE_LISTENER);

                            if(mMqttClient.sendRequest(request)) {
                                Log.d(TAG, "hello is called succussfull!");
                            } else {
                                Log.d(TAG, "hello is failed!");
                            }

                            break;
                    }

                }

            }
        };

        final Button buttonSyncPublish = (Button) findViewById(R.id.buttonSyncPublish);
        buttonSyncPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "@onClick send sync reqeust");
                Message m = mHandler.obtainMessage(REQUEST, REQUEST_HELLO, 0, null);
                mHandler.sendMessage(m);
            }
        });

        final Button buttonAsyncPublish = (Button) findViewById(R.id.buttonAsyncPublish);
        buttonAsyncPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "@onClick send async reqeust");
                P2PMqttAsyncRequest request = new P2PMqttAsyncRequest();
                request.setWhoareyou("controller");
                request.setMethodName("hello");
                request.setMethodParams("how are you");
                request.setListener(P2PMqttRequest.SIMPLE_LISTENER);

                if(mMqttClient.sendRequest(request)) {
                    Log.d(TAG, "hello is called succussfull!");
                } else {
                    Log.d(TAG, "hello is failed!");
                }

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMqttClient.MqttDisconnect();
    }
}
