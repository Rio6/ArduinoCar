/*
 * Author: Rio
 * Date: 2017/02/25
 */

package net.rio.controller;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    public static String TAG = "RobotControl";

    private IntentFilter intentFilter;
    private TextView infoText;
    private WifiP2pController controller;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        controller = new WifiP2pController(this);
        infoText = (TextView) findViewById(R.id.info_text);

        // Buttons
        Button discBtn = (Button) findViewById(R.id.disc_button);
        discBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoText.setText("" + controller.getPeerList().toString());
            }
        });

        Button connBtn = (Button) findViewById(R.id.conn_button);
        connBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.connect();
            }
        });

        // Don't let screen go black
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(controller.getReceiver(), intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(controller.getReceiver());
    }
}
