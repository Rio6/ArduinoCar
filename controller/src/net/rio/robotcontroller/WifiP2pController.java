package net.rio.robotcontroller;

import android.app.Activity;
import android.content.*;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.WifiP2pManager.*;
import android.os.Looper;
import android.util.Log;
import java.util.*;

class WifiP2pController implements PeerListListener, ConnectionInfoListener {

    private WifiP2pManager manager;
    private Channel channel;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    private BroadcastReceiver receiver = new BroadcastReceiver() {;
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                    Log.d(MainActivity.TAG, "Wifi direct is enabled");
                } else {
                    Log.d(MainActivity.TAG, "Wifi direct is disabled");
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if (manager != null) {
                    manager.requestPeers(channel, WifiP2pController.this);
                }
            }
        }
    };

    WifiP2pController(Activity activity) {

        manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(activity, Looper.getMainLooper(), null);

        // Start for discovering peers
        manager.discoverPeers(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(MainActivity.TAG, "Discovering");
            }
            @Override
            public void onFailure(int err) {
                Log.e(MainActivity.TAG, "Discover failed");
                switch(err) {
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.e(MainActivity.TAG, "Wifi direct is not supported");
                        break; 
                    case WifiP2pManager.BUSY:
                        Log.e(MainActivity.TAG, "Device or resource is busy");
                        break;
                    case WifiP2pManager.ERROR:
                        Log.e(MainActivity.TAG, "An error occured");
                        break;
                }
            }
        });
    }

    void connect() {
        if(peers.size() > 0) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = peers.get(0).deviceAddress;
            manager.connect(channel, config, new ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(MainActivity.TAG, "Connection success");
                }
                @Override
                public void onFailure(int err) {
                    Log.d(MainActivity.TAG, "Connection failed");
                }
            });
        }
    }

    List getPeerList() {
        return peers;
    }

    BroadcastReceiver getReceiver() {
        return receiver;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        Log.d(MainActivity.TAG, "I got " + peers.size() + " peers");
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
    }
}
