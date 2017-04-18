/*
 * Author: Rio
 * Date: 2017/03/11
 */

package net.rio.wifi;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.net.wifi.p2p.*; // WifiP2pManager, WifiP2pInfo, WifiP2pGroup, WifiP2pDeviceList, WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager.*;
import android.util.Log;
import java.util.*; // List ArrayList HashMap

import net.rio.car.AppEventListener;
import net.rio.car.MainActivity;

public class WifiP2pController implements ConnectionInfoListener, GroupInfoListener, PeerListListener {

    private AppEventListener eventListener;
    private WifiP2pManager manager;
    private Channel channel;
    private HashMap<String, String> infoMap = new HashMap<String, String>();

    public WifiP2pController(Activity activity, AppEventListener eventListener) {
        manager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(activity, Looper.getMainLooper(), null);
        this.eventListener = eventListener;
    }

    public void requestInfo() {
        manager.requestConnectionInfo(channel, this);
        manager.requestGroupInfo(channel, this);
        manager.requestPeers(channel, this);
    }

    public void createGroup() {
        manager.createGroup(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(MainActivity.TAG, "Creating group success");
            }
            @Override
            public void onFailure(int err) {
                perror("Creating group failed", err);
            }
        });
    }

    public void removeGroup() {
        manager.removeGroup(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(MainActivity.TAG, "Removing group success");
            }
            @Override
            public void onFailure(int err) {
                perror("Removing group failed", err);
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if(eventListener != null) {
            infoMap.put("Owner address", info.groupOwnerAddress != null ? info.groupOwnerAddress.getHostAddress() : "");
            infoMap.put("Group Owner", info.isGroupOwner ? "Yes" : "No");

            eventListener.onInfoChanged(infoMap);
        }
    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        if(eventListener != null) {
            if(group != null) {
                infoMap.put("SSID", group.getNetworkName());
                infoMap.put("Password", group.getPassphrase());
            } else {
                infoMap.clear();
            }
            eventListener.onInfoChanged(infoMap);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList deviceList) {
        if(eventListener != null) {
            List deviceNames = new ArrayList<>();
            for(WifiP2pDevice e : deviceList.getDeviceList()) {
                deviceNames.add(e.deviceName);
            }
            eventListener.onPeerChanged(deviceNames);
        }
    }

    private void perror(String info, int err) {
        Log.e(MainActivity.TAG, info);
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
}
