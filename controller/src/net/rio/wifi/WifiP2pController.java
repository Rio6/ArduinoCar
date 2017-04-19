/*
 * Author: Rio
 * Date: 2017/02/25
 */

package net.rio.wifi;

import net.rio.controller.AppEventListener;
import net.rio.controller.MainActivity;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.p2p.*; // WifiP2pManager, WifiP2pDevice, WifiP2pConfig, WifiP2pDeviceList, WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager.*;
import android.os.Looper;
import android.util.Log;
import java.util.*; // Hashmap, ArrayList, List

public class WifiP2pController implements PeerListListener, ConnectionInfoListener {

    private WifiP2pManager manager;
    private Channel channel;

    private WifiP2pDevice peer;
    private HashMap<String, WifiP2pDevice> peers = new HashMap<>();
    private AppEventListener eventListener;

    private String host;

    public WifiP2pController(Activity activity, AppEventListener eventListener) {

        this.eventListener = eventListener;

        manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(activity, Looper.getMainLooper(), null);

        // Start for discovering peers
        discoverPeers();
    }

    public void discoverPeers() {
        manager.discoverPeers(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(MainActivity.TAG, "Discovering");
            }
            @Override
            public void onFailure(int err) {
                perror("Discover failed", err);
            }
        });
    }

    public void selectPeer(String peerName) {
        peer = peers.get(peerName);
    }

    public void connect() {
        if(peer != null) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = peer.deviceAddress;
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

    public void requestInfo() {
        manager.requestConnectionInfo(channel, this);
        manager.requestPeers(channel, this);
    }

    public String getHostAddr() {
        return host;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if(eventListener != null) {
            HashMap<String, String> infoMap = new HashMap<>();
            infoMap.put("Owner address", info.groupOwnerAddress != null ? info.groupOwnerAddress.getHostAddress() : "");

            eventListener.onInfoChanged(infoMap);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        peers.clear();
        for(WifiP2pDevice device : peerList.getDeviceList()) {
            peers.put(device.deviceName, device);
        }

        if(eventListener != null) {
            eventListener.onPeerChanged(new ArrayList<String>(peers.keySet()));
        }
    }

    private void perror(String msg, int err) {
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
