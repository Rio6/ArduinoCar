/*
 * Author: Rio
 * Date: 2017/02/25
 */

package net.rio.wifi;

import android.app.Activity;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.WifiP2pManager.*;
import android.os.Looper;
import android.util.Log;
import java.util.*;

class WifiP2pController implements PeerListListener, ConnectionInfoListener {

    private WifiP2pManager manager;
    private Channel channel;

    private WifiP2pDevice peer;
    private HashMap<String, WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private AppEventListener eventListener;

    WifiP2pController(Activity activity, AppEventListener eventListener) {

        this.eventListener = eventListener;

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
                    perror("Connection failed", err);
                }
            });
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        peers.clear();
        for(WifiP2pDevice device : peerList.getDeviceList()) {
            peers.put(device.deviceName, device);
        }
        eventListener.onPeerChanged(new ArrayList<String>(peers.keySet()));
        Log.d(MainActivity.TAG, "I got " + peers.size() + " peers");
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
    }

    private perror(String msg, int err) {
        switch(err) {
            case WifiP2pManager.P2P_UNSUPPORTED:
                Log.e(MainActivity.TAG, msg + ": Wifi direct is not supported");
                break; 
            case WifiP2pManager.BUSY:
                Log.e(MainActivity.TAG, msg + ": Device or resource is busy");
                break;
            case WifiP2pManager.ERROR:
                Log.e(MainActivity.TAG, msg + ": An error occured");
                break;
        }
    }
}
