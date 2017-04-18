/*
 * Author: Rio
 * Date: 2017/02/25
 */

package net.rio.controller;

import net.rio.wifi.WifiP2pController;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*; // Button, Spinner, TextView

import java.util.*; // List, Map, HashMap

public class MainActivity extends Activity implements AppEventListener {

    public static String TAG = "RobotControl";

    private ArrayAdapter<String> peerAdpt;
    private TextView infoText;
    private Spinner peerSpnr;

    private Receiver receiver;
    private WifiP2pController controller;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        controller = new WifiP2pController(this, this);
        receiver = new Receiver(controller);

        infoText = (TextView) findViewById(R.id.info_text);

        // Setup button
        Button connBtn = (Button) findViewById(R.id.conn_button);
        connBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.connect();
            }
        });

        Button dconnBtn = (Button) findViewById(R.id.dconn_button);
        dconnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.disconnect();
            }
        });

        // Setup adapter
        peerAdpt = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        peerAdpt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        peerAdpt.setNotifyOnChange(true);

        // Setup spinner
        peerSpnr = (Spinner) findViewById(R.id.peer_spinner);
        peerSpnr.setAdapter(peerAdpt);
        peerSpnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int pos, long id) {
                controller.selectPeer((String) parent.getItemAtPosition(pos));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Don't let screen go black
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(receiver, receiver.getFilter());
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    public void onPeerChanged(List<String> peerNames) {
        peerAdpt.clear();
        peerAdpt.addAll(peerNames);
    }

    @Override
    public void onInfoChanged(HashMap<String, String> infoMap) {
        String info = "";

        for(Map.Entry<String, String> e : infoMap.entrySet()) {
            info += e.getKey() + ": " + e.getValue() + "\n";
        }

        infoText.setText(info);
    }
}
