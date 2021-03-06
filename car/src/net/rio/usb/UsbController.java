/*
 * Author: Rio
 * Date: 2017/02/21
 */

package net.rio.usb;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.*;
import android.hardware.usb.*;
import android.util.Log;
import android.widget.Toast;
import java.util.*;

import net.rio.car.MainActivity;

public class UsbController {

    private HashMap<String, UsbDevice> deviceList;
    private PendingIntent permIntent;
    private UsbDevice device;
    private UsbManager manager;
    private UsbTransmission transmission;

    public UsbController(Activity activity) {

        manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);

        permIntent = PendingIntent.getBroadcast(
                activity.getApplicationContext(), 0,
                new Intent(net.rio.car.Receiver.ACTION_USB_PERMISSION), 0);

        updateDevice();
    }

    public String[] getDeviceNames() {
        updateDevice();
        String[] rst = new String[deviceList.size()];
        UsbDevice[] e = deviceList.values().toArray(new UsbDevice[0]);
        for(int i = 0; i < rst.length; i++) {
            rst[i] = e[i].getDeviceName();
        }
        return rst;
    }

    public void selectDevice(String key) {
        stopConnection();
        device = deviceList.get(key);
        startConnection();
    }

    public void requestPermission() {
        if(device != null && !manager.hasPermission(device)) {
            manager.requestPermission(device, permIntent);
        }
    }

    public boolean startConnection() {
        if(device == null || !manager.hasPermission(device)) {
            Log.d(MainActivity.TAG, "Can't access usb");
            return false;
        }
        stopConnection();

        try {
            transmission = new UsbTransmission(manager, device);
        } catch(RuntimeException e) {
            Log.d(MainActivity.TAG, "Error starting connection: " + e.getMessage());
            return false;
        }

        return true;
    }

    public void stopConnection() {
        if(transmission != null) {
            transmission.stop();
            transmission = null;
        }
    }

    public void send(byte[] buff) {
        if(transmission == null) {
            if(!startConnection())
                return;
        }
        transmission.send(buff);
    }

    private void updateDevice() {
        deviceList = manager.getDeviceList();
    }
}
