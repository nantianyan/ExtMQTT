package com.example.extmqtt;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import com.example.p2pmqtt.P2PMqtt;
import com.example.p2pmqtt.P2PMqttRequestHandler;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "testMQTT";

    Context mContext = this;
    P2PMqtt mMqttClient = new P2PMqtt(this);

    class HelloHandler extends P2PMqttRequestHandler {
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

        P2PMqttRequestHandler handler = new HelloHandler();
        mMqttClient.installRequestHandler("hello0", handler);

        final Button buttonPublish = (Button) findViewById(R.id.buttonPublish);
        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMqttClient.sendRequest("controller", "hello", "this is hello's param");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMqttClient.MqttDisconnect();
    }
}
