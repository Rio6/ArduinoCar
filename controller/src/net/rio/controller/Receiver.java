/*
 * Author: Rio
 * Date: 2017/02/25
 */

package net.rio.controller;

import net.rio.wifi.WifiP2pController;

import android.content.*;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

class Receiver extends BroadcastReceiver {

    private IntentFilter filter;
    private WifiP2pController controller;

    Receiver(WifiP2pController controller) {

        this.controller = controller;

        filter = new IntentFilter();

        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    IntentFilter getFilter() {
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch(action) {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:

                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                String msg;

                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                    msg = "Wifi direct is enabled";
                } else {
                    msg = "Wifi direct is disabled";
                }
                Log.i(MainActivity.TAG, msg);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

                break;
            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                controller.discoverPeers();
                controller.requestInfo();
                break;
        }
    }
}
