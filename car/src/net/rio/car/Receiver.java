package net.rio.car;

import android.content.*;
import android.hardware.usb.*;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import net.rio.usb.UsbController;

public class Receiver extends BroadcastReceiver {

    public static final String ACTION_USB_PERMISSION = "net.rio.car.USB_PERMISSION";

    private IntentFilter filter;
    private UsbController uController;

    Receiver(UsbController uController) {

        this.uController = uController;

        filter = new IntentFilter();

        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    IntentFilter getFilter() {
        return filter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                String msg;
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    msg = "Permission granted";
                } else {
                    msg = "Permission denied";
                }
                Log.i(MainActivity.TAG, msg);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

            uController.stopConnection();

        } else if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            String msg;

            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                msg = "Wifi direct is enabled";
            } else {
                msg = "Wifi direct is disabled";
            }
            Log.i(MainActivity.TAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
