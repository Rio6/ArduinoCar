package net.rio.car;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import java.util.*;

import net.rio.usb.UsbController;
import net.rio.wifi.*;

public class MainActivity extends Activity implements AppEventListener {

    public static final String TAG = "Robot";

    private UsbController uController;
    private WifiP2pController wController;
    private Receiver receiver;
    private RobotServer server;

    private ArrayAdapter<String> deviceAdpt;
    private Spinner deviceSpnr;
    private TextView infoText;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Declare variables
        uController = new UsbController(this);
        wController = new WifiP2pController(this, this);
        receiver = new Receiver(uController);
        server = new RobotServer(uController);

        // Setup textview
        infoText = (TextView) findViewById(R.id.info_text);

        // Setup adapter
        deviceAdpt = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        deviceAdpt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceAdpt.setNotifyOnChange(true);

        // Setup spinner
        deviceSpnr = (Spinner) findViewById(R.id.device_spinner);
        deviceSpnr.setAdapter(deviceAdpt);
        deviceSpnr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int pos, long id) {
                uController.selectDevice((String) parent.getItemAtPosition(pos));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup buttons
        Button scanBtn = (Button) findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceAdpt.clear();
                deviceAdpt.addAll(uController.getDeviceNames());
            }
        });

        Button permBtn = (Button) findViewById(R.id.perm_button);
        permBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uController.requestPermission();
                uController.startConnection();
            }
        });

        Button requestBtn = (Button) findViewById(R.id.request_button);
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wController.requestInfo();
            }
        });

        Button createGroupBtn = (Button) findViewById(R.id.create_group_button);
        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wController.createGroup();
            }
        });

        Button removeGroupBtn = (Button) findViewById(R.id.remove_group_button);
        removeGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wController.removeGroup();
            }
        });

        // Don't let screen go black
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(receiver, receiver.getFilter());
        uController.startConnection();
        server.startServer();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
        server.stopServer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uController.stopConnection();
        wController.removeGroup();
    }

    @Override
    public void onInfoChanged(HashMap<String, String> infoMap) {
        updateInfo(infoMap);
    }

    private void updateInfo(HashMap<String, String> infoMap) {
        String info = "";
        for(Map.Entry<String, String> e : infoMap.entrySet()) {
            info += e.getKey() + ": " + e.getValue() + "\n";
        }
        infoText.setText(info);
    }
}
