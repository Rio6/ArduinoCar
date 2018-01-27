/*
 * Author: Rio
 * Date: 2017/12/16
 */

package net.rio.controller;

import net.rio.wifi.RobotClient;
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
    private RobotClient robotClient;

    private String hostAddr;
    private String connStat;
    private HashMap<String, String> infoMap;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        controller = new WifiP2pController(this, this);
        receiver = new Receiver(controller);
        robotClient = new RobotClient(new RobotClient.OnReceiveListener() {
            public void onReceive(byte[] data) {
                android.util.Log.d(MainActivity.TAG, "Got " + data.length);
            }
        }, this);
        connStat = "Not connected";

        infoText = (TextView) findViewById(R.id.info_text);

        // Setup button
        Button connP2pBtn = (Button) findViewById(R.id.conn_p2p_button);
        connP2pBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.connect();
            }
        });

        Button dconnP2pBtn = (Button) findViewById(R.id.dconn_p2p_button);
        dconnP2pBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.disconnect();
            }
        });

        Button connBtn = (Button) findViewById(R.id.conn_button);
        connBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robotClient.connect(hostAddr);
            }
        });

        Button dconnBtn = (Button) findViewById(R.id.dconn_button);
        dconnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robotClient.disconnect();
            }
        });

        ControlView ctlView = (ControlView) findViewById(R.id.control_view);
        ctlView.setOnMoveListener(new ControlView.OnMoveListener() {
            @Override
            public void onMove(float x, float y) {
                robotClient.send(new byte[]{(byte) (x * 128), (byte) (y * 128)});
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
        robotClient.disconnect();
    }

    @Override
    public void onConnectionChanged(String status) {
        connStat = status;
        updateInfo();
    }

    @Override
    public void onPeerChanged(List<String> peerNames) {
        peerAdpt.clear();
        peerAdpt.addAll(peerNames);
    }

    @Override
    public void onInfoChanged(HashMap<String, String> infoMap) {
        this.infoMap = infoMap;
        updateInfo();
    }

    private void updateInfo() {

        // Might be called on other thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String info = "";

                if(infoMap != null) {
                    for(Map.Entry<String, String> e : infoMap.entrySet()) {
                        info += e.getKey() + ": " + e.getValue() + "\n";

                        if(e.getKey().equals("Owner address")) hostAddr = e.getValue();
                    }
                }

                if(connStat != null)
                    info += connStat + "\n";

                infoText.setText(info);
            }
        });
    }
}
