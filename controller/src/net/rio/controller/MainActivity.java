/*
 * Author: Rio
 * Date: 2017/12/16
 */

package net.rio.controller;

import net.rio.wifi.RobotClient;
import net.rio.wifi.WifiP2pController;

import android.app.Activity;
import android.content.*; // Intent, IntentFilter, SharedPreferences
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*; // Menu, MenuItem, View
import android.widget.*; // Button, Spinner, TextView

import java.util.*; // List, Map, HashMap

public class MainActivity extends Activity implements AppEventListener {

    public static String TAG = "RobotControl";

    private ArrayAdapter<String> peerAdpt;
    private ControlView ctlView;
    private TextView infoText;
    private Spinner peerSpnr;

    private Receiver receiver;
    private WifiP2pController controller;
    private RobotClient robotClient;

    private String hostAddr;
    private String connStat;
    private HashMap<String, String> infoMap;
    private SharedPreferences pref;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        controller = new WifiP2pController(this, this);
        receiver = new Receiver(controller);

        ctlView = (ControlView) findViewById(R.id.control_view);
        ctlView.setOnMoveListener(new ControlView.OnMoveListener() {
            @Override
            public void onMove(float x, float y) {
                robotClient.send(new byte[]{(byte) (x * 128), (byte) (y * 128)});
            }
        });

        robotClient = new RobotClient(ctlView, this);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
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

        // Setup adapter
        peerAdpt = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
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
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, receiver.getFilter());

        if(!pref.getBoolean("p2p_server", true)) {
            hostAddr = pref.getString("host_ip", "127.0.0.1");
        }

        ctlView.setCameraAngle(Integer.valueOf(pref.getString("camera_angle", "0")));
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        robotClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }

    @Override
    public void onConnectionChanged(String status) {
        connStat = status;
        updateInfo();

        if(connStat.equals("Not connected")) {
            ctlView.clearImage();
        }
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

                        if(e.getKey().equals("Owner address")) {
                            if(pref.getBoolean("p2p_server", true)) {
                                hostAddr = e.getValue();
                            } else {
                                e.setValue(pref.getString("host_ip", "127.0.0.1"));
                            }
                        }
                    }
                }

                if(connStat != null)
                    info += connStat + "\n";

                infoText.setText(info);
            }
        });
    }
}
